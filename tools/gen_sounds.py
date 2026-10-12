import json
import os

import numpy as np
import soundfile as sf
from scipy import signal

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "common", "src", "main", "resources", "assets", "dbtenkaichi")
SR = 44100


def n_of(seconds):
    return max(1, int(SR * seconds))


def tt(seconds):
    return np.arange(n_of(seconds)) / SR


def white(seconds, rng):
    return rng.standard_normal(n_of(seconds))


def pink(seconds, rng):
    x = rng.standard_normal(n_of(seconds))
    b = [0.049922035, -0.095993537, 0.050612699, -0.004408786]
    a = [1, -2.494956002, 2.017265875, -0.522189400]
    return signal.lfilter(b, a, x) * 4.0


def brown(seconds, rng):
    x = np.cumsum(rng.standard_normal(n_of(seconds)))
    x = signal.lfilter([1, -1], [1, -0.995], x)
    return x / (np.max(np.abs(x)) + 1e-9)


def lowpass(x, cutoff, order=4):
    sos = signal.butter(order, min(cutoff, SR * 0.45), "low", fs=SR, output="sos")
    return signal.sosfilt(sos, x)


def highpass(x, cutoff, order=4):
    sos = signal.butter(order, max(cutoff, 10), "high", fs=SR, output="sos")
    return signal.sosfilt(sos, x)


def bandpass(x, low, high, order=3):
    sos = signal.butter(order, [max(low, 10), min(high, SR * 0.45)], "band", fs=SR, output="sos")
    return signal.sosfilt(sos, x)


def resonator(x, freq, q):
    b, a = signal.iirpeak(min(freq, SR * 0.45), q, fs=SR)
    return signal.lfilter(b, a, x)


def sweep_filter(x, f_start, f_end, q=2.0, kind="band", blocks=64):
    out = np.zeros_like(x)
    edges = np.linspace(0, len(x), blocks + 1).astype(int)
    zi = None
    for i in range(blocks):
        k = i / max(1, blocks - 1)
        f = f_start * (f_end / f_start) ** k
        f = min(max(f, 30.0), SR * 0.44)
        if kind == "low":
            sos = signal.butter(2, f, "low", fs=SR, output="sos")
        else:
            bw = f / q
            sos = signal.butter(2, [max(f - bw / 2, 20), min(f + bw / 2, SR * 0.45)], "band", fs=SR, output="sos")
        if zi is None or zi.shape != (sos.shape[0], 2):
            zi = np.zeros((sos.shape[0], 2))
        seg, zi = signal.sosfilt(sos, x[edges[i]:edges[i + 1]], zi=zi)
        out[edges[i]:edges[i + 1]] = seg
    return out


def env_adsr(seconds, attack, decay, sustain, release, curve=2.0):
    n = n_of(seconds)
    a = n_of(attack)
    d = n_of(decay)
    r = n_of(release)
    s_len = max(0, n - a - d - r)
    parts = [np.linspace(0, 1, a) ** (1 / curve), 1 - (1 - sustain) * np.linspace(0, 1, d) ** (1 / curve),
             np.full(s_len, sustain), sustain * (1 - np.linspace(0, 1, r)) ** curve]
    e = np.concatenate(parts)
    return pad(e, seconds)


def env_exp(seconds, attack, tau):
    t = tt(seconds)
    a = max(attack, 1e-4)
    rise = np.clip(t / a, 0, 1)
    return rise * np.exp(-np.maximum(t - a, 0) / tau)


def pad(x, seconds):
    n = n_of(seconds)
    if len(x) >= n:
        return x[:n]
    return np.concatenate([x, np.zeros(n - len(x))])


def at(x, offset, total):
    out = np.zeros(n_of(total))
    o = n_of(offset)
    if o >= len(out):
        return out
    seg = x[:len(out) - o]
    out[o:o + len(seg)] = seg
    return out


def mix(*parts):
    n = max(len(p) for p in parts)
    out = np.zeros(n)
    for p in parts:
        out[:len(p)] += p
    return out


def peak(x, level=0.92):
    m = np.max(np.abs(x))
    return x if m < 1e-9 else x / m * level


def saturate(x, drive=2.0):
    return np.tanh(x * drive) / np.tanh(drive)


