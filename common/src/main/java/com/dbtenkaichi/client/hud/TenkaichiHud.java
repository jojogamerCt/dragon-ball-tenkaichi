package com.dbtenkaichi.client.hud;

import com.dbtenkaichi.client.ClientState;
import com.dbtenkaichi.client.EntityVisual;
import com.dbtenkaichi.client.input.ClientController;
import com.dbtenkaichi.client.input.Keybinds;
import com.dbtenkaichi.client.input.LockOn;
import com.dbtenkaichi.client.render.CustomSkins;
import com.dbtenkaichi.client.ui.TenkaichiUi;
import com.dbtenkaichi.config.ConfigManager;
import com.dbtenkaichi.form.FormDefinition;
import com.dbtenkaichi.ki.BeamClashManager;
import com.dbtenkaichi.ki.Technique;
import com.dbtenkaichi.ki.Techniques;
import com.dbtenkaichi.platform.Services;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.player.RuntimeState;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

import java.text.NumberFormat;
import java.util.Locale;

public final class TenkaichiHud {
    private static final int PANEL_W = 176;
    private static final int PANEL_H = 82;
    private static float shownHealth = -1.0f;
    private static float trailHealth = -1.0f;
    private static float shownKi = -1.0f;
    private static float shownStamina = -1.0f;

    private TenkaichiHud() {
    }

