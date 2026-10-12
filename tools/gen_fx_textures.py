import json
import math
import os
import random

from PIL import Image, ImageDraw, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "common", "src", "main", "resources", "assets", "dbtenkaichi")


def path(*parts):
    p = os.path.join(ASSETS, *parts)
    os.makedirs(os.path.dirname(p), exist_ok=True)
    return p


def clamp(v, lo=0.0, hi=1.0):
    return max(lo, min(hi, v))


def white_alpha(size, fn):
    img = Image.new("RGBA", (size, size))
    px = img.load()
    for y in range(size):
        for x in range(size):
            u = (x + 0.5) / size * 2 - 1
            v = (y + 0.5) / size * 2 - 1
            a, w = fn(u, v)
            g = int(clamp(w) * 255)
            px[x, y] = (g, g, g, int(clamp(a) * 255))
    return img


def particle_json(name, frames):
    with open(path("particles", name + ".json"), "w", encoding="utf-8") as f:
        json.dump({"textures": ["dbtenkaichi:%s_%d" % (name, i) for i in range(frames)]}, f, indent=2)


def save_particle(name, index, img):
    img.save(path("textures", "particle", "%s_%d.png" % (name, index)))


def ki_spark():
    frames = 4
    for i in range(frames):
        k = 1.0 - i / frames * 0.6
        rot = i * 0.4

        def fn(u, v):
            r = math.hypot(u, v)
            ang = math.atan2(v, u) + rot
            rays = abs(math.cos(ang * 2)) ** 12
            core = math.exp(-(r * r) / (0.06 * k))
            ray = rays * math.exp(-r * 3.2 / k)
            a = core + ray * 0.9
            return a, 0.85 + core * 0.15
        save_particle("ki_spark", i, white_alpha(16, fn))
    particle_json("ki_spark", frames)


def aura_flame():
    frames = 6
    for i in range(frames):
        t = i / (frames - 1)
        rng = random.Random(40 + i)
        wob = [rng.uniform(-0.15, 0.15) for _ in range(4)]

        def fn(u, v):
            y = (v + 1) / 2
            width = 0.9 * (y ** 0.7) * (1.0 - t * 0.3)
            cx = sum(w * math.sin((1 - y) * (k + 2) * 2.2 + i) for k, w in enumerate(wob)) * (1 - y)
            d = abs(u - cx) / max(0.05, width)
            tip = clamp((y - 0.08) / 0.4)
            body = clamp(1.0 - d) ** 1.4 * tip
            bottom = clamp((1.0 - y) / 0.12)
            a = body * bottom * (1.0 - t * 0.5)
            return a, 0.8 + body * 0.2
        save_particle("aura_flame", i, white_alpha(32, fn))
    particle_json("aura_flame", frames)


def energy_orb():
    frames = 4
    for i in range(frames):
        pulse = 0.8 + 0.2 * math.sin(i / frames * math.pi * 2)

        def fn(u, v):
            r = math.hypot(u, v) / pulse
            glow = math.exp(-r * r * 3.0)
            core = math.exp(-r * r * 14.0)
            return glow * 0.85 + core * 0.15, 0.85 + core * 0.15
        save_particle("energy_orb", i, white_alpha(16, fn))
    particle_json("energy_orb", frames)


def impact_star():
    variants = 3
    for i in range(variants):
        rng = random.Random(70 + i)
        spikes = 6 + i * 2
        offs = [rng.uniform(0.6, 1.0) for _ in range(spikes)]
        rot = rng.uniform(0, math.pi)

        def fn(u, v):
            r = math.hypot(u, v)
            ang = (math.atan2(v, u) + rot) % (math.pi * 2)
            seg = ang / (math.pi * 2) * spikes
            idx = int(seg) % spikes
            frac = seg - int(seg)
            length = offs[idx]
            spike = (1.0 - abs(frac - 0.5) * 2) ** 3
            reach = length * (0.35 + 0.65 * spike)
            a = clamp(1.0 - r / max(0.05, reach)) ** 1.5
            core = math.exp(-r * r * 10)
            return a * 0.9 + core, 0.9 + core * 0.1
        save_particle("impact_star", i, white_alpha(32, fn))
    particle_json("impact_star", variants)


