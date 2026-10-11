package com.dbtenkaichi.client.render;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.player.Appearance;
import com.dbtenkaichi.player.HairStyle;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public final class CustomSkins {
    private static final int MAX_CACHED = 48;
    private static final Map<Long, Identifier> CACHE = new LinkedHashMap<>(16, 0.75f, true);
    private static int counter;

    private CustomSkins() {
    }

    public static Identifier textureFor(Appearance a, int eyeColor, int hairColor) {
        long key = hash(a, eyeColor, hairColor);
        Identifier existing = CACHE.get(key);
        if (existing != null) {
            return existing;
        }
        NativeImage image = compose(a, eyeColor, hairColor);
        Identifier id = Tenkaichi.id("dynamic/skin_" + (counter++));
        Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(id::toString, image));
        CACHE.put(key, id);
        if (CACHE.size() > MAX_CACHED) {
            Map.Entry<Long, Identifier> eldest = CACHE.entrySet().iterator().next();
            CACHE.remove(eldest.getKey());
            Minecraft.getInstance().getTextureManager().release(eldest.getValue());
        }
        return id;
    }

    public static void clear() {
        for (Identifier id : CACHE.values()) {
            Minecraft.getInstance().getTextureManager().release(id);
        }
        CACHE.clear();
    }

    private static long hash(Appearance a, int eyeColor, int hairColor) {
        long h = 17;
        int[] values = {a.presentation, a.build, a.skinColor, a.eyeStyle, eyeColor, a.eyebrowStyle, a.hairStyle, hairColor, a.expression, a.outfit, a.outfitPrimary, a.outfitSecondary};
        for (int v : values) {
            h = h * 1_000_003L + v;
        }
        return h;
    }

    static NativeImage compose(Appearance a, int eyeColor, int hairColor) {
        NativeImage img = new NativeImage(64, 64, true);
        int skin = a.skinColor;
        boolean bald = HairStyle.byOrdinal(a.hairStyle) == HairStyle.BALD;
        int scalp = bald ? skin : hairColor;
        box(img, 0, 0, 8, 8, 8, skin, skin);
        paintScalp(img, scalp, skin, bald);
        drawFace(img, a, eyeColor, bald ? shade(skin, 0.75f) : hairColor);
        box(img, 16, 16, 8, 12, 4, skin, skin);
        box(img, 40, 16, 4, 12, 4, skin, skin);
        box(img, 32, 48, 4, 12, 4, skin, skin);
        box(img, 0, 16, 4, 12, 4, skin, skin);
        box(img, 16, 48, 4, 12, 4, skin, skin);
        switch (Math.max(0, Math.min(2, a.outfit))) {
            case 0 -> gi(img, a);
            case 1 -> armor(img, a);
            default -> casual(img, a);
        }
        return img;
    }

    private static void box(NativeImage img, int u, int v, int w, int h, int d, int side, int top) {
        fill(img, u + d, v, w, d, shade(top, 1.08f));
        fill(img, u + d + w, v, w, d, shade(top, 0.7f));
        fill(img, u, v + d, d, h, shade(side, 0.86f));
        fill(img, u + d, v + d, w, h, side);
        fill(img, u + d + w, v + d, d, h, shade(side, 0.86f));
        fill(img, u + d + w + d, v + d, w, h, shade(side, 0.8f));
    }

    private static void boxRows(NativeImage img, int u, int v, int w, int h, int d, int fromRow, int toRow, int color) {
        for (int row = fromRow; row < toRow && row < h; row++) {
            int y = v + d + row;
            fill(img, u, y, d, 1, shade(color, 0.86f));
            fill(img, u + d, y, w, 1, color);
            fill(img, u + d + w, y, d, 1, shade(color, 0.86f));
            fill(img, u + d + w + d, y, w, 1, shade(color, 0.8f));
        }
    }

    private static void paintScalp(NativeImage img, int hair, int skin, boolean bald) {
        if (bald) {
            fill(img, 8, 0, 8, 8, shade(skin, 1.05f));
            return;
        }
        fill(img, 8, 0, 8, 8, hair);
        fill(img, 24, 8, 8, 8, shade(hair, 0.85f));
        fill(img, 0, 8, 8, 3, shade(hair, 0.9f));
        fill(img, 16, 8, 8, 3, shade(hair, 0.9f));
        fill(img, 0, 11, 2, 3, shade(hair, 0.9f));
        fill(img, 22, 11, 2, 3, shade(hair, 0.9f));
        fill(img, 8, 8, 8, 2, hair);
    }

    private static void drawFace(NativeImage img, Appearance a, int eyeColor, int browColor) {
        int skin = a.skinColor;
        int ox = 8;
        int oy = 8;
        int sclera = 0xF4F4F4;
        int iris = eyeColor;
        int lash = shade(skin, 0.35f);
        int browRow = oy + 3;
        switch (a.eyebrowStyle) {
            case 1 -> {
                px(img, ox + 1, browRow, browColor);
                px(img, ox + 2, browRow + 1, browColor);
                px(img, ox + 5, browRow + 1, browColor);
                px(img, ox + 6, browRow, browColor);
            }
            case 2 -> {
                fill(img, ox + 1, browRow, 3, 1, browColor);
                fill(img, ox + 4, browRow, 3, 1, browColor);
            }
            case 3 -> {
                px(img, ox + 2, browRow, browColor);
                px(img, ox + 5, browRow, browColor);
            }
            default -> {
                fill(img, ox + 1, browRow, 2, 1, browColor);
                fill(img, ox + 5, browRow, 2, 1, browColor);
            }
        }
        int eyeRow = oy + 4;
        switch (a.eyeStyle) {
            case 1 -> {
                fill(img, ox + 1, eyeRow, 2, 1, lash);
                fill(img, ox + 5, eyeRow, 2, 1, lash);
                px(img, ox + 2, eyeRow + 1, iris);
                px(img, ox + 5, eyeRow + 1, iris);
                px(img, ox + 1, eyeRow + 1, sclera);
                px(img, ox + 6, eyeRow + 1, sclera);
            }
            case 2 -> {
                px(img, ox + 1, eyeRow, sclera);
                px(img, ox + 2, eyeRow, iris);
                px(img, ox + 5, eyeRow, iris);
                px(img, ox + 6, eyeRow, sclera);
                px(img, ox + 2, eyeRow + 1, shade(iris, 0.6f));
                px(img, ox + 5, eyeRow + 1, shade(iris, 0.6f));
            }
            case 3 -> {
                fill(img, ox + 1, eyeRow, 2, 2, sclera);
                fill(img, ox + 5, eyeRow, 2, 2, sclera);
                fill(img, ox + 2, eyeRow, 1, 2, iris);
                fill(img, ox + 5, eyeRow, 1, 2, iris);
                fill(img, ox + 1, eyeRow - 1, 2, 1, lash);
                fill(img, ox + 5, eyeRow - 1, 2, 1, lash);
            }
            default -> {
                fill(img, ox + 1, eyeRow, 2, 2, sclera);
                fill(img, ox + 5, eyeRow, 2, 2, sclera);
                fill(img, ox + 2, eyeRow, 1, 2, iris);
                fill(img, ox + 5, eyeRow, 1, 2, iris);
            }
        }
        px(img, ox + 3, oy + 5, shade(skin, 0.88f));
        px(img, ox + 4, oy + 5, shade(skin, 0.88f));
        int mouth = shade(skin, 0.55f);
        int mouthRow = oy + 6;
        switch (a.expression) {
            case 1 -> fill(img, ox + 3, mouthRow + 1, 2, 1, mouth);
            case 2 -> {
                fill(img, ox + 3, mouthRow, 2, 2, 0x3A1414);
                fill(img, ox + 3, mouthRow, 2, 1, 0xF0F0F0);
            }
            case 3 -> {
                px(img, ox + 2, mouthRow, mouth);
                fill(img, ox + 3, mouthRow + 1, 2, 1, mouth);
                px(img, ox + 5, mouthRow, mouth);
            }
            default -> fill(img, ox + 3, mouthRow, 2, 1, mouth);
        }
        if (a.presentation == 1) {
            px(img, ox + 1, oy + 6, shade(skin, 0.92f) | 0);
            px(img, ox + 6, oy + 6, shade(skin, 0.92f));
        }
    }

    private static void gi(NativeImage img, Appearance a) {
        int main = a.outfitPrimary;
        int accent = a.outfitSecondary;
        boxRows(img, 16, 16, 8, 12, 4, 0, 12, main);
        fill(img, 20, 16, 8, 4, shade(main, 1.05f));
        fill(img, 23, 20, 2, 4, accent);
        px(img, 22, 20, accent);
        px(img, 25, 20, accent);
        boxRows(img, 16, 16, 8, 12, 4, 9, 11, accent);
        boxRows(img, 0, 16, 4, 12, 4, 0, 9, main);
        boxRows(img, 16, 48, 4, 12, 4, 0, 9, main);
        boxRows(img, 0, 16, 4, 12, 4, 9, 12, accent);
        boxRows(img, 16, 48, 4, 12, 4, 9, 12, accent);
        fill(img, 8, 16, 4, 4, shade(accent, 0.6f));
        fill(img, 24, 48, 4, 4, shade(accent, 0.6f));
        boxRows(img, 40, 16, 4, 12, 4, 0, 3, main);
        boxRows(img, 32, 48, 4, 12, 4, 0, 3, main);
        boxRows(img, 40, 16, 4, 12, 4, 8, 10, accent);
        boxRows(img, 32, 48, 4, 12, 4, 8, 10, accent);
    }

    private static void armor(NativeImage img, Appearance a) {
        int suit = a.outfitPrimary;
        int plate = 0xEDE7D6;
        int trim = a.outfitSecondary;
        boxRows(img, 16, 16, 8, 12, 4, 0, 12, suit);
        boxRows(img, 16, 16, 8, 12, 4, 0, 8, plate);
        fill(img, 20, 16, 8, 4, shade(trim, 1.0f));
        boxRows(img, 16, 16, 8, 12, 4, 8, 9, trim);
        boxRows(img, 40, 16, 4, 12, 4, 0, 12, suit);
        boxRows(img, 32, 48, 4, 12, 4, 0, 12, suit);
        boxRows(img, 40, 16, 4, 12, 4, 0, 2, trim);
        boxRows(img, 32, 48, 4, 12, 4, 0, 2, trim);
        fill(img, 44, 16, 4, 4, trim);
        fill(img, 36, 48, 4, 4, trim);
        boxRows(img, 40, 16, 4, 12, 4, 8, 12, plate);
        boxRows(img, 32, 48, 4, 12, 4, 8, 12, plate);
        boxRows(img, 0, 16, 4, 12, 4, 0, 12, suit);
        boxRows(img, 16, 48, 4, 12, 4, 0, 12, suit);
        boxRows(img, 0, 16, 4, 12, 4, 7, 12, plate);
        boxRows(img, 16, 48, 4, 12, 4, 7, 12, plate);
        boxRows(img, 0, 16, 4, 12, 4, 7, 8, trim);
        boxRows(img, 16, 48, 4, 12, 4, 7, 8, trim);
    }

    private static void casual(NativeImage img, Appearance a) {
        int shirt = a.outfitPrimary;
        int pants = a.outfitSecondary;
        boxRows(img, 16, 16, 8, 12, 4, 0, 12, shirt);
        boxRows(img, 40, 16, 4, 12, 4, 0, 5, shirt);
        boxRows(img, 32, 48, 4, 12, 4, 0, 5, shirt);
        boxRows(img, 0, 16, 4, 12, 4, 0, 11, pants);
        boxRows(img, 16, 48, 4, 12, 4, 0, 11, pants);
        boxRows(img, 0, 16, 4, 12, 4, 11, 12, 0x2A2A2A);
        boxRows(img, 16, 48, 4, 12, 4, 11, 12, 0x2A2A2A);
        fill(img, 8, 16, 4, 4, 0x2A2A2A);
        fill(img, 24, 48, 4, 4, 0x2A2A2A);
    }

    private static void fill(NativeImage img, int x, int y, int w, int h, int rgb) {
        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                px(img, x + dx, y + dy, rgb);
            }
        }
    }

    private static void px(NativeImage img, int x, int y, int rgb) {
        if (x >= 0 && y >= 0 && x < 64 && y < 64) {
            img.setPixel(x, y, 0xFF000000 | (rgb & 0xFFFFFF));
        }
    }

    static int shade(int rgb, float factor) {
        int r = Math.min(255, Math.round((rgb >> 16 & 255) * factor));
        int g = Math.min(255, Math.round((rgb >> 8 & 255) * factor));
        int b = Math.min(255, Math.round((rgb & 255) * factor));
        return r << 16 | g << 8 | b;
    }
}
