import json
import os

from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "common", "src", "main", "resources")
ASSETS = os.path.join(ROOT, "assets", "dbtenkaichi")
SIZE = 32


def canvas():
    return Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))


def shade(img, light=(255, 255, 255), dark=(150, 150, 150)):
    px = img.load()
    for y in range(SIZE):
        for x in range(SIZE):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            t = (x + y) / (2.0 * SIZE)
            c = tuple(int(light[i] + (dark[i] - light[i]) * t) for i in range(3))
            px[x, y] = c + (255,)
    edge(img)
    return img


def edge(img, color=(70, 70, 70, 255)):
    px = img.load()
    src = img.copy().load()
    for y in range(SIZE):
        for x in range(SIZE):
            if src[x, y][3] == 0:
                continue
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nx, ny = x + dx, y + dy
                if nx < 0 or ny < 0 or nx >= SIZE or ny >= SIZE or src[nx, ny][3] == 0:
                    px[x, y] = color
                    break


def top_shape(d, sleeves_long):
    d.polygon([(8, 5), (13, 4), (16, 7), (19, 4), (24, 5), (29, 11), (26, 14), (23, 12), (23, 28), (9, 28), (9, 12), (6, 14), (3, 11)], fill=(255, 255, 255, 255))
    if sleeves_long:
        d.polygon([(3, 11), (6, 14), (5, 22), (2, 21)], fill=(255, 255, 255, 255))
        d.polygon([(29, 11), (26, 14), (27, 22), (30, 21)], fill=(255, 255, 255, 255))


def pants_shape(d):
    d.polygon([(9, 4), (23, 4), (25, 28), (18, 28), (16, 12), (14, 28), (7, 28)], fill=(255, 255, 255, 255))


def boots_shape(d):
    d.polygon([(5, 10), (12, 10), (12, 22), (15, 24), (15, 28), (4, 28), (4, 22)], fill=(255, 255, 255, 255))
    d.polygon([(18, 10), (25, 10), (25, 22), (28, 24), (28, 28), (17, 28), (17, 22)], fill=(255, 255, 255, 255))


def overlay(kind, piece):
    img = canvas()
    d = ImageDraw.Draw(img)
    if piece == "top":
        if kind == "gi":
            d.polygon([(13, 4), (16, 13), (19, 4), (17, 4), (16, 8), (15, 4)], fill=(40, 70, 170, 255))
            d.rectangle([9, 19, 23, 21], fill=(40, 70, 170, 255))
        elif kind == "battle_armor":
            d.rectangle([10, 8, 22, 18], fill=(238, 232, 214, 255))
            d.rectangle([3, 9, 8, 12], fill=(220, 190, 80, 255))
            d.rectangle([24, 9, 29, 12], fill=(220, 190, 80, 255))
        else:
            d.line([(16, 7), (16, 27)], fill=(60, 60, 60, 255))
    elif piece == "pants":
        if kind == "gi":
            d.rectangle([9, 4, 23, 6], fill=(40, 70, 170, 255))
        elif kind == "battle_armor":
            d.rectangle([9, 4, 23, 7], fill=(238, 232, 214, 255))
        else:
            d.rectangle([9, 4, 23, 5], fill=(60, 50, 40, 255))
    else:
        if kind == "gi":
            d.rectangle([4, 10, 12, 13], fill=(200, 40, 40, 255))
            d.rectangle([17, 10, 25, 13], fill=(200, 40, 40, 255))
        elif kind == "battle_armor":
            d.rectangle([4, 10, 12, 13], fill=(220, 190, 80, 255))
            d.rectangle([17, 10, 25, 13], fill=(220, 190, 80, 255))
        else:
            d.rectangle([4, 26, 15, 28], fill=(240, 240, 240, 255))
            d.rectangle([17, 26, 28, 28], fill=(240, 240, 240, 255))
    return img


def write_item(name):
    os.makedirs(os.path.join(ASSETS, "items"), exist_ok=True)
    os.makedirs(os.path.join(ASSETS, "models", "item"), exist_ok=True)
    with open(os.path.join(ASSETS, "items", name + ".json"), "w") as f:
        json.dump({"model": {"type": "minecraft:model", "model": "dbtenkaichi:item/" + name,
                             "tints": [{"type": "minecraft:dye", "default": -1018342}]}}, f, indent=2)
    with open(os.path.join(ASSETS, "models", "item", name + ".json"), "w") as f:
        json.dump({"parent": "minecraft:item/generated",
                   "textures": {"layer0": "dbtenkaichi:item/" + name, "layer1": "dbtenkaichi:item/" + name + "_overlay"}}, f, indent=2)


def main():
    out = os.path.join(ASSETS, "textures", "item")
    os.makedirs(out, exist_ok=True)
    names = []
    for kind in ("gi", "battle_armor", "casual"):
        for piece in ("top", "pants", "boots"):
            name = kind + "_" + piece
            base = canvas()
            d = ImageDraw.Draw(base)
            if piece == "top":
                top_shape(d, kind != "casual")
            elif piece == "pants":
                pants_shape(d)
            else:
                boots_shape(d)
            shade(base).save(os.path.join(out, name + ".png"))
            overlay(kind, piece).save(os.path.join(out, name + "_overlay.png"))
            write_item(name)
            names.append("dbtenkaichi:" + name)
    tag_dir = os.path.join(ROOT, "data", "minecraft", "tags", "item")
    os.makedirs(tag_dir, exist_ok=True)
    with open(os.path.join(tag_dir, "dyeable.json"), "w") as f:
        json.dump({"replace": False, "values": names}, f, indent=2)
    print("wrote", len(names), "outfit items")


if __name__ == "__main__":
    main()
