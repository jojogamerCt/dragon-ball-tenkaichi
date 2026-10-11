import json
import os

import numpy as np
import soundfile as sf

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "common", "src", "main", "resources", "assets", "dbtenkaichi")
SR = 44100


def t(seconds):
    return np.arange(int(SR * seconds)) / SR


def noise(seconds, rng):
    return rng.uniform(-1.0, 1.0, int(SR * seconds))


def band(signal, low, high):
    spectrum = np.fft.rfft(signal)
    freqs = np.fft.rfftfreq(len(signal), 1.0 / SR)
    mask = np.ones_like(freqs)
    if low > 0:
        mask *= 1.0 / (1.0 + (low / np.maximum(freqs, 1.0)) ** 4)
    if high > 0:
        mask *= 1.0 / (1.0 + (freqs / high) ** 4)
    return np.fft.irfft(spectrum * mask, len(signal))


def env(n, attack, decay_power=2.0, hold=0.0):
    x = np.linspace(0.0, 1.0, n)
    a = max(1, int(n * attack))
    e = np.ones(n)
    e[:a] = np.linspace(0.0, 1.0, a)
    tail = x[a:]
    tail = (tail - tail[0]) / max(1e-9, (1.0 - tail[0]))
    tail = np.clip((tail - hold) / max(1e-9, 1.0 - hold), 0.0, 1.0)
    e[a:] = (1.0 - tail) ** decay_power
    return e


def sweep(f0, f1, seconds, shape="sine"):
    tt = t(seconds)
    freq = f0 * (f1 / f0) ** (tt / seconds)
    phase = 2 * np.pi * np.cumsum(freq) / SR
    if shape == "saw":
        return 2.0 * ((phase / (2 * np.pi)) % 1.0) - 1.0
    if shape == "square":
        return np.sign(np.sin(phase))
    return np.sin(phase)


def norm(x, peak=0.9):
    m = np.max(np.abs(x))
    return x if m < 1e-9 else x / m * peak


def pad(x, seconds):
    n = int(SR * seconds)
    if len(x) >= n:
        return x[:n]
    return np.concatenate([x, np.zeros(n - len(x))])


def mix(*parts):
    n = max(len(p) for p in parts)
    out = np.zeros(n)
    for p in parts:
        out[:len(p)] += p
    return out


def reverb(x, amount=0.3, length=0.4):
    rng = np.random.default_rng(3)
    ir_n = int(SR * length)
    ir = rng.uniform(-1, 1, ir_n) * np.exp(-np.linspace(0, 6, ir_n))
    ir[0] = 1.0 / max(amount, 1e-3)
    wet = np.convolve(x, ir)[:len(x) + ir_n]
    return norm(mix(x, wet * amount))


def thump(freq, seconds, rng):
    tt = t(seconds)
    body = np.sin(2 * np.pi * freq * tt * (1.0 - 0.5 * tt / seconds)) * env(len(tt), 0.01, 3.0)
    return body


def punch(rng, heavy=False):
    s = 0.45 if heavy else 0.22
    crack = band(noise(s, rng), 1500, 9000) * env(int(SR * s), 0.002, 8.0)
    body = thump(70 if heavy else 110, s, rng)
    crunch = band(noise(s, rng), 200, 1800) * env(int(SR * s), 0.005, 5.0) * (0.9 if heavy else 0.5)
    out = mix(crack * 0.6, body, crunch)
    if heavy:
        out = reverb(out, 0.25, 0.35)
    return norm(out)


def whoosh(rng, seconds=0.35, low=300, high=3000, rise=0.5):
    n = int(SR * seconds)
    base = noise(seconds, rng)
    x = np.linspace(0, 1, n)
    e = np.sin(np.pi * np.clip(x / rise, 0, 1) * 0.5) * (1 - np.clip((x - rise) / (1 - rise), 0, 1)) ** 1.5
    lowband = band(base, low, high * 0.5)
    highband = band(base, high * 0.5, high)
    return norm(lowband * e * (1 - x) + highband * e * x)


