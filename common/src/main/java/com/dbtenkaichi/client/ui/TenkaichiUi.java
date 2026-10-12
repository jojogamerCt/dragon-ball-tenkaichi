package com.dbtenkaichi.client.ui;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.client.ClientState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class TenkaichiUi {
    public static final Identifier PANEL = Tenkaichi.id("panel");
    public static final Identifier BUTTON = Tenkaichi.id("button");
    public static final Identifier BUTTON_HOVER = Tenkaichi.id("button_hover");
    public static final Identifier BUTTON_DISABLED = Tenkaichi.id("button_disabled");
    public static final Identifier SLOT = Tenkaichi.id("slot");
    public static final Identifier SLOT_SELECTED = Tenkaichi.id("slot_selected");
    public static final Identifier PORTRAIT_FRAME = Tenkaichi.id("portrait_frame");

    public static final int GOLD = 0xFFFFC440;
    public static final int GOLD_LIGHT = 0xFFFFF0B0;
    public static final int TEXT = 0xFFF2F2F2;
    public static final int MUTED = 0xFFB4B8CC;
    public static final int DANGER = 0xFFFF6A6A;

    private TenkaichiUi() {
    }

    public static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.blitSprite(RenderPipelines.GUI_TEXTURED, PANEL, x, y, w, h);
    }

    public static void slot(GuiGraphicsExtractor g, int x, int y, int size, boolean selected) {
        g.blitSprite(RenderPipelines.GUI_TEXTURED, selected ? SLOT_SELECTED : SLOT, x, y, size, size);
    }

    public static void sprite(GuiGraphicsExtractor g, Identifier sprite, int x, int y, int w, int h) {
        g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, w, h);
    }

    public static void sprite(GuiGraphicsExtractor g, Identifier sprite, int x, int y, int w, int h, int argb) {
        g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, w, h, argb);
    }

    public static Identifier skillIcon(String techniqueId) {
        int colon = techniqueId.indexOf(':');
        String path = colon >= 0 ? techniqueId.substring(colon + 1) : techniqueId;
        return Tenkaichi.id("skill/" + path);
    }

    public static void background(GuiGraphicsExtractor g, int width, int height) {
        g.fillGradient(0, 0, width, height, 0xE8050814, 0xF0141C34);
        float t = (ClientState.clientTicks % 200) / 200.0f;
        for (int i = 0; i < 14; i++) {
            int y = (int) (((i * 37 + t * height * 0.6f) % height));
            int len = 40 + (i * 53) % 120;
            int x = (int) ((i * 97 + t * width * 2.0f) % (width + len)) - len;
            g.fill(x, y, x + len, y + 1, 0x14FFD070);
        }
        g.fillGradient(0, 0, width, 24, 0x60000000, 0x00000000);
        g.fillGradient(0, height - 24, width, height, 0x00000000, 0x60000000);
    }

    public static void header(GuiGraphicsExtractor g, Font font, Component title, int cx, int y) {
        int w = font.width(title) + 36;
        int x0 = cx - w / 2;
        g.fillGradient(x0, y - 4, x0 + w, y + 12, 0xF0C08420, 0xF0704410);
        g.fill(x0 - 4, y - 4, x0, y + 12, 0x80C08420);
        g.fill(x0 + w, y - 4, x0 + w + 4, y + 12, 0x80C08420);
        g.fill(x0, y - 5, x0 + w, y - 4, GOLD_LIGHT);
        g.fill(x0, y + 12, x0 + w, y + 13, 0xFF3A2208);
        g.centeredText(font, title, cx, y, 0xFFFFF8E0);
    }

    public static void divider(GuiGraphicsExtractor g, int x, int y, int w) {
        g.fillGradient(x, y, x + w / 2, y + 1, 0x00FFC440, 0xC0FFC440);
        g.fill(x + w / 2, y, x + w, y + 1, 0x80FFC440);
    }

    public static int mix(int a, int b, float t) {
        int aa = a >>> 24;
        int ar = a >> 16 & 255;
        int ag = a >> 8 & 255;
        int ab = a & 255;
        int ba = b >>> 24;
        int br = b >> 16 & 255;
        int bg = b >> 8 & 255;
        int bb = b & 255;
        return Math.round(aa + (ba - aa) * t) << 24 | Math.round(ar + (br - ar) * t) << 16 | Math.round(ag + (bg - ag) * t) << 8 | Math.round(ab + (bb - ab) * t);
    }

    public static int darker(int argb, float f) {
        int r = Math.round((argb >> 16 & 255) * f);
        int gr = Math.round((argb >> 8 & 255) * f);
        int b = Math.round((argb & 255) * f);
        return argb & 0xFF000000 | r << 16 | gr << 8 | b;
    }

    public static void gauge(GuiGraphicsExtractor g, int x, int y, int w, int h, float ratio, float trail, int color) {
        float filled = clamp(ratio);
        float ghost = clamp(trail);
        int slant = h / 2;
        g.fill(x + slant - 1, y - 1, x + w + 1, y, 0xFF000000);
        g.fill(x - 1, y + h, x + w - slant + 1, y + h + 1, 0xFF000000);
        int top = mix(color, 0xFFFFFFFF, 0.35f);
        int bottom = darker(color, 0.55f);
        for (int row = 0; row < h; row++) {
            int off = (h - 1 - row) * slant / Math.max(1, h - 1);
            int x0 = x + off;
            int x1 = x0 + w - slant;
            int span = x1 - x0;
            g.fill(x0 - 1, y + row, x0, y + row + 1, 0xFF000000);
            g.fill(x1, y + row, x1 + 1, y + row + 1, 0xFF000000);
            g.fill(x0, y + row, x1, y + row + 1, 0xC0141824);
            int ghostEnd = x0 + Math.round(span * ghost);
            if (ghostEnd > x0) {
                g.fill(x0, y + row, ghostEnd, y + row + 1, 0x90FFFFFF);
            }
            int end = x0 + Math.round(span * filled);
            if (end > x0) {
                float t = h <= 1 ? 0.0f : (float) row / (h - 1);
                int c = row == 0 ? mix(color, 0xFFFFFFFF, 0.6f) : mix(top, bottom, t);
                g.fill(x0, y + row, end, y + row + 1, c);
                if (end - x0 > 2) {
                    g.fill(end - 1, y + row, end, y + row + 1, mix(c, 0xFFFFFFFF, 0.5f));
                }
            }
        }
        for (int i = 1; i < 4; i++) {
            int tx = x + (w - slant) * i / 4 + slant / 2;
            g.fill(tx, y + 1, tx + 1, y + h - 1, 0x40000000);
        }
    }

    private static float clamp(float v) {
        return Float.isNaN(v) ? 0.0f : Math.max(0.0f, Math.min(1.0f, v));
    }

    public static void label(GuiGraphicsExtractor g, Font font, Component text, int x, int y, int color) {
        g.text(font, text, x, y, color, true);
    }
}
