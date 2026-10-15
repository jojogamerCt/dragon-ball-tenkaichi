package com.dbtenkaichi.client.hud;

import com.dbtenkaichi.client.ClientState;
import com.dbtenkaichi.client.ui.TenkaichiUi;
import com.dbtenkaichi.race.KiSense;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SenseHud {
    private record Entry(int id, Vec3 pos, long bp, float ratio, int kind, String name) {
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static boolean scouter;
    private static boolean sensing;
    private static long updated;

    private SenseHud() {
    }

    public static void update(CompoundTag tag) {
        ENTRIES.clear();
        scouter = tag.getBooleanOr("scouter", false);
        sensing = tag.getBooleanOr("sensing", false);
        updated = ClientState.clientTicks;
        ListTag list = tag.getListOrEmpty("entries");
        for (Tag element : list) {
            if (element instanceof CompoundTag e) {
                ENTRIES.add(new Entry(e.getIntOr("id", -1), new Vec3(e.getDoubleOr("x", 0), e.getDoubleOr("y", 0), e.getDoubleOr("z", 0)),
                        e.getLongOr("bp", 0), e.getFloatOr("ratio", 1.0f), e.getIntOr("kind", 0), e.getStringOr("name", "")));
            }
        }
    }

    public static boolean active() {
        return (sensing || scouter) && ClientState.clientTicks - updated < 40;
    }

    private static int color(int kind) {
        return switch (kind) {
            case KiSense.KIND_ENEMY -> 0xFFFF5A4A;
            case KiSense.KIND_PLAYER -> 0xFFFFD45A;
            case KiSense.KIND_CREATURE -> 0xFFC88AFF;
            default -> 0xFF6AE8A0;
        };
    }

    public static void render(GuiGraphicsExtractor g, Minecraft mc) {
        if (!active() || mc.player == null || mc.level == null) {
            return;
        }
        Font font = mc.font;
        NumberFormat fmt = NumberFormat.getIntegerInstance(Locale.ROOT);
        float pulse = 0.6f + 0.4f * (float) Math.sin(ClientState.clientTicks * 0.2);
        for (Entry entry : ENTRIES) {
            Entity live = mc.level.getEntity(entry.id());
            Vec3 pos = live != null ? live.position().add(0, live.getBbHeight() * 0.6, 0) : entry.pos();
            WorldProjection.Point p = WorldProjection.project(pos, g.guiWidth(), g.guiHeight());
            int color = color(entry.kind());
            int x = Math.round(p.x());
            int y = Math.round(p.y());
            double distance = pos.distanceTo(mc.player.position());
            int size = Math.max(2, Math.min(6, (int) Math.round(2 + Math.log10(Math.max(1.0, entry.ratio() * 10.0)) * 2)));
            if (!p.onScreen()) {
                g.fill(x - 2, y - 2, x + 2, y + 2, color);
                continue;
            }
            int alpha = Math.round(140 + 115 * pulse);
            int c = alpha << 24 | (color & 0xFFFFFF);
            g.fill(x - size - 1, y - 1, x + size + 1, y + 1, c);
            g.fill(x - 1, y - size - 1, x + 1, y + size + 1, c);
            g.fill(x - size, y - size, x - size + 2, y - size + 2, c);
            g.fill(x + size - 2, y + size - 2, x + size, y + size, c);
            String text;
            if (scouter) {
                text = fmt.format(entry.bp());
            } else {
                text = strength(entry.ratio());
            }
            int tw = font.width(text);
            g.text(font, text, x - tw / 2, y + size + 3, scouter ? 0xFF7CFF7C : color, true);
            if (distance < 48 && !entry.name().isEmpty()) {
                g.text(font, entry.name(), x - font.width(entry.name()) / 2, y - size - 11, 0xFFE8E8E8, true);
            }
        }
        if (scouter) {
            int w = g.guiWidth();
            int h = g.guiHeight();
            g.fill(w - 70, h / 2 - 40, w - 6, h / 2 + 40, 0x2830FF60);
            g.fill(w - 70, h / 2 - 40, w - 69, h / 2 + 40, 0x8030FF60);
            for (int i = 0; i < 6; i++) {
                int yy = h / 2 - 34 + i * 12 + (int) (ClientState.clientTicks % 12);
                g.fill(w - 66, yy, w - 10, yy + 1, 0x4030FF60);
            }
            g.text(font, Component.translatable("hud.dbtenkaichi.scouter"), w - 66, h / 2 - 38, 0xFF7CFF7C, true);
        } else if (sensing) {
            g.text(font, Component.translatable("hud.dbtenkaichi.sensing", ENTRIES.size()), g.guiWidth() / 2 - 40, 6, TenkaichiUi.GOLD_LIGHT, true);
        }
    }

    private static String strength(float ratio) {
        if (ratio >= 4.0f) {
            return "▲▲▲";
        }
        if (ratio >= 1.5f) {
            return "▲▲";
        }
        if (ratio >= 0.75f) {
            return "▲";
        }
        if (ratio >= 0.3f) {
            return "•";
        }
        return "·";
    }
}