def explosion(rng, seconds=2.4, size=1.0):
    n = int(SR * seconds)
    rumble = band(noise(seconds, rng), 20, 400) * env(n, 0.005, 2.5)
    blast = band(noise(seconds, rng), 300, 6000) * env(n, 0.002, 6.0)
    sub = thump(45, seconds, rng)
    return norm(reverb(mix(rumble * 1.2 * size, blast * 0.7, sub * 0.9), 0.3, 0.6))


def hum(rng, seconds, base_freq=55.0, wobble=3.0, brightness=1.0):
    tt = t(seconds)
    vib = 1.0 + 0.02 * np.sin(2 * np.pi * wobble * tt)
    tone = sum(np.sin(2 * np.pi * base_freq * (k + 1) * tt * vib) / (k + 1) for k in range(6))
    air = band(noise(seconds, rng), 100, 1500 * brightness) * 0.6
    return norm(tone * 0.6 + air)


def loopable(x, fade=0.08):
    n = int(SR * fade)
    head = x[:n].copy()
    out = x[n:].copy()
    ramp = np.linspace(0, 1, n)
    out[-n:] = out[-n:] * (1 - ramp) + head * ramp
    return out


def tone(freq, seconds, decay=4.0, harmonics=(1.0, 0.5, 0.25)):
    tt = t(seconds)
    s = sum(a * np.sin(2 * np.pi * freq * (i + 1) * tt) for i, a in enumerate(harmonics))
    return s * env(len(tt), 0.005, decay)