def compress(x, threshold=0.35, ratio=4.0, attack=0.003, release=0.08):
    level = np.abs(x)
    a = np.exp(-1.0 / (SR * attack))
    r = np.exp(-1.0 / (SR * release))
    env = np.zeros_like(x)
    g = 0.0
    for i in range(0, len(x), 32):
        v = level[i:i + 32].max()
        coeff = a if v > g else r
        g = coeff * g + (1 - coeff) * v
        env[i:i + 32] = g
    over = np.maximum(env / threshold, 1.0)
    gain = over ** (1.0 / ratio - 1.0)
    return x * gain


def make_ir(length, damping=4000.0, density=0.35, seed=7):
    rng = np.random.default_rng(seed)
    n = n_of(length)
    tail = rng.standard_normal(n) * np.exp(-np.linspace(0, 7.0, n))
    tail = lowpass(tail, damping, 2)
    early = np.zeros(n)
    for k in range(10):
        pos = int(rng.uniform(0.004, 0.06) * SR)
        if pos < n:
            early[pos] += rng.uniform(0.3, 0.8) * (1 if k % 2 == 0 else -1)
    ir = early * 0.6 + tail * density
    ir[0] = 0.0
    return ir / (np.sqrt(np.sum(ir ** 2)) + 1e-9)


IRS = {}


