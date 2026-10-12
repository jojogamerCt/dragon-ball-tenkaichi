package com.dbtenkaichi.client.render;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.player.Appearance;
import com.dbtenkaichi.player.HairStyle;
import com.dbtenkaichi.race.Race;
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

    public static Identifier textureFor(Race race, Appearance a, int eyeColor, int hairColor) {
        long key = hash(race, a, eyeColor, hairColor);
        Identifier existing = CACHE.get(key);
        if (existing != null) {
            return existing;
        }
        NativeImage image = new SkinPainter(race, a, eyeColor, hairColor).paint();
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

    private static long hash(Race race, Appearance a, int eyeColor, int hairColor) {
        long h = 17 + race.ordinal();
        int[] values = {a.presentation, a.build, a.skinColor, a.eyeStyle, eyeColor, a.eyebrowStyle, a.hairStyle, hairColor, a.expression, a.outfit,
                a.outfitPrimary, a.outfitSecondary, a.featureColor};
        for (int v : values) {
            h = h * 1_000_003L + v;
        }
        return h;
    }

    static int shade(int rgb, float factor) {
        int r = Math.min(255, Math.max(0, Math.round((rgb >> 16 & 255) * factor)));
        int g = Math.min(255, Math.max(0, Math.round((rgb >> 8 & 255) * factor)));
        int b = Math.min(255, Math.max(0, Math.round((rgb & 255) * factor)));
        return r << 16 | g << 8 | b;
    }

    static int mix(int a, int b, float t) {
        int r = Math.round((a >> 16 & 255) + ((b >> 16 & 255) - (a >> 16 & 255)) * t);
        int g = Math.round((a >> 8 & 255) + ((b >> 8 & 255) - (a >> 8 & 255)) * t);
        int bl = Math.round((a & 255) + ((b & 255) - (a & 255)) * t);
        return r << 16 | g << 8 | bl;
    }

    static final class SkinPainter {
        private static final int S = 2;
        private final NativeImage img = new NativeImage(128, 128, true);
        private final Race race;
        private final Appearance a;
        private final int eyeColor;
        private final int hairColor;
        private final boolean bald;
        private final int skin;

        SkinPainter(Race race, Appearance a, int eyeColor, int hairColor) {
            this.race = race;
            this.a = a;
            this.eyeColor = eyeColor;
            this.hairColor = hairColor;
            this.bald = HairStyle.byOrdinal(a.hairStyle) == HairStyle.BALD || !race.hasHair();
            this.skin = a.skinColor;
        }

        NativeImage paint() {
            body(0, 0, 8, 8, 8, true);
            body(16, 16, 8, 12, 4, false);
            body(40, 16, 4, 12, 4, false);
            body(32, 48, 4, 12, 4, false);
            body(0, 16, 4, 12, 4, false);
            body(16, 48, 4, 12, 4, false);
            torsoAnatomy();
            limbAnatomy(40, 16);
            limbAnatomy(32, 48);
            scalp();
            face();
            ears();
            raceDetails();
            switch (Math.max(0, Math.min(2, a.outfit))) {
                case 0 -> gi();
                case 1 -> armor();
                default -> casual();
            }
            return img;
        }

        private void px(int x, int y, int rgb) {
            if (x >= 0 && y >= 0 && x < 128 && y < 128) {
                img.setPixel(x, y, 0xFF000000 | (rgb & 0xFFFFFF));
            }
        }

        private void clear(int x, int y) {
            if (x >= 0 && y >= 0 && x < 128 && y < 128) {
                img.setPixel(x, y, 0);
            }
        }

        private int noise(int x, int y) {
            int h = x * 374761393 + y * 668265263 + race.ordinal() * 2147483;
            h = (h ^ (h >>> 13)) * 1274126177;
            return (h ^ (h >>> 16)) & 255;
        }

        private void rect(int x, int y, int w, int h, int rgb, float top, float bottom, float grain) {
            for (int dy = 0; dy < h; dy++) {
                float t = h <= 1 ? 0 : (float) dy / (h - 1);
                float f = top + (bottom - top) * t;
                for (int dx = 0; dx < w; dx++) {
                    float n = 1.0f + (noise(x + dx, y + dy) / 255.0f - 0.5f) * grain;
                    px(x + dx, y + dy, shade(rgb, f * n));
                }
            }
        }

        private void hd(int u, int v, int w, int h, int rgb, float top, float bottom, float grain) {
            rect(u * S, v * S, w * S, h * S, rgb, top, bottom, grain);
        }

        private void body(int u, int v, int w, int h, int d, boolean head) {
            hd(u + d, v, w, d, skin, 1.08f, 1.0f, 0.05f);
            hd(u + d + w, v, w, d, skin, 0.72f, 0.66f, 0.05f);
            hd(u, v + d, d, h, skin, 0.92f, 0.8f, 0.05f);
            hd(u + d, v + d, w, h, skin, 1.04f, 0.9f, 0.05f);
            hd(u + d + w, v + d, d, h, skin, 0.92f, 0.8f, 0.05f);
            hd(u + d + w + d, v + d, w, h, skin, 0.86f, 0.76f, 0.05f);
            if (!head) {
                edgeShade((u + d) * S, (v + d) * S, w * S, h * S, 0.9f);
            }
        }

        private void edgeShade(int x, int y, int w, int h, float f) {
            for (int dy = 0; dy < h; dy++) {
                darken(x, y + dy, f);
                darken(x + w - 1, y + dy, f);
            }
        }

        private void darken(int x, int y, float f) {
            if (x < 0 || y < 0 || x >= 128 || y >= 128) {
                return;
            }
            int c = img.getPixel(x, y);
            if ((c >>> 24) == 0) {
                return;
            }
            px(x, y, shade(c & 0xFFFFFF, f));
        }

        private void torsoAnatomy() {
            int x = 20 * S;
            int y = 20 * S;
            float strength = a.build == 2 ? 0.82f : a.build == 1 ? 0.88f : 0.94f;
            for (int dx = 1; dx < 15; dx++) {
                darken(x + dx, y + 7 + (dx == 7 || dx == 8 ? 1 : 0), strength);
            }
            for (int dy = 8; dy < 21; dy++) {
                darken(x + 8, y + dy, strength);
            }
            if (a.build > 0) {
                for (int row = 11; row < 20; row += 3) {
                    for (int dx = 4; dx < 12; dx++) {
                        darken(x + dx, y + row, strength + 0.06f);
                    }
                }
            }
            for (int dx = 2; dx < 14; dx++) {
                darken(x + dx, y + 1, 1.06f);
            }
        }

        private void limbAnatomy(int u, int v) {
            int x = (u + 4) * S;
            int y = (v + 4) * S;
            for (int dy = 3; dy < 9; dy++) {
                for (int dx = 2; dx < 6; dx++) {
                    darken(x + dx, y + dy, 1.06f);
                }
            }
            for (int dx = 0; dx < 8; dx++) {
                darken(x + dx, y + 10, 0.9f);
            }
        }

        private void scalp() {
            if (bald) {
                hd(8, 0, 8, 8, skin, 1.12f, 1.02f, 0.04f);
                return;
            }
            int hair = hairColor;
            strands(8 * S, 0, 16, 16, hair, 1.0f);
            strands(24 * S, 8 * S, 16, 16, hair, 0.86f);
            strands(0, 8 * S, 16, 6, hair, 0.92f);
            strands(16 * S, 8 * S, 16, 6, hair, 0.92f);
            strands(0, 14 * S - 2, 4, 6, hair, 0.9f);
            strands(16 * S + 12, 14 * S - 2, 4, 6, hair, 0.9f);
            strands(8 * S, 8 * S, 16, 4, hair, 0.95f);
            for (int dx = 0; dx < 16; dx += 3) {
                int len = 2 + (noise(dx, 1) % 3);
                for (int dy = 0; dy < len; dy++) {
                    px(8 * S + dx, 8 * S + 4 + dy, shade(hair, 0.88f));
                }
            }
        }

        private void strands(int x, int y, int w, int h, int hair, float base) {
            for (int dx = 0; dx < w; dx++) {
                float strand = 0.82f + (noise(x + dx, 7) % 40) / 160.0f;
                for (int dy = 0; dy < h; dy++) {
                    float f = base * strand * (dy == 0 ? 1.15f : 1.0f) * (1.0f - dy * 0.012f);
                    if ((dx + dy / 3) % 5 == 0) {
                        f *= 0.85f;
                    }
                    px(x + dx, y + dy, shade(hair, f));
                }
            }
        }

        private void face() {
            int fx = 8 * S;
            int fy = 8 * S;
            int brow = bald ? shade(skin, 0.6f) : shade(hairColor, 0.9f);
            int browY = fy + 6;
            switch (a.eyebrowStyle) {
                case 1 -> {
                    line(fx + 2, browY - 1, fx + 6, browY + 1, brow);
                    line(fx + 9, browY + 1, fx + 13, browY - 1, brow);
                }
                case 2 -> {
                    line(fx + 2, browY, fx + 6, browY, brow);
                    line(fx + 2, browY + 1, fx + 6, browY + 1, brow);
                    line(fx + 9, browY, fx + 13, browY, brow);
                    line(fx + 9, browY + 1, fx + 13, browY + 1, brow);
                }
                case 3 -> {
                    line(fx + 3, browY, fx + 5, browY, brow);
                    line(fx + 10, browY, fx + 12, browY, brow);
                }
                default -> {
                    line(fx + 2, browY + 1, fx + 6, browY, brow);
                    line(fx + 9, browY, fx + 13, browY + 1, brow);
                }
            }
            eye(fx + 2, fy + 8, false);
            eye(fx + 9, fy + 8, true);
            int nose = shade(skin, 0.82f);
            px(fx + 7, fy + 10, shade(skin, 1.06f));
            px(fx + 8, fy + 10, shade(skin, 1.06f));
            px(fx + 7, fy + 11, nose);
            px(fx + 8, fy + 11, shade(skin, 0.75f));
            int mouth = shade(skin, 0.5f);
            int my = fy + 13;
            switch (a.expression) {
                case 1 -> line(fx + 6, my, fx + 9, my, mouth);
                case 2 -> {
                    for (int dx = 5; dx <= 10; dx++) {
                        px(fx + dx, my - 1, 0xF0F0F0);
                        px(fx + dx, my, 0x3A1414);
                    }
                    line(fx + 5, my + 1, fx + 10, my + 1, mouth);
                }
                case 3 -> {
                    px(fx + 5, my - 1, mouth);
                    line(fx + 6, my, fx + 9, my, mouth);
                    px(fx + 10, my - 1, mouth);
                }
                default -> line(fx + 6, my, fx + 9, my, mouth);
            }
            for (int dx = 2; dx < 14; dx++) {
                darken(fx + dx, fy + 15, 0.9f);
            }
            if (a.presentation == 1) {
                px(fx + 2, fy + 11, mix(skin, 0xFF8080, 0.25f));
                px(fx + 13, fy + 11, mix(skin, 0xFF8080, 0.25f));
                px(fx + 3, fy + 8, shade(skin, 0.4f));
                px(fx + 12, fy + 8, shade(skin, 0.4f));
            }
        }

        private void eye(int x, int y, boolean right) {
            int lid = shade(skin, 0.45f);
            switch (a.eyeStyle) {
                case 1 -> {
                    line(x, y + 1, x + 4, y + 1, lid);
                    px(x + (right ? 1 : 2), y + 2, eyeColor);
                    px(x + (right ? 2 : 3), y + 2, shade(eyeColor, 0.6f));
                    px(x + (right ? 0 : 1), y + 2, 0xF4F4F4);
                }
                case 2 -> {
                    line(x, y, x + 4, y, lid);
                    for (int dx = 0; dx < 5; dx++) {
                        px(x + dx, y + 1, dx == (right ? 1 : 3) || dx == 2 ? eyeColor : 0xF4F4F4);
                    }
                    px(x + 2, y + 2, shade(eyeColor, 0.5f));
                }
                case 3 -> {
                    line(x, y - 1, x + 4, y - 1, lid);
                    for (int dy = 0; dy < 3; dy++) {
                        for (int dx = 0; dx < 5; dx++) {
                            px(x + dx, y + dy, 0xF8F8F8);
                        }
                    }
                    for (int dy = 0; dy < 3; dy++) {
                        px(x + (right ? 1 : 2), y + dy, eyeColor);
                        px(x + (right ? 2 : 3), y + dy, shade(eyeColor, 0.7f));
                    }
                    px(x + (right ? 1 : 2), y, 0xFFFFFF);
                }
                default -> {
                    line(x, y, x + 4, y, lid);
                    for (int dx = 0; dx < 5; dx++) {
                        px(x + dx, y + 1, 0xF4F4F4);
                        px(x + dx, y + 2, 0xE6E6E6);
                    }
                    int ix = x + (right ? 1 : 2);
                    px(ix, y + 1, eyeColor);
                    px(ix + 1, y + 1, shade(eyeColor, 0.55f));
                    px(ix, y + 2, shade(eyeColor, 0.8f));
                    px(ix + 1, y + 2, eyeColor);
                    px(ix, y + 1, mix(eyeColor, 0xFFFFFF, 0.5f));
                }
            }
        }

        private void line(int x0, int y0, int x1, int y1, int rgb) {
            int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
            for (int i = 0; i <= steps; i++) {
                float t = steps == 0 ? 0 : (float) i / steps;
                px(Math.round(x0 + (x1 - x0) * t), Math.round(y0 + (y1 - y0) * t), rgb);
            }
        }

        private void ears() {
            int ear = shade(skin, 0.85f);
            for (int[] side : new int[][]{{0, 8}, {16, 8}}) {
                int x = side[0] * S + 6;
                int y = side[1] * S + 6;
                for (int dy = 0; dy < 5; dy++) {
                    px(x, y + dy, ear);
                    px(x + 1, y + dy, shade(ear, dy == 0 || dy == 4 ? 0.8f : 1.05f));
                    px(x + 2, y + dy, ear);
                }
                px(x + 1, y + 2, shade(ear, 0.7f));
            }
        }

        private void raceDetails() {
            int feature = a.featureColor;
            switch (race) {
                case NAMEKIAN -> {
                    for (int[] limb : new int[][]{{40, 16}, {32, 48}}) {
                        int x = limb[0] * S;
                        int y = (limb[1] + 4) * S;
                        for (int band = 0; band < 3; band++) {
                            int by = y + 3 + band * 5;
                            rect(x, by, 32, 2, feature, 1.0f, 0.9f, 0.08f);
                        }
                    }
                    for (int dx = 2; dx < 14; dx += 4) {
                        px(8 * S + dx, 8 * S + 3, shade(skin, 0.8f));
                    }
                }
                case FRIEZA_RACE -> {
                    rect(8 * S, 0, 16, 16, feature, 1.1f, 0.95f, 0.04f);
                    rect(24 * S, 8 * S, 16, 8, feature, 1.0f, 0.9f, 0.04f);
                    rect(20 * S + 2, 20 * S + 2, 12, 10, feature, 1.12f, 0.95f, 0.04f);
                    for (int[] limb : new int[][]{{44, 16}, {36, 48}}) {
                        rect(limb[0] * S, (limb[1] + 4) * S, 8, 4, feature, 1.1f, 1.0f, 0.04f);
                    }
                    for (int[] leg : new int[][]{{4, 16}, {20, 48}}) {
                        rect(leg[0] * S, (leg[1] + 4) * S + 12, 8, 6, feature, 1.1f, 0.95f, 0.04f);
                    }
                }
                case MAJIN -> {
                    for (int dx = 0; dx < 16; dx++) {
                        px(8 * S + dx, 8 * S + 1, shade(skin, 0.85f));
                    }
                    for (int dy = 12; dy < 22; dy += 3) {
                        for (int dx = 2; dx < 14; dx++) {
                            darken(20 * S + dx, 20 * S + dy, 0.93f);
                        }
                    }
                }
                case BIO_ANDROID -> {
                    for (int y = 0; y < 128; y++) {
                        for (int x = 0; x < 128; x++) {
                            int c = img.getPixel(x, y);
                            if ((c >>> 24) != 0 && noise(x / 3, y / 3) > 222) {
                                px(x, y, mix(c & 0xFFFFFF, feature, 0.75f));
                            }
                        }
                    }
                    rect(8 * S + 2, 8 * S, 12, 3, feature, 0.9f, 1.0f, 0.05f);
                }
                case ANDROID -> {
                    px(0 * S + 7, 8 * S + 7, feature);
                    px(16 * S + 8, 8 * S + 7, feature);
                    line(20 * S, 20 * S + 22, 28 * S - 1, 20 * S + 22, shade(skin, 0.85f));
                }
                case DEMON -> {
                    line(8 * S + 2, 8 * S + 11, 8 * S + 4, 8 * S + 13, feature);
                    line(8 * S + 13, 8 * S + 11, 8 * S + 11, 8 * S + 13, feature);
                }
                case KAIOSHIN -> {
                    px(0 * S + 7, 8 * S + 12, feature);
                    px(16 * S + 8, 8 * S + 12, feature);
                }
                default -> {
                }
            }
        }

        private void overlayClear() {
            for (int y = 0; y < 128; y++) {
                for (int x = 0; x < 128; x++) {
                    boolean overlay = (y >= 64 && y < 96 && x < 112) || (y >= 96 && (x < 32 || x >= 96)) || (y < 32 && x >= 64);
                    if (overlay) {
                        clear(x, y);
                    }
                }
            }
        }

        private void faceRows(int u, int v, int w, int h, int d, int from, int to, int rgb, float grain) {
            for (int row = from * S; row < to * S && row < h * S; row++) {
                float t = (float) row / (h * S);
                int y = (v + d) * S + row;
                rect(u * S, y, d * S, 1, rgb, 0.86f - t * 0.08f, 0.86f, grain);
                rect((u + d) * S, y, w * S, 1, rgb, 1.02f - t * 0.1f, 1.0f, grain);
                rect((u + d + w) * S, y, d * S, 1, rgb, 0.86f - t * 0.08f, 0.86f, grain);
                rect((u + d + w + d) * S, y, w * S, 1, rgb, 0.8f - t * 0.08f, 0.8f, grain);
            }
        }

        private void topBottom(int u, int v, int w, int d, int rgb) {
            hd(u + d, v, w, d, rgb, 1.05f, 1.0f, 0.05f);
            hd(u + d + w, v, w, d, rgb, 0.65f, 0.6f, 0.05f);
        }

        private void folds(int u, int v, int w, int h, int d, int rgb) {
            int x = (u + d) * S;
            int y = (v + d) * S;
            for (int i = 0; i < w * S; i += 5) {
                int fx = x + i + (noise(i, v) % 3);
                for (int dy = 2; dy < h * S - 2; dy += 1) {
                    if (noise(fx, y + dy) > 150) {
                        darken(fx, y + dy, 0.9f);
                    }
                }
            }
        }

        private void gi() {
            overlayClear();
            int main = a.outfitPrimary;
            int accent = a.outfitSecondary;
            faceRows(16, 16, 8, 12, 4, 0, 12, accent, 0.06f);
            faceRows(16, 32, 8, 12, 4, 0, 12, main, 0.08f);
            topBottom(16, 32, 8, 4, main);
            int cx = 20 * S;
            int cy = 36 * S;
            for (int dy = 0; dy < 10; dy++) {
                int half = Math.max(0, 5 - dy / 2);
                for (int dx = 8 - half; dx < 8 + half; dx++) {
                    clear(cx + dx, cy + dy);
                }
            }
            for (int dy = 0; dy < 10; dy++) {
                int half = Math.max(0, 5 - dy / 2);
                darken(cx + 8 - half - 1, cy + dy, 0.75f);
                darken(cx + 8 + half, cy + dy, 0.75f);
            }
            folds(16, 32, 8, 12, 4, main);
            faceRows(16, 32, 8, 12, 4, 9, 11, accent, 0.05f);
            for (int dy = 18; dy < 26; dy++) {
                px(cx + 10, cy + dy, shade(accent, 0.8f));
                px(cx + 11, cy + dy + 1, shade(accent, 0.7f));
            }
            emblem(cx + 3, cy + 4, accent);
            emblem((32 + 4) * S + 5, (32 + 4) * S + 4, accent);
            faceRows(0, 32, 4, 12, 4, 0, 8, main, 0.08f);
            faceRows(0, 48, 4, 12, 4, 0, 8, main, 0.08f);
            topBottom(0, 32, 4, 4, main);
            topBottom(0, 48, 4, 4, main);
            folds(0, 32, 4, 12, 4, main);
            folds(0, 48, 4, 12, 4, main);
            faceRows(0, 32, 4, 12, 4, 8, 12, accent, 0.06f);
            faceRows(0, 48, 4, 12, 4, 8, 12, accent, 0.06f);
            hd(8, 32, 4, 4, shade(accent, 0.55f), 1.0f, 0.9f, 0.05f);
            hd(8, 48, 4, 4, shade(accent, 0.55f), 1.0f, 0.9f, 0.05f);
            faceRows(0, 16, 4, 12, 4, 0, 12, main, 0.08f);
            faceRows(16, 48, 4, 12, 4, 0, 12, main, 0.08f);
            faceRows(0, 16, 4, 12, 4, 8, 12, accent, 0.06f);
            faceRows(16, 48, 4, 12, 4, 8, 12, accent, 0.06f);
            faceRows(40, 32, 4, 12, 4, 7, 10, accent, 0.05f);
            faceRows(48, 48, 4, 12, 4, 7, 10, accent, 0.05f);
            faceRows(40, 16, 4, 12, 4, 7, 10, accent, 0.05f);
            faceRows(32, 48, 4, 12, 4, 7, 10, accent, 0.05f);
            faceRows(40, 16, 4, 12, 4, 0, 4, main, 0.08f);
            faceRows(32, 48, 4, 12, 4, 0, 4, main, 0.08f);
            faceRows(40, 32, 4, 12, 4, 0, 4, main, 0.08f);
            faceRows(48, 48, 4, 12, 4, 0, 4, main, 0.08f);
            faceRows(40, 32, 4, 12, 4, 3, 4, shade(main, 0.75f), 0.04f);
            faceRows(48, 48, 4, 12, 4, 3, 4, shade(main, 0.75f), 0.04f);
            coverTops(main, main);
        }

        private void coverTops(int shirt, int pants) {
            hd(44, 16, 4, 4, shirt, 1.05f, 0.95f, 0.05f);
            hd(36, 48, 4, 4, shirt, 1.05f, 0.95f, 0.05f);
            hd(44, 32, 4, 4, shirt, 1.08f, 1.0f, 0.05f);
            hd(52, 48, 4, 4, shirt, 1.08f, 1.0f, 0.05f);
            hd(4, 16, 4, 4, pants, 0.8f, 0.75f, 0.05f);
            hd(20, 48, 4, 4, pants, 0.8f, 0.75f, 0.05f);
            hd(4, 32, 4, 4, pants, 0.85f, 0.8f, 0.05f);
            hd(4, 48, 4, 4, pants, 0.85f, 0.8f, 0.05f);
            hd(28, 16, 8, 4, pants, 0.7f, 0.65f, 0.05f);
            hd(28, 32, 8, 4, pants, 0.72f, 0.66f, 0.05f);
        }

        private void emblem(int x, int y, int color) {
            int bg = 0xF2F2F2;
            for (int dy = -3; dy <= 3; dy++) {
                for (int dx = -3; dx <= 3; dx++) {
                    if (dx * dx + dy * dy <= 10) {
                        px(x + dx, y + dy, bg);
                    }
                }
            }
            line(x, y - 2, x, y + 2, color);
            line(x - 2, y, x + 2, y, color);
            px(x - 1, y - 1, color);
            px(x + 1, y - 1, color);
            px(x - 1, y + 1, color);
            px(x + 1, y + 1, color);
        }

        private void armor() {
            overlayClear();
            int suit = a.outfitPrimary;
            int plate = 0xEDE7D6;
            int trim = a.outfitSecondary;
            faceRows(16, 16, 8, 12, 4, 0, 12, suit, 0.04f);
            faceRows(40, 16, 4, 12, 4, 0, 12, suit, 0.04f);
            faceRows(32, 48, 4, 12, 4, 0, 12, suit, 0.04f);
            faceRows(0, 16, 4, 12, 4, 0, 12, suit, 0.04f);
            faceRows(16, 48, 4, 12, 4, 0, 12, suit, 0.04f);
            for (int[] seam : new int[][]{{40, 16}, {32, 48}, {0, 16}, {16, 48}}) {
                for (int dy = 0; dy < 24; dy++) {
                    darken((seam[0] + 4) * S + 3, (seam[1] + 4) * S + dy, 0.8f);
                }
            }
            faceRows(16, 32, 8, 12, 4, 0, 8, plate, 0.03f);
            topBottom(16, 32, 8, 4, plate);
            faceRows(16, 32, 8, 12, 4, 7, 9, trim, 0.03f);
            int cx = 20 * S;
            int cy = 36 * S;
            for (int dx = 0; dx < 16; dx++) {
                px(cx + dx, cy, shade(plate, 1.1f));
                darken(cx + dx, cy + 13, 0.8f);
            }
            for (int dy = 1; dy < 13; dy++) {
                px(cx + 7, cy + dy, shade(plate, 0.92f));
                px(cx + 8, cy + dy, shade(plate, 1.08f));
            }
            rect(cx + 1, cy + 1, 3, 5, trim, 1.0f, 0.9f, 0.03f);
            rect(cx + 12, cy + 1, 3, 5, trim, 1.0f, 0.9f, 0.03f);
            faceRows(40, 32, 4, 12, 4, 0, 2, trim, 0.03f);
            faceRows(48, 48, 4, 12, 4, 0, 2, trim, 0.03f);
            topBottom(40, 32, 4, 4, plate);
            topBottom(48, 48, 4, 4, plate);
            faceRows(40, 32, 4, 12, 4, 8, 12, plate, 0.03f);
            faceRows(48, 48, 4, 12, 4, 8, 12, plate, 0.03f);
            faceRows(0, 32, 4, 12, 4, 6, 12, plate, 0.03f);
            faceRows(0, 48, 4, 12, 4, 6, 12, plate, 0.03f);
            faceRows(0, 32, 4, 12, 4, 6, 7, trim, 0.03f);
            faceRows(0, 48, 4, 12, 4, 6, 7, trim, 0.03f);
            hd(8, 32, 4, 4, shade(plate, 0.6f), 1.0f, 0.9f, 0.03f);
            hd(8, 48, 4, 4, shade(plate, 0.6f), 1.0f, 0.9f, 0.03f);
            coverTops(suit, suit);
            hd(44, 32, 4, 4, plate, 1.1f, 1.0f, 0.03f);
            hd(52, 48, 4, 4, plate, 1.1f, 1.0f, 0.03f);
        }

        private void casual() {
            overlayClear();
            int shirt = a.outfitPrimary;
            int pants = a.outfitSecondary;
            faceRows(16, 16, 8, 12, 4, 0, 12, shirt, 0.05f);
            faceRows(40, 16, 4, 12, 4, 0, 4, shirt, 0.05f);
            faceRows(32, 48, 4, 12, 4, 0, 4, shirt, 0.05f);
            faceRows(0, 16, 4, 12, 4, 0, 11, pants, 0.12f);
            faceRows(16, 48, 4, 12, 4, 0, 11, pants, 0.12f);
            faceRows(0, 16, 4, 12, 4, 11, 12, 0x2A2A2A, 0.03f);
            faceRows(16, 48, 4, 12, 4, 11, 12, 0x2A2A2A, 0.03f);
            hd(8, 16, 4, 4, 0x2A2A2A, 1.0f, 0.9f, 0.03f);
            hd(8, 48, 4, 4, 0x2A2A2A, 1.0f, 0.9f, 0.03f);
            int jacket = shade(pants, 0.7f);
            faceRows(16, 32, 8, 12, 4, 0, 12, jacket, 0.06f);
            topBottom(16, 32, 8, 4, jacket);
            int cx = 20 * S;
            int cy = 36 * S;
            for (int dy = 0; dy < 24; dy++) {
                for (int dx = 5; dx < 11; dx++) {
                    clear(cx + dx, cy + dy);
                }
                darken(cx + 4, cy + dy, 0.75f);
                darken(cx + 11, cy + dy, 0.75f);
            }
            faceRows(40, 32, 4, 12, 4, 0, 6, jacket, 0.06f);
            faceRows(48, 48, 4, 12, 4, 0, 6, jacket, 0.06f);
            folds(0, 16, 4, 12, 4, pants);
            folds(16, 48, 4, 12, 4, pants);
            coverTops(shirt, pants);
            hd(44, 32, 4, 4, jacket, 1.05f, 0.95f, 0.05f);
            hd(52, 48, 4, 4, jacket, 1.05f, 0.95f, 0.05f);
        }
    }
}