def roar(rng, seconds=2.4, start=110.0, end=240.0):
    saw = sweep(start, end, seconds, "saw")
    tt = t(seconds)
    vib = np.sin(2 * np.pi * 5.5 * tt) * 0.15
    saw = saw * (1 + vib)
    formant1 = band(saw, 500, 900)
    formant2 = band(saw, 1100, 1700)
    breath = band(noise(seconds, rng), 800, 4000) * 0.35
    e = env(len(tt), 0.08, 1.2, hold=0.5)
    return norm(reverb((formant1 + formant2 * 0.6 + breath) * e, 0.3, 0.5))


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

    put("combat.punch_light", [punch(rng) for _ in range(3)])
    put("combat.punch_heavy", [punch(rng, True) for _ in range(3)])
    put("combat.kick", [norm(mix(whoosh(rng, 0.18, 400, 2500, 0.7) * 0.5, punch(rng))) for _ in range(2)])
    put("combat.swing", [whoosh(rng, 0.25, 500, 4000, 0.4) * 0.7 for _ in range(3)])
    put("combat.guard", [norm(mix(thump(160, 0.3, rng), band(noise(0.3, rng), 400, 2500) * env(int(SR * 0.3), 0.002, 6.0) * 0.5)) for _ in range(2)])
    put("combat.perfect_guard", [norm(mix(tone(1760, 0.6, 3.0, (1.0, 0.4, 0.2, 0.1)), whoosh(rng, 0.2, 2000, 8000, 0.2) * 0.3))])
    put("combat.guard_break", [norm(mix(punch(rng, True), sweep(900, 180, 0.5) * env(int(SR * 0.5), 0.01, 2.0) * 0.4))])
    put("combat.clash", [norm(reverb(mix(punch(rng, True), np.concatenate([np.zeros(int(SR * 0.04)), punch(rng, True)])), 0.4, 0.5))])
    put("combat.dodge", [whoosh(rng, 0.2, 1500, 9000, 0.3) for _ in range(2)])
    put("combat.dash", [whoosh(rng, 0.45, 300, 6000, 0.25) for _ in range(2)])
    put("combat.vanish", [norm(mix(sweep(600, 4000, 0.18) * env(int(SR * 0.18), 0.05, 1.5) * 0.5, whoosh(rng, 0.18, 2000, 10000, 0.5) * 0.6))])
    put("ki.charge_start", [norm(mix(hum(rng, 1.2, 50, 4.0) * env(int(SR * 1.2), 0.4, 1.5, 0.3), sweep(200, 600, 1.2) * env(int(SR * 1.2), 0.5, 1.0) * 0.3))])
    put("ki.charge_loop", [loopable(hum(rng, 2.2, 48, 6.0, 1.4) * 0.8)], False)
    put("ki.blast", [norm(mix(sweep(1400, 260, 0.35) * env(int(SR * 0.35), 0.005, 2.0) * 0.6, band(noise(0.35, rng), 600, 5000) * env(int(SR * 0.35), 0.003, 5.0))) for _ in range(3)])
    put("ki.impact", [norm(explosion(rng, 0.7, 0.5)) for _ in range(2)])
    put("ki.beam_charge", [norm(hum(rng, 2.6, 70, 8.0, 2.0) * env(int(SR * 2.6), 0.85, 0.5, 0.9) + sweep(150, 900, 2.6) * 0.25 * np.linspace(0, 1, int(SR * 2.6)))])
    put("ki.beam_fire", [norm(mix(explosion(rng, 1.8, 0.9), sweep(500, 180, 1.8) * env(int(SR * 1.8), 0.01, 1.0) * 0.4))])
    put("ki.beam_loop", [loopable(norm(band(noise(1.4, rng), 60, 2500) * 0.8 + hum(rng, 1.4, 62, 9.0) * 0.5))], False)
    put("ki.explosion", [explosion(rng, 2.6, 1.0) for _ in range(2)])
    put("ki.clash_loop", [norm(mix(band(noise(2.0, rng), 80, 3000) * (0.7 + 0.3 * np.sign(np.sin(2 * np.pi * 6 * t(2.0)))), hum(rng, 2.0, 40, 12.0) * 0.6))])
    put("flight.takeoff", [norm(mix(whoosh(rng, 0.6, 150, 3000, 0.2), thump(60, 0.4, rng) * 0.6))])
    put("flight.loop", [loopable(band(noise(2.0, rng), 200, 2500) * 0.5)], False)
    put("flight.sonic", [norm(reverb(mix(band(noise(0.15, rng), 50, 12000) * env(int(SR * 0.15), 0.001, 10.0), band(noise(1.6, rng), 30, 300) * env(int(SR * 1.6), 0.01, 2.0)), 0.4, 0.8))])
    put("flight.land", [norm(mix(thump(55, 0.6, rng), band(noise(0.6, rng), 150, 3000) * env(int(SR * 0.6), 0.003, 4.0) * 0.7))])
    put("aura.burst", [norm(reverb(mix(whoosh(rng, 0.9, 80, 5000, 0.15), sweep(300, 1200, 0.9) * env(int(SR * 0.9), 0.02, 2.0) * 0.3), 0.3, 0.6))])
    put("aura.loop", [loopable(hum(rng, 2.4, 44, 3.0, 0.9) * 0.6)], False)
    put("transform.power_up", [norm(mix(hum(rng, 2.0, 50, 5.0, 1.5) * np.linspace(0.2, 1.0, int(SR * 2.0)), sweep(220, 880, 2.0) * 0.25 * env(int(SR * 2.0), 0.6, 1.0)))])
    put("transform.scream", [roar(rng, 2.8, 105, 230), roar(rng, 2.8, 150, 320)])
    put("transform.revert", [norm(sweep(700, 120, 0.9) * env(int(SR * 0.9), 0.02, 2.0) + band(noise(0.9, rng), 100, 1200) * env(int(SR * 0.9), 0.02, 3.0) * 0.4)])
    put("ui.click", [norm(tone(1200, 0.06, 6.0, (1.0, 0.3)) * 0.6)], False)
    put("ui.confirm", [norm(mix(tone(880, 0.25, 4.0), np.concatenate([np.zeros(int(SR * 0.08)), tone(1320, 0.3, 4.0)])))], False)
    put("training.beat", [norm(thump(90, 0.25, rng) * 0.7)], False)
    put("training.success", [norm(mix(tone(1046, 0.4, 3.0), np.concatenate([np.zeros(int(SR * 0.06)), tone(1568, 0.4, 3.0) * 0.7])))])
    put("voice.grunt", [norm(band(sweep(180, 120, 0.35, "saw"), 300, 1600) * env(int(SR * 0.35), 0.03, 2.0) + band(noise(0.35, rng), 600, 3000) * 0.2 * env(int(SR * 0.35), 0.03, 2.0)) for _ in range(2)])

    with open(os.path.join(ASSETS, "sounds.json"), "w", encoding="utf-8") as f:
        json.dump(sounds, f, indent=2)
    print("generated %d sound events" % len(sounds))


if __name__ == "__main__":
    generate()
