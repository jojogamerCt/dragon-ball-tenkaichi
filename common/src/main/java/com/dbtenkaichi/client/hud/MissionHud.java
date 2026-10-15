package com.dbtenkaichi.client.hud;

import com.dbtenkaichi.client.ClientState;
import com.dbtenkaichi.client.ui.TenkaichiUi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class MissionHud {
    private static CompoundTag data = new CompoundTag();
    private static long updated;

    private MissionHud() {
    }

    public static void update(CompoundTag tag) {
        data = tag;
        updated = ClientState.clientTicks;
    }

    public static CompoundTag data() {
        return data;
    }

    public static boolean hasTracked() {
        return data.contains("tracked");
    }

    public static void render(GuiGraphicsExtractor g, Minecraft mc) {
        if (!hasTracked() || mc.player == null) {
            return;
        }
        Font font = mc.font;
        boolean story = data.getBooleanOr("story", false);
        Component label = Component.translatable(story ? "hud.dbtenkaichi.mission_story" : "hud.dbtenkaichi.mission_side");
        Component title = Component.translatable(data.getStringOr("title", ""));
        Component objective = Component.translatable(data.getStringOr("objective", ""));
        int count = data.getIntOr("count", 1);
        int progress = data.getIntOr("progress", 0);
        if (count > 1) {
            objective = objective.copy().append(" (" + Math.min(progress, count) + "/" + count + ")");
        }
        int width = 168;
        int x = g.guiWidth() - width - 6;
        int y = 46;
        List<FormattedCharSequence> titleLines = font.split(title, width - 16);
        List<FormattedCharSequence> objLines = font.split(objective, width - 22);
        int height = 20 + titleLines.size() * 10 + objLines.size() * 10 + 14;
        float appear = Math.min(1.0f, (ClientState.clientTicks - updated + 1) / 6.0f);
        if (appear < 1.0f) {
            x += Math.round((1.0f - appear) * 40);
        }
        TenkaichiUi.panel(g, x, y, width, height);
        g.fillGradient(x + 3, y + 3, x + width - 3, y + 14, story ? 0xC0C08420 : 0xC0306090, 0x40000000);
        g.text(font, label, x + 8, y + 5, story ? TenkaichiUi.GOLD_LIGHT : 0xFFBFE6FF, true);
        int ty = y + 18;
        for (FormattedCharSequence line : titleLines) {
            g.text(font, line, x + 8, ty, TenkaichiUi.TEXT, true);
            ty += 10;
        }
        TenkaichiUi.divider(g, x + 6, ty + 1, width - 12);
        ty += 4;
        g.fill(x + 8, ty + 3, x + 12, ty + 7, TenkaichiUi.GOLD);
        for (FormattedCharSequence line : objLines) {
            g.text(font, line, x + 16, ty, 0xFFDDE6FF, true);
            ty += 10;
        }
        int stages = Math.max(1, data.getIntOr("stages", 1));
        int stage = data.getIntOr("stage", 0);
        int dotsX = x + 8;
        for (int i = 0; i < stages && i < 20; i++) {
            int color = i < stage ? TenkaichiUi.GOLD : i == stage ? 0xFFFFFFFF : 0xFF555A6A;
            g.fill(dotsX + i * 7, ty + 3, dotsX + i * 7 + 5, ty + 6, color);
        }
        Vec3 point = waypoint(mc);
        if (point != null) {
            double distance = point.distanceTo(mc.player.position());
            String meters = Math.round(distance) + "m";
            g.text(font, meters, x + width - 8 - font.width(meters), ty, TenkaichiUi.MUTED, true);
            if (data.getBooleanOr("waypoint", true)) {
                marker(g, mc, point, distance, story);
            }
        }
    }

    private static Vec3 waypoint(Minecraft mc) {
        if (!data.contains("wx") || mc.level == null) {
            return null;
        }
        String dim = data.getStringOr("wdim", "");
        if (!dim.equals(mc.level.dimension().identifier().toString())) {
            return null;
        }
        int entityId = data.getIntOr("entity", -1);
        if (entityId >= 0) {
            Entity entity = mc.level.getEntity(entityId);
            if (entity != null) {
                return entity.position().add(0, entity.getBbHeight() + 0.6, 0);
            }
        }
        return new Vec3(data.getDoubleOr("wx", 0), data.getDoubleOr("wy", 0), data.getDoubleOr("wz", 0));
    }

    private static void marker(GuiGraphicsExtractor g, Minecraft mc, Vec3 point, double distance, boolean story) {
        WorldProjection.Point p = WorldProjection.project(point, g.guiWidth(), g.guiHeight());
        int color = story ? TenkaichiUi.GOLD : 0xFF7CC8FF;
        float bob = (float) Math.sin(ClientState.clientTicks * 0.15) * 2.0f;
        int x = Math.round(p.x());
        int y = Math.round(p.y() + (p.onScreen() ? bob : 0));
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        if (!p.onScreen()) {
            g.pose().rotate(p.angle());
            g.fill(2, -4, 10, 4, 0xC0000000);
            for (int i = 0; i < 6; i++) {
                g.fill(3 + i, -3 + i / 2, 4 + i, 3 - i / 2, color);
            }
            g.pose().popMatrix();
            return;
        }
        g.pose().rotate((float) Math.toRadians(45));
        g.fill(-6, -6, 6, 6, 0xC0000000);
        g.fill(-5, -5, 5, 5, color);
        g.fill(-3, -3, 3, 3, 0xFF1A1206);
        g.fill(-1, -1, 1, 1, color);
        g.pose().popMatrix();
        String text = Math.round(distance) + "m";
        g.centeredText(mc.font, text, x, y + 10, 0xFFFFFFFF);
    }
}