def reverb(x, wet=0.25, length=1.2, damping=4000.0):
    key = (length, damping)
    if key not in IRS:
        IRS[key] = make_ir(length, damping)
    w = signal.fftconvolve(x, IRS[key])[:len(x) + n_of(length) // 2]
    out = mix(x * (1 - wet * 0.5), w * wet)
    return out


def finish(x, level=0.9, room=0.18, length=0.8, tail_fade=0.02):
    y = reverb(x, room, length) if room > 0 else x
    y = highpass(y, 28, 2)
    y = compress(y, 0.45, 3.0)
    y = peak(y, level)
    f = n_of(tail_fade)
    y[-f:] *= np.linspace(1, 0, f)
    return y


def osc(freq, seconds, shape="sine", phase=0.0):
    t = tt(seconds)
    f = np.broadcast_to(np.asarray(freq, dtype=float), t.shape) if np.ndim(freq) else np.full(t.shape, float(freq))
    ph = phase + 2 * np.pi * np.cumsum(f) / SR
    if shape == "saw":
        return 2.0 * ((ph / (2 * np.pi)) % 1.0) - 1.0
    if shape == "square":
        return np.tanh(np.sin(ph) * 6.0)
    if shape == "tri":
        return 2.0 * np.abs(2.0 * ((ph / (2 * np.pi)) % 1.0) - 1.0) - 1.0
    return np.sin(ph)


def glide(f0, f1, seconds, curve=1.0):
    k = np.linspace(0, 1, n_of(seconds)) ** curve
    return f0 * (f1 / f0) ** k


def supersaw(freq, seconds, voices=7, detune=0.012, rng=None):
    rng = rng or np.random.default_rng(1)
    out = np.zeros(n_of(seconds))
    for v in range(voices):
        d = 1.0 + detune * (v - voices // 2) / max(1, voices // 2)
        out += osc(np.asarray(freq) * d, seconds, "saw", rng.uniform(0, 6.28))
    return out / voices


def fm(carrier, ratio, index, seconds, index_env=None):
    t = tt(seconds)
    c = np.asarray(carrier) if np.ndim(carrier) else np.full(t.shape, float(carrier))
    mod_phase = 2 * np.pi * np.cumsum(c * ratio) / SR
    idx = index if index_env is None else index * index_env
    car_phase = 2 * np.pi * np.cumsum(c) / SR
    return np.sin(car_phase + idx * np.sin(mod_phase))


def crackle(seconds, rng, rate=120.0, low=1500, high=9000, decay=0.0025):
    n = n_of(seconds)
    imp = np.zeros(n)
    count = int(rate * seconds)
    pos = rng.integers(0, n, count)
    imp[pos] = rng.uniform(-1, 1, count) * rng.uniform(0.3, 1.0, count)
    k = np.exp(-np.arange(n_of(decay * 6)) / (SR * decay))
    sparks = signal.fftconvolve(imp, k)[:n]
    return bandpass(sparks, low, high)


def flanger(x, depth=0.003, rate=0.3, mix_amount=0.5):
    n = len(x)
    t = np.arange(n) / SR
    delay = (depth * 0.5 * (1 + np.sin(2 * np.pi * rate * t))) * SR + 1
    idx = np.arange(n) - delay
    idx0 = np.clip(np.floor(idx).astype(int), 0, n - 1)
    frac = idx - np.floor(idx)
    idx1 = np.clip(idx0 + 1, 0, n - 1)
    delayed = x[idx0] * (1 - frac) + x[idx1] * frac
    return x * (1 - mix_amount) + delayed * mix_amount


def loopable(x, fade=0.12):
    n = n_of(fade)
    head = x[:n].copy()
    out = x[n:].copy()
    ramp = np.linspace(0, 1, n)
    out[-n:] = out[-n:] * (1 - ramp) + head * ramp
    return out


def thump(f0, f1, seconds, tau):
    return osc(glide(f0, f1, seconds, 0.35), seconds) * env_exp(seconds, 0.0015, tau)


def punch(rng, heavy=False):
    s = 0.6 if heavy else 0.32
    pitch = rng.uniform(0.9, 1.12)
    body = thump(150 * pitch, 48, s, 0.09 if heavy else 0.05) * (1.1 if heavy else 0.8)
    smack = bandpass(white(s, rng), 700 * pitch, 3200) * env_exp(s, 0.0008, 0.018 if heavy else 0.012)
    slap = highpass(white(s, rng), 3500) * env_exp(s, 0.0003, 0.006) * 0.6
    click = resonator(white(s, rng) * env_exp(s, 0.0002, 0.0015), 2600 * pitch, 6) * 0.8
    cloth = bandpass(white(s, rng), 1200, 6000) * env_exp(s, 0.004, 0.04) * 0.18
    out = mix(body, smack * 0.9, slap, click, cloth)
    if heavy:
        sub = thump(70, 32, s, 0.16) * 0.8
        crunch = bandpass(crackle(s, rng, 400, 800, 5000, 0.004), 800, 5000) * env_exp(s, 0.002, 0.06) * 0.7
        out = mix(saturate(out, 1.8), sub, crunch)
    return finish(out, 0.92, 0.22 if heavy else 0.12, 0.6)


def whoosh(rng, seconds, f0, f1, peak_at=0.5, q=1.6, level=0.85):
    base = pink(seconds, rng)
    x = np.linspace(0, 1, len(base))
    e = np.where(x < peak_at, (x / peak_at) ** 2, ((1 - x) / (1 - peak_at)) ** 1.6)
    swept = sweep_filter(base, f0, f1, q)
    air = highpass(white(seconds, rng), 5000) * 0.08
    return finish(flanger((swept + air) * e, 0.002, 1.5, 0.35), level, 0.08, 0.4)


def explosion(rng, seconds=3.0, size=1.0):
    crack = highpass(white(seconds, rng), 1800) * env_exp(seconds, 0.0005, 0.012) * 0.9
    boom = thump(90, 28, seconds, 0.35) * 1.4 * size
    body = lowpass(brown(seconds, rng), 900) * env_exp(seconds, 0.004, 0.35) * 1.6
    roar = sweep_filter(pink(seconds, rng), 2500, 120, 1.2) * env_exp(seconds, 0.01, 0.6) * 0.8
    rumble = lowpass(brown(seconds, rng), 180) * env_adsr(seconds, 0.05, 0.4, 0.6, seconds * 0.6) * 1.5
    debris = crackle(seconds, rng, 70, 1500, 7000, 0.003) * env_exp(seconds, 0.05, 0.9) * 0.6
    out = saturate(mix(crack, boom, body, roar, rumble, debris), 1.5)
    return finish(out, 0.95, 0.3, 1.6)


def energy_hum(rng, seconds, base, brightness=1.0, wobble=5.0):
    t = tt(seconds)
    b = float(np.mean(brightness))
    wobble = float(np.mean(wobble))
    vib = 1.0 + 0.006 * np.sin(2 * np.pi * wobble * t) + 0.003 * np.sin(2 * np.pi * 0.7 * t)
    f = base * vib
    core = supersaw(f, seconds, 7, 0.01, rng) * 0.4
    sub = osc(f * 0.5, seconds) * 0.5
    shimmer = fm(f * 4, 1.5, 1.8, seconds) * 0.12 * brightness
    filt = sweep_filter(core, 400 * b, 1400 * b, 1.0, "low", 16)
    air = bandpass(pink(seconds, rng), 300, 3000 * b) * (0.25 + 0.1 * np.sin(2 * np.pi * wobble * 0.5 * t))
    sparks = crackle(seconds, rng, 25 * b, 2500, 9000) * 0.35 * np.asarray(brightness) / max(b, 1e-3)
    return mix(filt, sub, shimmer, air, sparks)


def tone_bell(freq, seconds, decay=0.4, ratio=3.5, index=2.5):
    e = env_exp(seconds, 0.002, decay)
    return fm(freq, ratio, index, seconds, env_exp(seconds, 0.001, decay * 0.4)) * e


def thunder(rng, seconds=3.6, distance=1.0):
    crack = highpass(white(seconds, rng), 2200) * env_exp(seconds, 0.0004, 0.01 + 0.02 * distance) * (1.2 - 0.5 * distance)
    snap = crackle(seconds, rng, 900, 1500, 9000, 0.0015) * env_exp(seconds, 0.001, 0.05) * 0.9
    roll = np.zeros(n_of(seconds))
    for _ in range(5):
        start = rng.uniform(0.02, 0.9)
        dur = seconds - start
        seg = lowpass(brown(dur, rng), rng.uniform(250, 600)) * env_exp(dur, rng.uniform(0.02, 0.2), rng.uniform(0.4, 1.0))
        roll += at(seg * rng.uniform(0.5, 1.0), start, seconds)
    boom = thump(70, 30, seconds, 0.4) * 0.8
    out = saturate(mix(crack, snap, roll * 1.6, boom), 1.4)
    return finish(out, 0.8, 0.35, 2.0)


def rumble(rng, seconds=3.4):
    base = lowpass(brown(seconds, rng), 140) * 2.0
    grind = bandpass(crackle(seconds, rng, 220, 300, 2500, 0.006), 200, 2500) * 0.5
    pebbles = crackle(seconds, rng, 40, 2000, 6000, 0.002) * 0.25
    e = env_adsr(seconds, 0.6, 0.2, 0.85, 0.9)
    trem = 0.8 + 0.2 * np.sin(2 * np.pi * 7.0 * tt(seconds))
    return finish(mix(base * trem, grind, pebbles) * e, 0.9, 0.25, 1.4)


VOWELS = {
    "a": [(800, 1.0, 80), (1150, 0.5, 90), (2900, 0.25, 120), (3900, 0.12, 130)],
    "aa": [(700, 1.0, 70), (1220, 0.55, 80), (2600, 0.3, 110), (3600, 0.15, 130)],
    "o": [(450, 1.0, 70), (800, 0.4, 80), (2830, 0.15, 100), (3500, 0.1, 130)],
}


def voice(rng, seconds, f_curve, vowel_from="o", vowel_to="aa", grit=1.0, breath=0.3):
    n = n_of(seconds)
    jitter = 1.0 + lowpass(rng.standard_normal(n), 30, 2) * 0.03
    vib = 1.0 + 0.018 * np.sin(2 * np.pi * 5.3 * tt(seconds)) * np.linspace(0.3, 1.0, n)
    f = f_curve * jitter * vib
    phase = np.cumsum(f) / SR
    frac = phase % 1.0
    glottal = np.where(frac < 0.6, 0.5 * (1 - np.cos(np.pi * frac / 0.6)), np.cos(np.pi * (frac - 0.6) / 0.8))
    glottal = np.diff(np.concatenate([[0.0], glottal]))
    shimmer = 1.0 + lowpass(rng.standard_normal(n), 40, 2) * 0.15
    src = glottal * shimmer * 40.0
    a = VOWELS[vowel_from]
    b = VOWELS[vowel_to]
    k = np.linspace(0, 1, n)
    out = np.zeros(n)
    for (f1, g1, bw1), (f2, g2, bw2) in zip(a, b):
        blocks = 24
        edges = np.linspace(0, n, blocks + 1).astype(int)
        seg_out = np.zeros(n)
        for i in range(blocks):
            kk = i / (blocks - 1)
            fc = f1 + (f2 - f1) * kk
            gain = g1 + (g2 - g1) * kk
            bw = bw1 + (bw2 - bw1) * kk
            bb, aa = signal.iirpeak(fc, fc / bw, fs=SR)
            lo = max(0, edges[i] - 2000)
            seg = signal.lfilter(bb, aa, src[lo:edges[i + 1]])[edges[i] - lo:]
            seg_out[edges[i]:edges[i + 1]] = seg * gain
        out += seg_out
    noise_breath = bandpass(white(seconds, rng), 900, 5000) * breath * (0.6 + 0.4 * k)
    raw = out + noise_breath * np.max(np.abs(out))
    return saturate(raw / (np.max(np.abs(raw)) + 1e-9), 1.0 + grit * 0.8)


def scream(rng, seconds, f0, f1):
    f = np.concatenate([glide(f0 * 0.8, f0, seconds * 0.25, 0.5), glide(f0, f1, seconds * 0.75, 0.6)])
    f = pad(f, seconds)
    f[f == 0] = f1
    main = voice(rng, seconds, f, "o", "aa", 1.0, 0.12)
    octave = voice(rng, seconds, f * 0.5, "o", "a", 0.8, 0.1) * 0.45
    e = env_adsr(seconds, 0.12, 0.3, 0.85, seconds * 0.25, 1.5)
    power = energy_hum(rng, seconds, 55, 1.2, 7.0) * np.linspace(0.0, 0.35, n_of(seconds))
    return finish(mix((main + octave) * e, power), 0.95, 0.3, 1.4)


def generate():
    rng = np.random.default_rng(2026)
    sounds = {}

    def put(event, variants, subtitle=True):
        files = []
        folder = event.split(".")[0]
        name = event.split(".", 1)[1]
        for i, data in enumerate(variants):
            rel = "%s/%s_%d" % (folder, name, i + 1)
            out = os.path.join(ASSETS, "sounds", folder, "%s_%d.ogg" % (name, i + 1))
            os.makedirs(os.path.dirname(out), exist_ok=True)
            sf.write(out, np.clip(data, -1.0, 1.0).astype(np.float32), SR, format="OGG", subtype="VORBIS")
            files.append("dbtenkaichi:" + rel)
        entry = {"sounds": files}
        if subtitle:
            entry["subtitle"] = "subtitles.dbtenkaichi." + event
        sounds[event] = entry

    put("combat.punch_light", [punch(rng) for _ in range(4)])
    put("combat.punch_heavy", [punch(rng, True) for _ in range(3)])
    put("combat.kick", [finish(mix(whoosh(rng, 0.22, 400, 3000, 0.6, 1.4, 0.5), at(punch(rng), 0.12, 0.6)), 0.92, 0.1, 0.5) for _ in range(3)])
    put("combat.swing", [whoosh(rng, rng.uniform(0.22, 0.3), rng.uniform(500, 800), rng.uniform(3500, 6000), 0.4) * 0.75 for _ in range(4)])

    def guard():
        s = 0.5
        body = thump(220, 90, s, 0.06)
        ring = resonator(white(s, rng) * env_exp(s, 0.0005, 0.004), 1900, 12) * 0.4
        ring2 = resonator(white(s, rng) * env_exp(s, 0.0005, 0.004), 3100, 18) * 0.25
        scrape = bandpass(white(s, rng), 600, 3000) * env_exp(s, 0.001, 0.05) * 0.5
        return finish(mix(body, ring, ring2, scrape), 0.9, 0.2, 0.6)

    put("combat.guard", [guard() for _ in range(3)])

    def perfect_guard():
        s = 1.2
        bell = mix(tone_bell(1320, s, 0.5, 2.0, 2.0), tone_bell(1980, s, 0.35, 3.5, 1.5) * 0.5, tone_bell(2640, s, 0.25, 1.41, 1.2) * 0.3)
        shine = sweep_filter(highpass(white(s, rng), 3000), 4000, 11000, 2.0) * env_exp(s, 0.01, 0.15) * 0.3
        return finish(mix(bell, shine, guard() * 0.6), 0.9, 0.35, 1.4)

    put("combat.perfect_guard", [perfect_guard()])
    put("combat.guard_break", [finish(mix(punch(rng, True), osc(glide(1200, 140, 0.6, 0.7), 0.6, "saw") * env_exp(0.6, 0.005, 0.15) * 0.3, crackle(0.6, rng, 300, 1500, 8000) * env_exp(0.6, 0.001, 0.12) * 0.6), 0.95, 0.3, 0.9)])

    def clash():
        s = 1.4
        a = punch(rng, True)
        b = punch(rng, True)
        shock = sweep_filter(pink(s, rng), 6000, 200, 1.4) * env_exp(s, 0.002, 0.25)
        sub = thump(60, 25, s, 0.4)
        return finish(mix(a, at(b, 0.035, s), shock * 0.8, sub), 0.95, 0.35, 1.4)

    put("combat.clash", [clash() for _ in range(2)])
    put("combat.dodge", [whoosh(rng, 0.24, 1500, 9000, 0.3, 1.8) for _ in range(3)])
    put("combat.dash", [whoosh(rng, 0.5, 250, 7000, 0.22, 1.4) for _ in range(2)])

    def vanish():
        s = 0.45
        zip_tone = osc(glide(500, 5200, s, 0.6), s, "tri") * env_exp(s, 0.01, 0.08) * 0.4
        air = whoosh(rng, s, 2000, 11000, 0.25, 2.0)
        pop = resonator(white(s, rng) * env_exp(s, 0.0003, 0.002), 3800, 8) * 0.5
        return finish(mix(zip_tone, air, at(pop, 0.1, s)), 0.9, 0.25, 0.8)

    put("combat.vanish", [vanish() for _ in range(2)])

    def charge_start():
        s = 1.6
        hum = energy_hum(rng, s, 55, 1.0, 6.0)
        e = env_adsr(s, 0.5, 0.3, 0.8, 0.6)
        rise = sweep_filter(pink(s, rng), 150, 3000, 1.2) * env_adsr(s, 0.9, 0.2, 0.5, 0.4) * 0.6
        burst = thump(110, 45, s, 0.2) * 0.6
        return finish(mix(hum * e, rise, burst), 0.9, 0.3, 1.2)

    put("ki.charge_start", [charge_start()])
    put("ki.charge_loop", [loopable(finish(energy_hum(rng, 3.0, 52, 1.2, 6.5), 0.8, 0.2, 1.0, 0.001))], False)

    def ki_blast():
        s = 0.7
        zap = osc(glide(2200, 300, s, 0.4), s, "saw") * env_exp(s, 0.002, 0.08)
        zap = sweep_filter(zap, 5000, 600, 1.0, "low", 24) * 0.5
        thrust = sweep_filter(pink(s, rng), 4000, 500, 1.6) * env_exp(s, 0.003, 0.12)
        core = fm(glide(400, 180, s), 2.0, 3.0, s, env_exp(s, 0.002, 0.06)) * env_exp(s, 0.002, 0.15) * 0.4
        sparks = crackle(s, rng, 120, 3000, 10000) * env_exp(s, 0.002, 0.15) * 0.5
        return finish(mix(zap, thrust, core, sparks, thump(140, 60, s, 0.05) * 0.5), 0.9, 0.2, 0.8)

    put("ki.blast", [ki_blast() for _ in range(4)])
    put("ki.impact", [explosion(rng, 1.4, 0.5) for _ in range(3)])

    def beam_charge():
        s = 3.2
        t = tt(s)
        k = np.linspace(0, 1, len(t))
        hum = energy_hum(rng, s, glide(60, 95, s, 1.5), 0.8 + k * 1.2, 4.0 + 10 * k)
        trem = 0.7 + 0.3 * np.sin(2 * np.pi * np.cumsum(3 + 18 * k ** 2) / SR)
        chord = sum(osc(glide(f, f * 2.0, s, 1.4), s, "sine") for f in (220, 277, 330)) * 0.12 * k ** 1.5
        inhale = sweep_filter(pink(s, rng), 120, 6000, 1.0) * k ** 2 * 0.5
        sparks = crackle(s, rng, 160, 2500, 10000) * k * 0.6
        return finish(mix(hum * trem * (0.3 + 0.7 * k), chord, inhale, sparks), 0.92, 0.3, 1.4)

    put("ki.beam_charge", [beam_charge()])

    def beam_fire():
        s = 2.6
        blast = explosion(rng, s, 0.8) * 0.8
        jet = flanger(bandpass(pink(s, rng), 150, 5000), 0.004, 0.6, 0.5) * env_adsr(s, 0.02, 0.3, 0.7, 1.2) * 0.9
        core = supersaw(glide(110, 82, s, 0.5), s, 9, 0.02, rng) * env_adsr(s, 0.01, 0.4, 0.6, 1.0) * 0.4
        core = lowpass(core, 1800)
        shimmer = fm(glide(880, 660, s), 1.5, 2.0, s) * env_exp(s, 0.01, 0.7) * 0.12
        return finish(mix(blast, jet, core, shimmer), 0.95, 0.3, 1.6)

    put("ki.beam_fire", [beam_fire()])

    def beam_loop():
        s = 3.0
        jet = flanger(bandpass(pink(s, rng), 120, 4500), 0.004, 0.5, 0.5)
        core = lowpass(supersaw(82, s, 9, 0.02, rng), 1500) * 0.5
        roar = lowpass(brown(s, rng), 250) * 1.2
        sparks = crackle(s, rng, 90, 3000, 10000) * 0.4
        return loopable(finish(mix(jet, core, roar, sparks), 0.85, 0.2, 1.0, 0.001))

    put("ki.beam_loop", [beam_loop()], False)
    put("ki.explosion", [explosion(rng, 3.4, 1.0) for _ in range(3)])

    def clash_loop():
        s = 3.0
        t = tt(s)
        jets = beam_loop()[:n_of(s)] if len(beam_loop()) >= n_of(s) else pad(beam_loop(), s)
        grind = crackle(s, rng, 400, 800, 9000, 0.003) * (0.6 + 0.4 * np.sign(np.sin(2 * np.pi * 7 * t)))
        pulse = osc(55, s) * (0.5 + 0.5 * np.sin(2 * np.pi * 2.0 * t)) * 0.4
        return loopable(finish(mix(jets, grind, pulse), 0.88, 0.25, 1.0, 0.001))

    put("ki.clash_loop", [clash_loop()])

    def takeoff():
        s = 1.0
        return finish(mix(whoosh(rng, s, 150, 4000, 0.18, 1.2), thump(80, 35, s, 0.15) * 0.8, lowpass(brown(s, rng), 200) * env_exp(s, 0.01, 0.25) * 0.8), 0.9, 0.2, 1.0)

    put("flight.takeoff", [takeoff() for _ in range(2)])

    def flight_loop():
        s = 3.0
        t = tt(s)
        gust = bandpass(pink(s, rng), 250, 2800) * (0.75 + 0.25 * np.sin(2 * np.pi * 0.66 * t) + 0.1 * np.sin(2 * np.pi * 2.3 * t))
        whistle = resonator(white(s, rng), 1700, 30) * 0.12 * (0.5 + 0.5 * np.sin(2 * np.pi * 0.33 * t))
        return loopable(finish(mix(gust, whistle, lowpass(brown(s, rng), 120) * 0.6), 0.7, 0.0, 0.5, 0.001))

    put("flight.loop", [flight_loop()], False)

    def sonic():
        s = 2.4
        crack = highpass(white(s, rng), 800) * env_exp(s, 0.0003, 0.01) * 1.2
        boom = thump(60, 22, s, 0.35) * 1.4
        tail = lowpass(brown(s, rng), 400) * env_exp(s, 0.02, 0.5) * 1.2
        sweep_air = sweep_filter(pink(s, rng), 8000, 300, 1.2) * env_exp(s, 0.005, 0.3) * 0.7
        return finish(saturate(mix(crack, boom, tail, sweep_air), 1.6), 0.95, 0.4, 2.0)

    put("flight.sonic", [sonic()])

    def land():
        s = 1.1
        impact = thump(90, 30, s, 0.18) * 1.2
        dirt = bandpass(crackle(s, rng, 300, 400, 4000, 0.004), 300, 4000) * env_exp(s, 0.002, 0.2) * 0.8
        dust = bandpass(pink(s, rng), 200, 2500) * env_exp(s, 0.01, 0.3) * 0.6
        return finish(mix(impact, dirt, dust), 0.92, 0.25, 1.0)

    put("flight.land", [land() for _ in range(2)])

    def aura_burst():
        s = 2.2
        whoom = whoosh(rng, s, 90, 6000, 0.12, 1.2, 1.0)
        boom = thump(85, 30, s, 0.3) * 1.2
        flare = supersaw(glide(110, 220, s, 0.3), s, 7, 0.015, rng)
        flare = sweep_filter(flare, 300, 4000, 1.0, "low", 24) * env_exp(s, 0.02, 0.5) * 0.5
        sparks = crackle(s, rng, 200, 2500, 10000) * env_exp(s, 0.01, 0.6) * 0.6
        return finish(mix(whoom, boom, flare, sparks), 0.95, 0.35, 1.6)

    put("aura.burst", [aura_burst() for _ in range(2)])
    put("aura.loop", [loopable(finish(energy_hum(rng, 3.0, 46, 0.9, 4.0) * 0.8, 0.75, 0.2, 1.0, 0.001))], False)

    def power_up():
        s = 3.0
        k = np.linspace(0, 1, n_of(s))
        hum = energy_hum(rng, s, glide(48, 80, s, 1.2), 0.8 + k, 5.0)
        rise = sweep_filter(pink(s, rng), 150, 5000, 1.2) * k ** 1.5 * 0.7
        chord = sum(osc(glide(f, f * 1.5, s, 1.3), s, "saw") for f in (110, 165, 220)) * 0.08 * k
        chord = lowpass(chord, 2500)
        return finish(mix(hum * (0.3 + 0.7 * k), rise, chord), 0.92, 0.3, 1.4)

    put("transform.power_up", [power_up()])
    put("transform.scream", [scream(rng, 3.0, 150, 290), scream(rng, 3.0, 190, 360), scream(rng, 3.0, 120, 250)])

    def revert():
        s = 1.6
        k = np.linspace(0, 1, n_of(s))
        hum = energy_hum(rng, s, glide(80, 35, s, 0.6), 1.2 - k, 4.0) * (1 - k) ** 1.5
        exhale = sweep_filter(pink(s, rng), 4000, 150, 1.2) * env_exp(s, 0.02, 0.5) * 0.6
        return finish(mix(hum, exhale), 0.85, 0.3, 1.2)

    put("transform.revert", [revert()])
    put("transform.thunder", [thunder(rng, 3.8, 0.2), thunder(rng, 4.2, 0.6), thunder(rng, 3.6, 0.4)])
    put("transform.rumble", [rumble(rng, 3.6), rumble(rng, 3.2)])

    def click():
        s = 0.12
        return finish(mix(tone_bell(1800, s, 0.03, 2.0, 1.0) * 0.6, resonator(white(s, rng) * env_exp(s, 0.0002, 0.001), 4200, 10) * 0.3), 0.6, 0.05, 0.2)

    put("ui.click", [click()], False)

    def confirm():
        s = 0.9
        return finish(mix(tone_bell(880, s, 0.3, 2.0, 1.5), at(tone_bell(1318.5, s, 0.35, 2.0, 1.5), 0.07, s) * 0.8, at(tone_bell(1760, s, 0.4, 2.0, 1.2), 0.14, s) * 0.6), 0.7, 0.3, 1.0)

    put("ui.confirm", [confirm()], False)

    def beat():
        s = 0.4
        return finish(mix(thump(120, 60, s, 0.08), tone_bell(440, s, 0.12, 1.0, 0.5) * 0.3), 0.75, 0.15, 0.6)

    put("training.beat", [beat()], False)

    def success():
        s = 1.2
        notes = (1046.5, 1318.5, 1568.0, 2093.0)
        return finish(mix(*[at(tone_bell(f, s, 0.45, 2.0, 1.3), i * 0.06, s) * (1.0 - i * 0.15) for i, f in enumerate(notes)]), 0.75, 0.35, 1.2)

    put("training.success", [success()])

    def grunt():
        s = 0.5
        f = glide(rng.uniform(120, 150), rng.uniform(85, 105), s, 0.7)
        v = voice(rng, s, f, "aa", "o", 1.5, 0.4)
        return finish(v * env_adsr(s, 0.02, 0.1, 0.7, 0.25), 0.85, 0.15, 0.6)

    put("voice.grunt", [grunt() for _ in range(3)])

    with open(os.path.join(ASSETS, "sounds.json"), "w", encoding="utf-8") as f:
        json.dump(sounds, f, indent=2)
    print("generated %d sound events" % len(sounds))


if __name__ == "__main__":
    generate()
