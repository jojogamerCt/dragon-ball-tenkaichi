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


def clamp8(v):
    return int(max(0, min(255, v)))


def hair():
    rng = random.Random(7)
    size = 128
    img = Image.new("RGBA", (size, size))
    px = img.load()
    strand = [rng.uniform(0.78, 1.0) for _ in range(size)]
    phase = [rng.uniform(0, math.pi * 2) for _ in range(size)]
    for x in range(size):
        for y in range(size):
            local = (y % 32) / 32.0
            v = strand[x] * (1.08 - local * 0.3)
            v *= 0.92 + 0.08 * math.sin(y * 0.35 + phase[x])
            if (x + y // 6) % 9 == 0:
                v *= 0.8
            if (x * 3 + y) % 23 == 0:
                v *= 1.12
            band = math.exp(-((local - 0.22) ** 2) / 0.004)
            v += band * 0.18 * strand[x]
            g = clamp8(v * 236 + rng.uniform(-5, 5))
            px[x, y] = (g, g, g, 255)
    img.save(path("textures", "entity", "hair.png"))


def tail():
    rng = random.Random(11)
    w, h = 64, 32
    img = Image.new("RGBA", (w, h))
    px = img.load()
    for x in range(w):
        for y in range(h):
            fur = 0.8 + 0.12 * math.sin(x * 1.9 + math.sin(y * 0.8) * 2.0)
            fur += 0.08 * math.sin(y * 2.4 + x * 0.3)
            if (x * 7 + y * 3) % 11 == 0:
                fur *= 0.85
            g = clamp8(fur * 240 + rng.uniform(-8, 8))
            px[x, y] = (g, g, g, 255)
    img.save(path("textures", "entity", "tail.png"))


def feature():
    rng = random.Random(23)
    size = 128
    img = Image.new("RGBA", (size, size))
    px = img.load()
    for x in range(size):
        for y in range(size):
            local = (y % 16) / 16.0
            v = 1.0 - local * 0.16 + 0.04 * math.sin(x * 0.7) * math.sin(y * 0.5)
            if y % 16 == 0:
                v *= 1.1
            g = clamp8(v * 238 + rng.uniform(-3, 3))
            px[x, y] = (g, g, g, 255)
    img.save(path("textures", "entity", "feature.png"))


def thick_tail():
    rng = random.Random(29)
    w, h = 64, 64
    img = Image.new("RGBA", (w, h))
    px = img.load()
    for x in range(w):
        for y in range(h):
            ring = (y % 8) / 8.0
            v = 0.98 - abs(ring - 0.5) * 0.25
            if y % 8 == 0:
                v *= 0.78
            v += 0.03 * math.sin(x * 0.9)
            g = clamp8(v * 245 + rng.uniform(-3, 3))
            px[x, y] = (g, g, g, 255)
    img.save(path("textures", "entity", "thick_tail.png"))


def armor_pad():
    rng = random.Random(31)
    size = 64
    img = Image.new("RGBA", (size, size))
    px = img.load()
    for x in range(size):
        for y in range(size):
            v = 1.0 - (y % 16) / 16.0 * 0.22
            if y % 16 in (0, 1):
                v = 1.15
            if y % 16 == 13:
                v *= 0.7
            g = clamp8(v * 225 + rng.uniform(-3, 3))
            px[x, y] = (g, g, g, 255)
    img.save(path("textures", "entity", "armor_pad.png"))


def gem():
    size = 128
    img = Image.new("RGBA", (size, size))
    px = img.load()
    for x in range(size):
        for y in range(size):
            u = (x % 16) / 15.0
            v = (y % 16) / 15.0
            base = 0.62 + 0.38 * (1.0 - v) ** 1.4
            spec = math.exp(-((u - 0.3) ** 2 + (v - 0.22) ** 2) / 0.012)
            rim = 0.85 if (x % 16 in (0, 15) or y % 16 in (0, 15)) else 1.0
            g = clamp8((base * rim + spec * 0.55) * 230)
            px[x, y] = (g, g, g, 255)
    img.save(path("textures", "entity", "gem.png"))


def aura_swirl():
    size = 64
    img = Image.new("RGBA", (size, size))
    px = img.load()
    for x in range(size):
        for y in range(size):
            u = x / size
            v = y / size
            wave = math.sin((u * 3.0 + v * 1.0) * math.pi * 2) * 0.5 + 0.5
            flame = math.sin((u * 7.0 - v * 2.0) * math.pi * 2 + math.sin(v * math.pi * 4) * 1.5) * 0.5 + 0.5
            value = max(0.0, (wave * 0.55 + flame * 0.65) - 0.55) * 2.2
            value = min(1.0, value)
            c = int(value * 255)
            px[x, y] = (c, c, c, 255)
    img = img.filter(ImageFilter.GaussianBlur(1.2))
    img.save(path("textures", "entity", "aura_swirl.png"))


def orb(draw, cx, cy, r, inner, outer):
    for i in range(r, 0, -1):
        t = i / r
        col = tuple(int(inner[k] * (1 - t) + outer[k] * t) for k in range(3)) + (255,)
        draw.ellipse([cx - i, cy - i, cx + i, cy + i], fill=col)


def star(draw, cx, cy, r, color):
    points = []
    for i in range(10):
        a = -math.pi / 2 + i * math.pi / 5
        rr = r if i % 2 == 0 else r * 0.45
        points.append((cx + math.cos(a) * rr, cy + math.sin(a) * rr))
    draw.polygon(points, fill=color)


def icon():
    size = 256
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    glow = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    gd.ellipse([18, 18, size - 18, size - 18], fill=(255, 200, 80, 150))
    glow = glow.filter(ImageFilter.GaussianBlur(18))
    img.alpha_composite(glow)
    d = ImageDraw.Draw(img)
    orb(d, size // 2, size // 2, 96, (255, 236, 150), (233, 120, 20))
    for (sx, sy) in [(100, 104), (156, 104), (100, 156), (156, 156)]:
        star(d, sx, sy, 22, (214, 36, 36, 255))
    d.ellipse([78, 62, 128, 98], fill=(255, 255, 255, 120))
    img = img.resize((128, 128), Image.LANCZOS)
    img.save(path("icon.png"))


def senzu():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse([3, 5, 12, 11], fill=(92, 158, 58, 255), outline=(40, 86, 30, 255))
    d.line([(6, 7), (9, 7)], fill=(170, 220, 120, 255))
    d.point((7, 9), fill=(60, 110, 40, 255))
    img.save(path("textures", "item", "senzu_bean.png"))


def egg(name, base, spots):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse([4, 2, 12, 14], fill=base + (255,), outline=tuple(int(c * 0.55) for c in base) + (255,))
    rng = random.Random(name)
    for _ in range(5):
        x = rng.randint(5, 10)
        y = rng.randint(4, 12)
        d.rectangle([x, y, x + 1, y], fill=spots + (255,))
    d.point((6, 4), fill=(255, 255, 255, 200))
    img.save(path("textures", "item", name + ".png"))


def item_models():
    for name in ["senzu_bean", "master_spawn_egg", "saiyan_raider_spawn_egg"]:
        with open(path("models", "item", name + ".json"), "w", encoding="utf-8") as f:
            json.dump({"parent": "minecraft:item/generated", "textures": {"layer0": "dbtenkaichi:item/" + name}}, f, indent=2)
        with open(path("items", name + ".json"), "w", encoding="utf-8") as f:
            json.dump({"model": {"type": "minecraft:model", "model": "dbtenkaichi:item/" + name}}, f, indent=2)


if __name__ == "__main__":
    hair()
    tail()
    feature()
    thick_tail()
    armor_pad()
    gem()
    aura_swirl()
    print("textures and item models generated")