def shock_ring():
    variants = 2
    for i in range(variants):
        width = 0.08 + i * 0.05

        def fn(u, v):
            r = math.hypot(u, v)
            ring = math.exp(-((r - 0.78) ** 2) / (2 * width * width))
            inner = clamp(1.0 - r / 0.78) * 0.12
            return ring + inner, 1.0
        save_particle("shock_ring", i, white_alpha(32, fn))
    particle_json("shock_ring", variants)


def dust_puff():
    frames = 4
    for i in range(frames):
        rng = random.Random(90 + i)
        blobs = [(rng.uniform(-0.4, 0.4), rng.uniform(-0.4, 0.4), rng.uniform(0.3, 0.55)) for _ in range(6)]

        def fn(u, v):
            a = 0.0
            for bx, by, br in blobs:
                d = math.hypot(u - bx, v - by) / br
                a = max(a, clamp(1.0 - d * d))
            shade = 0.75 + 0.25 * clamp(-v * 0.5 + 0.5)
            edge = clamp(1.0 - (u * u + v * v)) ** 1.5
            return a * edge * (1.0 - i * 0.15), shade
        save_particle("dust_puff", i, white_alpha(16, fn).filter(ImageFilter.GaussianBlur(0.4)))
    particle_json("dust_puff", frames)


