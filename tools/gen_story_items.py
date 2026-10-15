import json
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "common", "src", "main", "resources", "assets", "dbtenkaichi")


def save(name, img):
    tex = os.path.join(ASSETS, "textures", "item")
    os.makedirs(tex, exist_ok=True)
    img.save(os.path.join(tex, name + ".png"))
    model_dir = os.path.join(ASSETS, "models", "item")
    os.makedirs(model_dir, exist_ok=True)
    with open(os.path.join(model_dir, name + ".json"), "w", encoding="utf-8", newline="\n") as f:
        json.dump({"parent": "minecraft:item/generated", "textures": {"layer0": "dbtenkaichi:item/" + name}}, f, indent=2)
        f.write("\n")
    item_dir = os.path.join(ASSETS, "items")
    os.makedirs(item_dir, exist_ok=True)
    with open(os.path.join(item_dir, name + ".json"), "w", encoding="utf-8", newline="\n") as f:
        json.dump({"model": {"type": "minecraft:model", "model": "dbtenkaichi:item/" + name}}, f, indent=2)
        f.write("\n")


def scouter():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    frame = (226, 226, 222, 255)
    shade = (150, 150, 150, 255)
    dark = (70, 70, 74, 255)
    for y in range(5, 12):
        for x in range(1, 4):
            px[x, y] = frame if x < 3 else shade
    for x in range(1, 4):
        px[x, 4] = dark
        px[x, 12] = dark
    px[2, 8] = (230, 60, 50, 255)
    for x in range(4, 10):
        px[x, 6] = frame
        px[x, 7] = shade
    for y in range(3, 11):
        for x in range(9, 15):
            edge = y in (3, 10) or x in (9, 14)
            if edge:
                px[x, y] = dark
            else:
                g = 200 + (x - 10) * 8 - (y - 4) * 6
                px[x, y] = (60, max(140, min(255, g)), 90, 210)
    px[11, 5] = (210, 255, 210, 230)
    px[12, 5] = (170, 255, 180, 230)
    return img


def candy():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    pink = (255, 140, 190, 255)
    light = (255, 200, 225, 255)
    dark = (200, 80, 140, 255)
    wrap = (250, 236, 120, 255)
    cx, cy = 8, 8
    for y in range(16):
        for x in range(16):
            d = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
            if d <= 4.2:
                px[x, y] = light if (x - cx) + (y - cy) < -3 else pink if d < 3.4 else dark
    for (x, y) in [(2, 6), (2, 10), (3, 7), (3, 9), (3, 8), (1, 5), (1, 11), (13, 7), (13, 9), (13, 8), (14, 6), (14, 10), (15, 5), (15, 11)]:
        px[x, y] = wrap
    return img


def main():
    save("scouter", scouter())
    save("majin_candy", candy())
    print("wrote scouter, majin_candy")


if __name__ == "__main__":
    main()
