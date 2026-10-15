package com.dbtenkaichi.client.story;

import com.dbtenkaichi.client.ClientState;
import com.dbtenkaichi.client.anim.PoseApplier;
import com.dbtenkaichi.client.fx.CameraEffects;
import com.dbtenkaichi.client.ui.TenkaichiUi;
import com.dbtenkaichi.story.Scenes;
import com.dbtenkaichi.story.StoryScene;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ScenePlayer {
    private static @Nullable StoryScene scene;
    private static Vec3 anchor = Vec3.ZERO;
    private static final Map<String, Integer> ACTORS = new HashMap<>();
    private static long start;
    private static boolean seen;
    private static boolean hudWasHidden;

    private ScenePlayer() {
    }

    public static boolean active() {
        return scene != null;
    }

    public static int elapsed() {
        return scene == null ? -1 : (int) (ClientState.clientTicks - start);
    }

    public static String sceneId() {
        return scene == null ? "" : scene.id;
    }

    public static boolean skippable() {
        return scene != null && seen;
    }

    public static void start(CompoundTag tag) {
        StoryScene found = Scenes.get(tag.getStringOr("scene", ""));
        if (found == null) {
            return;
        }
        scene = found;
        anchor = new Vec3(tag.getDoubleOr("ax", 0), tag.getDoubleOr("ay", 0), tag.getDoubleOr("az", 0));
        seen = tag.getBooleanOr("seen", false);
        ACTORS.clear();
        CompoundTag actors = tag.getCompoundOrEmpty("actors");
        for (String key : actors.keySet()) {
            ACTORS.put(key, actors.getIntOr(key, -1));
        }
        start = ClientState.clientTicks;
        hudWasHidden = false;
    }

    public static void stop() {
        scene = null;
        ACTORS.clear();
    }

    private static float time(float partial) {
        return ClientState.clientTicks - start + partial;
    }

    public static void tick(Minecraft mc) {
        if (scene == null) {
            return;
        }
        float t = time(0);
        if (t > scene.duration + 60 || mc.level == null || mc.player == null) {
            stop();
            return;
        }
        for (StoryScene.Effect effect : scene.effects) {
            if ("shake".equals(effect.kind()) && t >= effect.from() && t < effect.to()) {
                CameraEffects.addShake(1.2f);
            }
        }
    }

    private static @Nullable Entity entity(String name) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return null;
        }
        Integer id = ACTORS.get(name);
        return id == null ? null : mc.level.getEntity(id);
    }

    private static Vec3 target(StoryScene.Cam cam, float partial) {
        if ("anchor".equals(cam.target())) {
            return anchor.add(0, 1.0, 0);
        }
        Entity entity = entity(cam.target());
        if (entity == null) {
            return anchor.add(0, 1.0, 0);
        }
        return entity.getPosition(partial).add(0, entity.getBbHeight() * cam.height(), 0);
    }

    public record Pose(Vec3 position, float yaw, float pitch) {
    }

    public static @Nullable Pose camera(float partial) {
        if (scene == null) {
            return null;
        }
        Minecraft mc = Minecraft.getInstance();
        int t = (int) Math.floor(time(partial));
        StoryScene.Shot shot = scene.shotAt(t);
        if (shot == null || mc.level == null) {
            return null;
        }
        float k = shot.to() <= shot.from() ? 0.0f : (time(partial) - shot.from()) / (shot.to() - shot.from());
        k = Mth.clamp(k, 0.0f, 1.0f);
        if (shot.ease()) {
            k = k * k * (3.0f - 2.0f * k);
        }
        StoryScene.Cam a = shot.start();
        StoryScene.Cam b = shot.end();
        Vec3 ta = target(a, partial);
        Vec3 tb = target(b, partial);
        Vec3 focus = ta.lerp(tb, k);
        float yaw = Mth.lerp(k, a.yaw(), b.yaw());
        float pitch = Mth.lerp(k, a.pitch(), b.pitch());
        float distance = Mth.lerp(k, a.distance(), b.distance());
        double rad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        Vec3 forward = new Vec3(-Math.sin(rad), 0, Math.cos(rad));
        Vec3 desired = focus.add(forward.scale(distance * Math.cos(pitchRad))).add(0, -distance * Math.sin(pitchRad), 0);
        Vec3 position = com.dbtenkaichi.client.cinematic.CameraSolver.solve(mc.level, focus, desired, mc.player, "scene:" + scene.id + ":" + shot.from());
        Vec3 look = focus.subtract(position);
        float lookYaw = (float) (Math.toDegrees(Math.atan2(look.z, look.x)) - 90.0);
        float lookPitch = (float) (-Math.toDegrees(Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z))));
        return new Pose(position, lookYaw, lookPitch);
    }

    public static void render(GuiGraphicsExtractor g, Minecraft mc) {
        if (scene == null) {
            return;
        }
        float partial = PoseApplier.partial();
        float t = time(partial);
        int w = g.guiWidth();
        int h = g.guiHeight();
        float in = Math.min(1.0f, t / 10.0f) * Math.min(1.0f, Math.max(0.0f, (scene.duration - t) / 10.0f));
        for (StoryScene.Effect effect : scene.effects) {
            if (t < effect.from() || t >= effect.to()) {
                continue;
            }
            float k = effect.to() <= effect.from() ? 1.0f : (t - effect.from()) / (effect.to() - effect.from());
            float value = effect.a() + (effect.b() - effect.a()) * k;
            int alpha = Math.round(Mth.clamp(value, 0.0f, 1.0f) * 255);
            switch (effect.kind()) {
                case "fade" -> g.fill(0, 0, w, h, alpha << 24);
                case "flash", "tint" -> g.fill(0, 0, w, h, alpha << 24 | (effect.color() & 0xFFFFFF));
                default -> {
                }
            }
        }
        int bar = Math.round(h * 0.11f * Math.max(in, 0.0f));
        g.fill(0, 0, w, bar, 0xFF000000);
        g.fill(0, h - bar, w, h, 0xFF000000);
        g.fill(0, bar, w, bar + 1, TenkaichiUi.GOLD);
        g.fill(0, h - bar - 1, w, h - bar, TenkaichiUi.GOLD);
        for (StoryScene.Effect effect : scene.effects) {
            if (effect.kind().startsWith("title:") && t >= effect.from() && t < effect.to()) {
                Component title = Component.translatable(effect.kind().substring(6));
                float fade = Math.min(1.0f, Math.min(t - effect.from(), effect.to() - t) / 12.0f);
                int a = Math.round(255 * Mth.clamp(fade, 0.0f, 1.0f));
                if (a > 8) {
                    g.pose().pushMatrix();
                    g.pose().translate(w / 2.0f, h * 0.28f);
                    g.pose().scale(2.2f, 2.2f);
                    g.centeredText(mc.font, title, 0, 0, a << 24 | 0xFFE26A);
                    g.pose().popMatrix();
                }
            }
        }
        StoryScene.Line line = scene.lineAt((int) t);
        if (line != null) {
            subtitle(g, mc.font, line, t, w, h, bar);
        }
        if (seen) {
            Component hint = Component.translatable("hud.dbtenkaichi.scene_skip", com.dbtenkaichi.client.input.Keybinds.SKIP_SCENE.getTranslatedKeyMessage());
            g.text(mc.font, hint, w - mc.font.width(hint) - 8, h - bar + Math.max(2, (bar - 9) / 2), 0xFFB4B8CC, true);
        }
    }

    private static void subtitle(GuiGraphicsExtractor g, Font font, StoryScene.Line line, float t, int w, int h, int bar) {
        Minecraft mc = Minecraft.getInstance();
        Component speaker = "player".equals(line.speaker()) && mc.player != null ? mc.player.getName()
                : Component.translatable("character.dbtenkaichi." + line.speaker());
        Component text = Component.translatable(line.key());
        int maxWidth = Math.min(360, w - 60);
        List<FormattedCharSequence> lines = font.split(text, maxWidth);
        int boxW = Math.max(font.width(speaker) + 24, maxWidth + 20);
        int boxH = 18 + lines.size() * 10;
        int x = (w - boxW) / 2;
        int y = h - bar - boxH - 10;
        float shown = Math.min(1.0f, (t - line.from()) / 6.0f);
        int alpha = Math.round(230 * shown);
        if (alpha < 10) {
            return;
        }
        TenkaichiUi.panel(g, x, y, boxW, boxH);
        g.fill(x + 6, y + 4, x + 6 + font.width(speaker) + 10, y + 15, 0xC0201408);
        g.text(font, speaker, x + 11, y + 6, TenkaichiUi.GOLD, true);
        int ty = y + 18;
        int visibleChars = Math.round((t - line.from()) * 2.2f);
        for (FormattedCharSequence seq : lines) {
            g.text(font, seq, x + 10, ty, TenkaichiUi.TEXT, true);
            ty += 10;
        }
        if (visibleChars < 0) {
            g.fill(x, y, x + boxW, y + boxH, 0x80000000);
        }
    }
}