    public static boolean replacesVanillaBars() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.isCreative() || mc.player.isSpectator()) {
            return false;
        }
        return Services.PLATFORM.playerData(mc.player).isCreated();
    }

    public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }
        PlayerData data = Services.PLATFORM.playerData(player);
        if (!data.isCreated()) {
            return;
        }
        if (com.dbtenkaichi.client.story.ScenePlayer.active()) {
            try {
                com.dbtenkaichi.client.story.ScenePlayer.render(g, mc);
            } catch (RuntimeException e) {
                com.dbtenkaichi.client.render.RenderGuard.report("scene_hud", e);
            }
            return;
        }
        try {
            float scale = Math.max(0.5f, Math.min(2.0f, ConfigManager.client().hudScale));
            EntityVisual visual = ClientState.visual(player.getId());
            if (com.dbtenkaichi.client.cinematic.CinematicDirector.active(visual)) {
                cinematic(g, mc, visual);
                return;
            }
            if (mc.gui.hud.isHidden()) {
                return;
            }
            cinematic(g, mc, visual);
            g.pose().pushMatrix();
            g.pose().scale(scale, scale);
            panel(g, mc, player, data, visual);
            g.pose().popMatrix();
            skills(g, mc, data);
            reticle(g, mc, delta);
            clash(g, mc);
            indicators(g, mc);
            SenseHud.render(g, mc);
            MissionHud.render(g, mc);
        } catch (RuntimeException e) {
            com.dbtenkaichi.client.render.RenderGuard.report("hud", e);
        }
    }

    private static float approach(float current, float target, float speed) {
        if (current < 0) {
            return target;
        }
        return current + (target - current) * speed;
    }

    private static void panel(GuiGraphicsExtractor g, Minecraft mc, LocalPlayer player, PlayerData data, EntityVisual visual) {
        Font font = mc.font;
        int x = 4;
        int y = 4;
        float maxHealth = Math.max(1.0f, player.getMaxHealth());
        shownHealth = approach(shownHealth, player.getHealth(), 0.35f);
        trailHealth = trailHealth < shownHealth ? shownHealth : approach(trailHealth, shownHealth, 0.05f);
        shownKi = approach(shownKi, ClientState.ki, 0.3f);
        shownStamina = approach(shownStamina, ClientState.stamina, 0.3f);
        FormDefinition form = visual.formDefinition();
        int accent = form == null ? TenkaichiUi.GOLD : 0xFF000000 | visual.auraColor();

        TenkaichiUi.panel(g, x + 20, y + 4, PANEL_W - 20, PANEL_H - 6);
        g.fill(x + 44, y + 5, x + PANEL_W - 2, y + 17, 0x50000000);
        portrait(g, mc, player, visual, x, y, accent);

        int tx = x + 48;
        Component title = form == null ? Component.translatable(data.race().translationKey())
                : Component.translatable(data.race().translationKey()).append(" · ").append(Component.translatable(form.name));
        g.text(font, title, tx, y + 7, form == null ? TenkaichiUi.TEXT : accent, true);
        if (visual.kaioken() > 0) {
            Component kaioken = Component.translatable("hud.dbtenkaichi.kaioken", (int) com.dbtenkaichi.form.KaiokenSystem.multiplier(visual.kaioken()));
            g.text(font, kaioken, x + PANEL_W - 4 - font.width(kaioken), y + 19, 0xFFFF5A3C, true);
        }
        if (ConfigManager.client().showBattlePower) {
            String bp = NumberFormat.getIntegerInstance(Locale.ROOT).format(ClientState.battlePower);
            g.text(font, "BP", tx, y + 19, 0xFFB08A3A, true);
            g.text(font, bp, tx + 14, y + 19, TenkaichiUi.GOLD_LIGHT, true);
        }

        int gx = x + 46;
        int gw = PANEL_W - 54;
        int hy = y + 30;
        TenkaichiUi.gauge(g, gx, hy, gw, 8, shownHealth / maxHealth, trailHealth / maxHealth, 0xFFE8343F);
        smallText(g, font, String.format(Locale.ROOT, "%d/%d", (int) Math.ceil(player.getHealth()), (int) maxHealth), gx + 6, hy);
        int kiColor = 0xFF000000 | (form != null ? visual.auraColor() : visual.appearance.auraColor);
        if ((kiColor & 0xFF) > 0xD0 && (kiColor >> 8 & 0xFF) > 0xD0 && (kiColor >> 16 & 0xFF) > 0xD0) {
            kiColor = 0xFF5FB8FF;
        }
        int ky = hy + 11;
        TenkaichiUi.gauge(g, gx - 4, ky, gw, 8, shownKi / Math.max(1.0f, ClientState.maxKi), 0.0f, kiColor);
        smallText(g, font, Component.translatable("hud.dbtenkaichi.ki", (int) ClientState.ki, (int) ClientState.maxKi).getString(), gx + 2, ky);
        int sy = ky + 11;
        TenkaichiUi.gauge(g, gx - 8, sy, (int) (gw * 0.62f), 4, shownStamina / Math.max(1.0f, ClientState.maxStamina), 0.0f, 0xFFB9F25A);
        int food = player.getFoodData().getFoodLevel();
        int fx = gx - 8 + (int) (gw * 0.62f) + 4;
        TenkaichiUi.gauge(g, fx, sy, gx + gw - 8 - fx, 4, food / 20.0f, 0.0f, 0xFFE8A040);

        int infoY = y + PANEL_H - 15;
        int right = x + PANEL_W - 4;
        Component tp = Component.translatable("hud.dbtenkaichi.tp", compact(ClientState.trainingPoints));
        int cursor = x + 26;
        g.text(font, tp, cursor, infoY, TenkaichiUi.MUTED, true);
        cursor += font.width(tp) + 8;
        int armor = player.getArmorValue();
        if (armor > 0) {
            String shield = "⛨ " + armor;
            g.text(font, shield, cursor, infoY, 0xFFBFD4E8, true);
            cursor += font.width(shield) + 8;
        }
        if (form != null) {
            Component mastery = Component.translatable("hud.dbtenkaichi.mastery");
            int labelWidth = font.width(mastery);
            if (cursor + labelWidth + 4 + 16 <= right) {
                g.text(font, mastery, cursor, infoY, TenkaichiUi.MUTED, true);
                cursor += labelWidth + 4;
            }
            int gaugeWidth = Math.min(30, right - cursor);
            if (gaugeWidth >= 8) {
                TenkaichiUi.gauge(g, cursor, infoY + 2, gaugeWidth, 4, visual.mastery, 0.0f, 0xFFFFE25A);
            }
        }
        if (ClientState.fatigue > 0.3f) {
            g.text(font, Component.translatable("hud.dbtenkaichi.fatigue", Math.round(ClientState.fatigue * 100)), x + PANEL_W + 4, y + 8, 0xFFFF9A6A, true);
        }
    }

    static String compact(long value) {
        long abs = Math.abs(value);
        if (abs < 100_000L) {
            return NumberFormat.getIntegerInstance(Locale.ROOT).format(value);
        }
        if (abs < 1_000_000L) {
            return String.format(Locale.ROOT, "%.1fK", value / 1_000.0);
        }
        if (abs < 1_000_000_000L) {
            return String.format(Locale.ROOT, "%.2fM", value / 1_000_000.0);
        }
        return String.format(Locale.ROOT, "%.2fB", value / 1_000_000_000.0);
    }

    private static void smallText(GuiGraphicsExtractor g, Font font, String text, int x, int y) {
        g.pose().pushMatrix();
        g.pose().translate(x, y + 1);
        g.pose().scale(0.75f, 0.75f);
        g.text(font, text, 0, 0, 0xFFFFFFFF, true);
        g.pose().popMatrix();
    }

    private static void portrait(GuiGraphicsExtractor g, Minecraft mc, LocalPlayer player, EntityVisual visual, int x, int y, int accent) {
        int size = 44;
        TenkaichiUi.sprite(g, TenkaichiUi.PORTRAIT_FRAME, x, y, size, size, accent);
        int face = 26;
        int fx = x + (size - face) / 2;
        int fy = y + (size - face) / 2;
        if (visual.appearance.useOwnSkin) {
            Identifier skin = player.getSkin().body().texturePath();
            g.blit(RenderPipelines.GUI_TEXTURED, skin, fx, fy, 8, 8, face, face, 8, 8, 64, 64);
            g.blit(RenderPipelines.GUI_TEXTURED, skin, fx, fy, 40, 8, face, face, 8, 8, 64, 64);
        } else {
            Identifier skin = CustomSkins.textureFor(visual.race, visual.appearance, visual.eyeColor(), visual.hairColor());
            g.blit(RenderPipelines.GUI_TEXTURED, skin, fx, fy, 16, 16, face, face, 16, 16, 128, 128);
        }
        g.outline(fx - 1, fy - 1, face + 2, face + 2, 0x80000000);
    }

    private static void skills(GuiGraphicsExtractor g, Minecraft mc, PlayerData data) {
        Font font = mc.font;
        int size = 22;
        int gap = 3;
        int total = PlayerData.SKILL_SLOTS * size + (PlayerData.SKILL_SLOTS - 1) * gap;
        int x = g.guiWidth() / 2 + 98;
        int y = g.guiHeight() - size - 2;
        if (x + total > g.guiWidth() - 4) {
            x = g.guiWidth() - total - 4;
            y -= 28;
        }
        boolean charging = ClientState.localHas(RuntimeState.FLAG_SKILL_CHARGING);
        for (int i = 0; i < PlayerData.SKILL_SLOTS; i++) {
            int sx = x + i * (size + gap);
            String id = data.skillInSlot(i);
            Technique technique = id.isEmpty() ? null : Techniques.get(id);
            boolean held = Keybinds.SKILLS[i].isDown();
            TenkaichiUi.slot(g, sx, y, size, held && technique != null);
            if (technique != null) {
                boolean affordable = ClientState.ki >= technique.kiCost();
                TenkaichiUi.sprite(g, TenkaichiUi.skillIcon(technique.id()), sx + 3, y + 3, size - 6, size - 6, affordable ? 0xFFFFFFFF : 0xFF707070);
                if (!affordable) {
                    g.fill(sx + 3, y + 3, sx + size - 3, y + size - 3, 0x50800000);
                }
                if (charging && technique.type() == Technique.Type.BEAM && held) {
                    float ratio = Math.min(1.0f, (float) ClientState.chargeTicks / Math.max(1, technique.maxCharge()));
                    int color = ClientState.chargeTicks < technique.minCharge() ? 0xFFFF8040 : 0xFF40E0FF;
                    TenkaichiUi.gauge(g, sx, y + size + 1, size, 3, ratio, 0.0f, color);
                }
            }
            String key = Keybinds.SKILLS[i].getTranslatedKeyMessage().getString();
            if (key.length() > 3) {
                key = key.substring(0, 3);
            }
            int kw = font.width(key);
            g.fill(sx + size - kw - 3, y - 4, sx + size + 1, y + 5, 0xD0101420);
            g.text(font, key, sx + size - kw - 1, y - 3, TenkaichiUi.GOLD_LIGHT, false);
        }
    }

    private static void reticle(GuiGraphicsExtractor g, Minecraft mc, DeltaTracker delta) {
        LivingEntity target = LockOn.targetEntity();
        if (target == null) {
            return;
        }
        float partial = delta.getGameTimeDeltaPartialTick(false);
        Vec3 pos = target.getPosition(partial).add(0, target.getBbHeight() * 0.55, 0);
        float[] screen = project(mc, g, pos);
        if (screen == null) {
            return;
        }
        int cx = (int) screen[0];
        int cy = (int) screen[1];
        float time = ClientState.clientTicks + partial;
        int r = 11 + (int) (Math.sin(time * 0.25f) * 1.5f);
        int color = 0xFFFF5050;
        int len = 5;
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sy = -1; sy <= 1; sy += 2) {
                int px = cx + sx * r;
                int py = cy + sy * r;
                g.fill(Math.min(px, px - sx * len), py, Math.max(px, px - sx * len) + 1, py + 1, color);
                g.fill(px, Math.min(py, py - sy * len), px + 1, Math.max(py, py - sy * len) + 1, color);
            }
        }
        g.fill(cx - 1, cy - 1, cx + 2, cy + 2, 0xFFFFFFFF);
        float hp = target.getHealth() / Math.max(1.0f, target.getMaxHealth());
        TenkaichiUi.gauge(g, cx - 22, cy + r + 4, 44, 4, hp, 0.0f, 0xFFE8343F);
        Component name = target.getDisplayName();
        int w = mc.font.width(name);
        g.fill(cx - w / 2 - 3, cy + r + 10, cx + w / 2 + 3, cy + r + 20, 0xB0101420);
        g.centeredText(mc.font, name, cx, cy + r + 11, TenkaichiUi.TEXT);
    }

    public static float[] project(Minecraft mc, GuiGraphicsExtractor g, Vec3 world) {
        Camera camera = mc.gameRenderer.mainCamera();
        Vec3 rel = world.subtract(camera.position());
        Vector3fc f = camera.forwardVector();
        Vector3fc u = camera.upVector();
        Vector3fc l = camera.leftVector();
        double z = rel.x * f.x() + rel.y * f.y() + rel.z * f.z();
        if (z < 0.1) {
            return null;
        }
        double x = rel.x * l.x() + rel.y * l.y() + rel.z * l.z();
        double y = rel.x * u.x() + rel.y * u.y() + rel.z * u.z();
        double fov = Math.toRadians(camera.getFov());
        double scale = (g.guiHeight() / 2.0) / Math.tan(fov / 2.0);
        double sx = g.guiWidth() / 2.0 - x / z * scale;
        double sy = g.guiHeight() / 2.0 - y / z * scale;
        return new float[]{(float) sx, (float) sy};
    }

    private static void clash(GuiGraphicsExtractor g, Minecraft mc) {
        if (ClientState.clashId < 0 || mc.player == null) {
            return;
        }
        int width = 180;
        int x = Math.max(g.guiWidth() / 2 - width / 2, PANEL_W + 10);
        int y = 12;
        boolean first = ClientState.clashFirst == mc.player.getId();
        boolean involved = first || ClientState.clashSecond == mc.player.getId();
        float balance = first ? ClientState.clashBalance : -ClientState.clashBalance;
        TenkaichiUi.panel(g, x - 8, y - 6, width + 16, 22);
        int mid = x + width / 2;
        int marker = mid + (int) (balance * width / 2);
        g.fillGradient(x, y, marker, y + 10, 0xFF6FC8FF, 0xFF1F5FAF);
        g.fillGradient(marker, y, x + width, y + 10, 0xFFFF8A5A, 0xFFAF2F1A);
        g.fill(x, y, x + width, y + 1, 0x60FFFFFF);
        g.fill(marker - 2, y - 3, marker + 2, y + 13, 0xFFFFFFFF);
        g.fill(marker - 1, y - 4, marker + 1, y + 14, TenkaichiUi.GOLD);
        int phase = (int) ((ClientState.clientTicks - ClientState.clashUpdated + ClientState.clashPulse) % BeamClashManager.PULSE_PERIOD);
        boolean window = phase <= BeamClashManager.PULSE_WINDOW || phase >= BeamClashManager.PULSE_PERIOD - BeamClashManager.PULSE_WINDOW;
        float ring = 1.0f - (float) phase / BeamClashManager.PULSE_PERIOD;
        int rs = 6 + (int) (ring * 18);
        int rc = window ? 0xFF7CFF7C : 0x88FFFFFF;
        int centerX = x + width / 2;
        g.outline(centerX - rs, y + 26 - rs / 2, rs * 2, rs, rc);
        g.outline(centerX - 6, y + 26 - 3, 12, 6, TenkaichiUi.GOLD);
        if (involved) {
            g.centeredText(mc.font, Component.translatable("hud.dbtenkaichi.clash_hint", Keybinds.CHARGE.getTranslatedKeyMessage()), centerX, y + 40, window ? 0xFF7CFF7C : 0xFFDDDDDD);
        }
    }

    private static void pill(GuiGraphicsExtractor g, Font font, Component text, int cx, int y, int color) {
        int w = font.width(text) + 12;
        g.fill(cx - w / 2, y - 2, cx + w / 2, y + 9, 0xA0101420);
        g.fill(cx - w / 2, y - 2, cx - w / 2 + 2, y + 9, color);
        g.fill(cx + w / 2 - 2, y - 2, cx + w / 2, y + 9, color);
        g.centeredText(font, text, cx, y, color);
    }

    private static void indicators(GuiGraphicsExtractor g, Minecraft mc) {
        int cx = g.guiWidth() / 2;
        int cy = g.guiHeight() / 2;
        Font font = mc.font;
        int y = cy + 14;
        if (ClientState.localHas(RuntimeState.FLAG_FLYING)) {
            Component text = Component.translatable(ClientState.localHas(RuntimeState.FLAG_FAST_FLIGHT) ? "hud.dbtenkaichi.flight_fast" : "hud.dbtenkaichi.flight");
            pill(g, font, text, cx, y, 0xFF9FD8FF);
            y += 13;
        }
        if (ClientController.inStance()) {
            pill(g, font, Component.translatable("hud.dbtenkaichi.stance"), cx, y, 0xFFFFC870);
            y += 13;
        }
        int hold = ClientController.attackHoldTicks();
        if (hold >= 6) {
            float ratio = Math.min(1.0f, (hold - 6) / 20.0f);
            TenkaichiUi.gauge(g, cx - 18, y, 36, 4, ratio, 0.0f, 0xFFFF8040);
        }
        if (ClientState.localHas(RuntimeState.FLAG_EXHAUSTED)) {
            pill(g, font, Component.translatable("hud.dbtenkaichi.exhausted"), cx, cy - 26, TenkaichiUi.DANGER);
        }
    }

    private static void cinematic(GuiGraphicsExtractor g, Minecraft mc, EntityVisual visual) {
        long elapsed = ClientState.clientTicks - visual.cinematicStart;
        if (!visual.cinematicFull || elapsed >= visual.cinematicTicks) {
            return;
        }
        float t = Math.min(1.0f, elapsed / 8.0f) * Math.min(1.0f, (visual.cinematicTicks - elapsed) / 8.0f);
        float partial = com.dbtenkaichi.client.anim.PoseApplier.partial();
        float dark = com.dbtenkaichi.client.cinematic.CinematicDirector.darken(visual, partial);
        if (dark > 0.0f) {
            int a = Math.round(Math.min(1.0f, dark) * 235);
            g.fillGradient(0, 0, g.guiWidth(), g.guiHeight() / 2, a << 24 | 0x06100A, (a * 2 / 3) << 24 | 0x0A140C);
            g.fillGradient(0, g.guiHeight() / 2, g.guiWidth(), g.guiHeight(), (a * 2 / 3) << 24 | 0x0A140C, a << 24 | 0x06100A);
        }
        com.dbtenkaichi.client.cinematic.CinematicScript.Backdrop backdrop = com.dbtenkaichi.client.cinematic.CinematicDirector.backdrop(visual, partial);
        float streak = com.dbtenkaichi.client.cinematic.CinematicDirector.streak(visual, partial);
        if (backdrop != null) {
            g.fill(0, 0, g.guiWidth(), g.guiHeight(), 0xFF000000 | backdrop.color());
            if (streak >= 0.0f) {
                streak(g, streak, 1.0f);
            }
            bust(g, mc, backdrop, elapsed + partial);
        } else if (streak >= 0.0f) {
            streak(g, streak, 0.8f);
        }
        for (com.dbtenkaichi.client.cinematic.CinematicScript.Orb orb : com.dbtenkaichi.client.cinematic.CinematicDirector.orbs(visual, partial)) {
            orb(g, orb);
        }
        float lines = com.dbtenkaichi.client.cinematic.CinematicDirector.speedLines(visual, partial);
        if (lines > 0.0f) {
            speedLines(g, lines, elapsed + partial);
        }
        float beat = com.dbtenkaichi.client.cinematic.CinematicDirector.heartbeat(visual, partial);
        if (beat > 0.0f) {
            heartbeat(g, beat, elapsed + partial);
        }
        int tint = com.dbtenkaichi.client.cinematic.CinematicDirector.tintColor(visual, partial);
        if ((tint >>> 24) > 0) {
            g.fill(0, 0, g.guiWidth(), g.guiHeight(), tint);
        }
        float flash = com.dbtenkaichi.client.cinematic.CinematicDirector.flash(visual, partial);
        if (flash > 0.0f) {
            int a = Math.round(Math.min(1.0f, flash) * 255);
            g.fill(0, 0, g.guiWidth(), g.guiHeight(), a << 24 | (com.dbtenkaichi.client.cinematic.CinematicDirector.flashColor(visual) & 0xFFFFFF));
        }
        int bar = (int) (g.guiHeight() * 0.11f * t);
        g.fill(0, 0, g.guiWidth(), bar, 0xFF000000);
        g.fill(0, g.guiHeight() - bar, g.guiWidth(), g.guiHeight(), 0xFF000000);
        g.fill(0, bar, g.guiWidth(), bar + 1, TenkaichiUi.GOLD);
        g.fill(0, g.guiHeight() - bar - 1, g.guiWidth(), g.guiHeight() - bar, TenkaichiUi.GOLD);
        FormDefinition form = com.dbtenkaichi.form.FormRegistry.get(visual.cinematicForm, true);
        if (form != null && com.dbtenkaichi.client.cinematic.CinematicDirector.revealed(visual)) {
            int color = 0xFF000000 | FormDefinition.parseColor(form.visual.auraColor, 0xFFFFFF);
            g.pose().pushMatrix();
            g.pose().translate(g.guiWidth() / 2.0f, g.guiHeight() - bar - 22);
            g.pose().scale(2.0f, 2.0f);
            g.centeredText(mc.font, Component.translatable(form.name), 0, 0, color);
            g.pose().popMatrix();
        }
    }

    private static void orb(GuiGraphicsExtractor g, com.dbtenkaichi.client.cinematic.CinematicScript.Orb orb) {
        int w = g.guiWidth();
        int h = g.guiHeight();
        int cx = Math.round(w * orb.x());
        int cy = Math.round(h * orb.y());
        int r = Math.max(4, Math.round(h * orb.radius()));
        int color = orb.color() & 0xFFFFFF;
        for (int ring = 3; ring >= 1; ring--) {
            int rr = r + ring * r / 5;
            int alpha = 40 / ring;
            for (int dy = -rr; dy <= rr; dy++) {
                int half = (int) Math.sqrt(Math.max(0, rr * rr - dy * dy));
                g.fill(cx - half, cy + dy, cx + half, cy + dy + 1, alpha << 24 | color);
            }
        }
        for (int dy = -r; dy <= r; dy++) {
            int half = (int) Math.sqrt(Math.max(0, r * r - dy * dy));
            float shade = 0.85f + 0.15f * (1.0f - Math.abs(dy) / (float) r);
            g.fill(cx - half, cy + dy, cx + half, cy + dy + 1, 0xFF000000 | (TenkaichiUi.mix(0xFF000000 | color, 0xFFFFFFFF, 1.0f - shade) & 0xFFFFFF));
        }
        for (int i = 0; i < 7; i++) {
            int sx = cx + Math.round((float) Math.cos(i * 1.7) * r * 0.5f);
            int sy = cy + Math.round((float) Math.sin(i * 2.3) * r * 0.45f);
            int sr = Math.max(1, r / (6 + i));
            g.fill(sx - sr, sy - sr / 2, sx + sr, sy + sr / 2, 0x30000000);
        }
    }

    private static void speedLines(GuiGraphicsExtractor g, float strength, float time) {
        int w = g.guiWidth();
        int h = g.guiHeight();
        g.pose().pushMatrix();
        g.pose().translate(w * 0.5f, h * 0.5f);
        int count = Math.round(40 * Math.min(1.0f, strength));
        for (int i = 0; i < count; i++) {
            float angle = (float) (i * 2.399963 + Math.floor(time * 0.5) * 0.7);
            int inner = Math.round(Math.min(w, h) * (0.32f + ((i * 37) % 17) / 60.0f));
            int outer = Math.max(w, h);
            g.pose().pushMatrix();
            g.pose().rotate(angle);
            int alpha = Math.round(170 * Math.min(1.0f, strength));
            g.fill(inner, -1, outer, 1, alpha << 24 | 0xFFFFFF);
            g.pose().popMatrix();
        }
        g.pose().popMatrix();
    }

    private static void heartbeat(GuiGraphicsExtractor g, float strength, float time) {
        float pulse = (float) Math.pow(Math.max(0.0, Math.sin(time * 0.35)), 8.0);
        int alpha = Math.round(160 * Math.min(1.0f, strength) * (0.35f + pulse * 0.65f));
        int w = g.guiWidth();
        int h = g.guiHeight();
        int edge = Math.round(Math.min(w, h) * (0.12f + pulse * 0.08f));
        for (int i = 0; i < edge; i++) {
            int a = alpha * (edge - i) / edge;
            int c = a << 24 | 0x8A0A0A;
            g.fill(0, i, w, i + 1, c);
            g.fill(0, h - i - 1, w, h - i, c);
            g.fill(i, 0, i + 1, h, c);
            g.fill(w - i - 1, 0, w - i, h, c);
        }
    }

    private static void streak(GuiGraphicsExtractor g, float progress, float alpha) {
        int w = g.guiWidth();
        int h = g.guiHeight();
        float grow = Math.min(1.0f, progress * 2.4f);
        float ease = 1.0f - (1.0f - grow) * (1.0f - grow);
        float fade = progress < 0.7f ? 1.0f : Math.max(0.0f, 1.0f - (progress - 0.7f) / 0.3f);
        int a = Math.round(255 * alpha * fade);
        int length = (int) (w * 1.7f * ease);
        if (a <= 0 || length <= 0) {
            return;
        }
        int core = Math.max(2, h / 70);
        int x0 = (int) (-w * 0.85f);
        int x1 = x0 + length;
        g.pose().pushMatrix();
        g.pose().translate(w * 0.5f, h * 0.5f);
        g.pose().rotate((float) Math.toRadians(-14.0));
        g.fill(x0, -core * 6, x1, core * 6, (a * 60 / 255) << 24 | 0xC81400);
        g.fill(x0, -core * 3, x1, core * 3, (a * 150 / 255) << 24 | 0xF02A10);
        g.fill(x0, -core, x1, core, a << 24 | 0xFF5030);
        g.fill(x0, -Math.max(1, core / 2), x1, Math.max(1, core / 2), a << 24 | 0xFFD8C0);
        g.fill(x1 - core * 10, -core * 4, x1, core * 4, (a * 190 / 255) << 24 | 0xFFA070);
        g.pose().popMatrix();
    }

    private static void bust(GuiGraphicsExtractor g, Minecraft mc, com.dbtenkaichi.client.cinematic.CinematicScript.Backdrop backdrop, float time) {
        if (mc.player == null) {
            return;
        }
        int w = g.guiWidth();
        int h = g.guiHeight();
        int size = Math.round(h * backdrop.zoom());
        int cx = w / 2 + Math.round((float) Math.sin(time * 2.7f) * 1.5f);
        int cy = Math.round(h * backdrop.centerY()) + Math.round((float) Math.cos(time * 3.1f) * 1.0f);
        com.dbtenkaichi.client.screen.EntityPreview.draw(g, cx - w, cy - h, cx + w, cy + h, size, backdrop.yaw(), 0.0f, 1.0f, mc.player);
    }
}