def lightning():
    variants = 4
    size = 32
    for i in range(variants):
        rng = random.Random(110 + i)
        img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        glow = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        dg = ImageDraw.Draw(glow)
        d = ImageDraw.Draw(img)
        pts = [(rng.randint(10, 22), 0)]
        y = 0
        while y < size - 1:
            y = min(size - 1, y + rng.randint(3, 6))
            x = max(2, min(size - 3, pts[-1][0] + rng.randint(-6, 6)))
            pts.append((x, y))
        dg.line(pts, fill=(255, 255, 255, 140), width=5)
        d.line(pts, fill=(255, 255, 255, 255), width=1)
        branch = pts[len(pts) // 2]
        bpts = [branch]
        for _ in range(3):
            bpts.append((max(1, min(size - 2, bpts[-1][0] + rng.choice([-5, 5]))), min(size - 1, bpts[-1][1] + rng.randint(2, 5))))
        dg.line(bpts, fill=(255, 255, 255, 100), width=3)
        d.line(bpts, fill=(255, 255, 255, 220), width=1)
        out = Image.alpha_composite(glow.filter(ImageFilter.GaussianBlur(1.6)), img)
        save_particle("lightning", i, out)
    particle_json("lightning", variants)


def speed_line():
    def fn(u, v):
        across = math.exp(-(u * u) / 0.01)
        along = clamp(1.0 - abs(v)) ** 0.6
        return across * along, 1.0
    save_particle("speed_line", 0, white_alpha(16, fn))
    particle_json("speed_line", 1)


def rock():
    variants = 3
    for i in range(variants):
        rng = random.Random(150 + i)
        pts = []
        n = 7
        for k in range(n):
            ang = k / n * math.pi * 2 + rng.uniform(-0.25, 0.25)
            rad = rng.uniform(0.55, 0.95)
            pts.append((math.cos(ang) * rad, math.sin(ang) * rad))

        def inside(u, v):
            c = False
            for k in range(n):
                x1, y1 = pts[k]
                x2, y2 = pts[(k + 1) % n]
                if (y1 > v) != (y2 > v) and u < (x2 - x1) * (v - y1) / (y2 - y1 + 1e-9) + x1:
                    c = not c
            return c

        def fn(u, v):
            if not inside(u, v):
                return 0.0, 0.0
            light = 0.7 + 0.3 * clamp(-(u * 0.6 + v * 0.8) * 0.5 + 0.5)
            grain = 0.9 + 0.1 * math.sin(u * 23 + v * 17 + i)
            return 1.0, light * grain
        save_particle("rock", i, white_alpha(16, fn))
    particle_json("rock", variants)


def shaded_item(size, mask_fn, color_fn, light=(-0.6, -0.7), outline=0.38):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    px = img.load()
    inside = [[mask_fn((x + 0.5) / size, (y + 0.5) / size) for x in range(size)] for y in range(size)]
    for y in range(size):
        for x in range(size):
            if not inside[y][x]:
                continue
            edge = any(not (0 <= x + dx < size and 0 <= y + dy < size and inside[y + dy][x + dx]) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            u = (x + 0.5) / size
            v = (y + 0.5) / size
            base, nx, ny = color_fn(u, v)
            lam = clamp(0.62 - (nx * light[0] + ny * light[1]) * 0.5)
            r, g, b = (int(clamp(c / 255 * (0.55 + lam * 0.75)) * 255) for c in base)
            if edge:
                r, g, b = (int(c * outline) for c in base)
            px[x, y] = (r, g, b, 255)
    return img


def senzu():
    size = 32

    def mask(u, v):
        dx = (u - 0.5) / 0.36
        dy = (v - 0.52) / 0.24
        bend = 0.18 * math.sin((u - 0.5) * 3.0)
        dy2 = (v - 0.52 - bend * 0.3) / 0.24
        return dx * dx + dy2 * dy2 <= 1.0 and not (abs(u - 0.5) < 0.06 and v < 0.38)

    def color(u, v):
        nx = (u - 0.5) / 0.36
        ny = (v - 0.52) / 0.24
        crease = abs((u - 0.5) * 0.9 - (v - 0.5) * 0.4) < 0.025 and 0.3 < u < 0.7
        base = (74, 140, 52) if crease else (112, 184, 70)
        return base, nx, ny
    img = shaded_item(size, mask, color)
    d = ImageDraw.Draw(img)
    d.line([(11, 13), (15, 12)], fill=(214, 245, 170, 255))
    d.point((12, 12), fill=(240, 255, 220, 255))
    img.save(path("textures", "item", "senzu_bean.png"))


def egg(name, base, spot, accent):
    size = 32
    rng = random.Random(name)
    spots = [(rng.uniform(0.32, 0.68), rng.uniform(0.3, 0.8), rng.uniform(0.05, 0.09)) for _ in range(7)]

    def mask(u, v):
        dx = (u - 0.5) / (0.3 + (v - 0.2) * 0.12)
        dy = (v - 0.55) / 0.4
        return dx * dx + dy * dy <= 1.0

    def color(u, v):
        nx = (u - 0.5) / 0.36
        ny = (v - 0.55) / 0.4
        c = base
        for sx, sy, sr in spots:
            if math.hypot(u - sx, (v - sy) * 0.9) < sr:
                c = spot
        if abs(v - 0.62) < 0.035:
            c = accent
        return c, nx, ny
    img = shaded_item(size, mask, color)
    d = ImageDraw.Draw(img)
    d.ellipse([11, 8, 13, 11], fill=(255, 255, 255, 190))
    img.save(path("textures", "item", name + ".png"))


def icon_canvas():
    return Image.new("RGBA", (32, 32), (0, 0, 0, 0))


def glow_layer(draw_fn, blur):
    layer = icon_canvas()
    draw_fn(ImageDraw.Draw(layer))
    return layer.filter(ImageFilter.GaussianBlur(blur))


def skill_ki_blast():
    img = icon_canvas()
    img = Image.alpha_composite(img, glow_layer(lambda d: d.ellipse([12, 7, 26, 21], fill=(110, 200, 255, 230)), 3.0))
    img = Image.alpha_composite(img, glow_layer(lambda d: d.polygon([(4, 25), (15, 12), (19, 17)], fill=(120, 200, 255, 160)), 1.2))
    d = ImageDraw.Draw(img)
    d.ellipse([14, 9, 24, 19], fill=(200, 240, 255, 255))
    d.ellipse([16, 11, 21, 16], fill=(255, 255, 255, 255))
    for x, y in ((8, 22), (6, 26), (11, 19)):
        d.point((x, y), fill=(220, 245, 255, 255))
    return img


def beam_icon(core, glow, hands):
    img = icon_canvas()
    img = Image.alpha_composite(img, glow_layer(lambda d: d.polygon([(9, 13), (31, 6), (31, 26), (9, 19)], fill=glow + (220,)), 2.5))
    d = ImageDraw.Draw(img)
    d.polygon([(10, 14), (31, 9), (31, 23), (10, 18)], fill=glow + (255,))
    d.polygon([(11, 15), (31, 12), (31, 20), (11, 17)], fill=core + (255,))
    img = Image.alpha_composite(img, glow_layer(lambda dd: dd.ellipse([3, 9, 17, 23], fill=glow + (255,)), 2.0))
    d = ImageDraw.Draw(img)
    d.ellipse([6, 12, 14, 20], fill=core + (255,))
    if hands:
        skin = (236, 190, 140, 255)
        dark = (150, 100, 70, 255)
        d.polygon([(1, 8), (8, 11), (8, 15), (2, 14)], fill=skin, outline=dark)
        d.polygon([(1, 24), (8, 21), (8, 17), (2, 18)], fill=skin, outline=dark)
    return img


def skill_icons():
    icons = {
        "ki_blast": skill_ki_blast(),
        "kamehameha": beam_icon((235, 250, 255), (63, 168, 255), True),
        "raider_cannon": beam_icon((255, 236, 255), (176, 75, 255), False),
    }
    for name, img in icons.items():
        img.save(path("textures", "gui", "sprites", "skill", name + ".png"))


def ui_textures():
    def panel(w, h, top, bottom, border, highlight, name, corner=4):
        img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        px = img.load()
        for y in range(h):
            t = y / max(1, h - 1)
            col = tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(4))
            for x in range(w):
                cut = corner
                dx = min(x, w - 1 - x)
                dy = min(y, h - 1 - y)
                if dx + dy < cut - 1:
                    continue
                is_border = dx == 0 or dy == 0 or dx + dy == cut - 1
                inner = dx == 1 or dy == 1 or dx + dy == cut
                if is_border:
                    px[x, y] = border
                elif inner and y < h // 2:
                    px[x, y] = highlight
                else:
                    px[x, y] = col
        img.save(path("textures", "gui", "sprites", name + ".png"))
        with open(path("textures", "gui", "sprites", name + ".png.mcmeta"), "w", encoding="utf-8") as f:
            json.dump({"gui": {"scaling": {"type": "nine_slice", "width": w, "height": h, "border": corner + 1}}}, f, indent=2)

    panel(64, 64, (22, 26, 44, 236), (10, 12, 22, 240), (255, 196, 64, 255), (255, 230, 150, 120), "panel")
    panel(64, 20, (60, 70, 110, 255), (30, 34, 60, 255), (255, 196, 64, 255), (255, 240, 190, 140), "button")
    panel(64, 20, (110, 90, 40, 255), (70, 50, 20, 255), (255, 230, 120, 255), (255, 250, 220, 180), "button_hover")
    panel(64, 20, (40, 42, 52, 255), (26, 28, 34, 255), (90, 90, 100, 255), (120, 120, 130, 80), "button_disabled")
    panel(24, 24, (20, 22, 36, 220), (8, 10, 18, 230), (255, 196, 64, 255), (255, 230, 150, 100), "slot", corner=3)
    panel(24, 24, (80, 60, 20, 230), (40, 28, 8, 240), (255, 240, 150, 255), (255, 250, 220, 160), "slot_selected", corner=3)

    size = 72
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    px = img.load()
    for y in range(size):
        for x in range(size):
            u = (x + 0.5) / size * 2 - 1
            v = (y + 0.5) / size * 2 - 1
            r = math.hypot(u, v)
            if r > 1.0:
                continue
            if r > 0.9:
                px[x, y] = (255, 196, 64, 255)
            elif r > 0.84:
                px[x, y] = (120, 80, 20, 255)
            else:
                t = clamp((v + 1) / 2)
                px[x, y] = (int(40 - 20 * t), int(46 - 22 * t), int(80 - 40 * t), 230)
    img.save(path("textures", "gui", "sprites", "portrait_frame.png"))


def item_models():
    for name in ["senzu_bean", "master_spawn_egg", "saiyan_raider_spawn_egg"]:
        with open(path("models", "item", name + ".json"), "w", encoding="utf-8") as f:
            json.dump({"parent": "minecraft:item/generated", "textures": {"layer0": "dbtenkaichi:item/" + name}}, f, indent=2)


if __name__ == "__main__":
    ki_spark()
    aura_flame()
    energy_orb()
    impact_star()
    shock_ring()
    dust_puff()
    lightning()
    speed_line()
    rock()
    senzu()
    egg("master_spawn_egg", (240, 122, 26), (31, 63, 174), (250, 250, 250))
    egg("saiyan_raider_spawn_egg", (44, 40, 66), (176, 75, 255), (230, 220, 200))
    skill_icons()
    ui_textures()
    item_models()
    print("fx, item, skill and ui textures generated")
