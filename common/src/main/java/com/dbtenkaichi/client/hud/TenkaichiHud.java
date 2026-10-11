package com.dbtenkaichi.client.hud;

import com.dbtenkaichi.client.ClientState;
import com.dbtenkaichi.client.EntityVisual;
import com.dbtenkaichi.client.input.ClientController;
import com.dbtenkaichi.client.input.Keybinds;
import com.dbtenkaichi.client.input.LockOn;
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
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

import java.text.NumberFormat;
import java.util.Locale;

public final class TenkaichiHud {
    private static float shownHealth = -1.0f;
    private static float trailHealth = -1.0f;
    private static float shownKi = -1.0f;
    private static float shownStamina = -1.0f;

    private TenkaichiHud() {
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
        try {
            float scale = Math.max(0.5f, Math.min(2.0f, ConfigManager.client().hudScale));
            EntityVisual visual = ClientState.visual(player.getId());
            cinematic(g, mc, visual);
            g.pose().pushMatrix();
            g.pose().scale(scale, scale);
            panel(g, mc, player, data, visual);
            g.pose().popMatrix();
            skills(g, mc, data);
            reticle(g, mc, delta);
            clash(g, mc);
            indicators(g, mc);
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
        int x = 6;
        int y = 6;
        int width = 132;
        float maxHealth = Math.max(1.0f, player.getMaxHealth());
        shownHealth = approach(shownHealth, player.getHealth(), 0.35f);
        trailHealth = trailHealth < shownHealth ? shownHealth : approach(trailHealth, shownHealth, 0.06f);
        shownKi = approach(shownKi, ClientState.ki, 0.3f);
        shownStamina = approach(shownStamina, ClientState.stamina, 0.3f);
        g.fill(x - 3, y - 3, x + width + 3, y + 62, 0x88000000);
        g.outline(x - 3, y - 3, width + 6, 65, 0x55FFFFFF);
        FormDefinition form = visual.formDefinition();
        Component title = form == null ? Component.translatable(data.race().translationKey())
                : Component.translatable(data.race().translationKey()).append(" · ").append(Component.translatable(form.name));
        int titleColor = form == null ? 0xFFFFFFFF : 0xFF000000 | visual.auraColor();
        g.text(font, title, x, y, titleColor, true);
        if (ConfigManager.client().showBattlePower) {
            String bp = "BP " + NumberFormat.getIntegerInstance(Locale.ROOT).format(ClientState.battlePower);
            g.text(font, bp, x, y + 10, 0xFFFFD45A, true);
        }
        int barY = y + 22;
        bar(g, x, barY, width, 8, trailHealth / maxHealth, 0xFFFFFFFF, 0xFFFFFFFF, 0x55FFFFFF);
        bar(g, x, barY, width, 8, shownHealth / maxHealth, 0xFFE8343F, 0xFF9C1222, 0);
        g.text(font, String.format(Locale.ROOT, "%d/%d", (int) Math.ceil(player.getHealth()), (int) maxHealth), x + 2, barY, 0xFFFFFFFF, true);
        int kiColor = 0xFF000000 | (form != null ? visual.auraColor() : visual.appearance.auraColor);
        if ((kiColor & 0xFF) > 0xD0 && (kiColor >> 8 & 0xFF) > 0xD0 && (kiColor >> 16 & 0xFF) > 0xD0) {
            kiColor = 0xFF6FC3FF;
        }
        bar(g, x, barY + 11, width, 8, shownKi / Math.max(1.0f, ClientState.maxKi), kiColor, darker(kiColor), 0);
        g.text(font, Component.translatable("hud.dbtenkaichi.ki", (int) ClientState.ki, (int) ClientState.maxKi), x + 2, barY + 11, 0xFFFFFFFF, true);
        bar(g, x, barY + 22, (int) (width * 0.8f), 4, shownStamina / Math.max(1.0f, ClientState.maxStamina), 0xFFB9F25A, 0xFF5E8F1F, 0);
        int infoY = barY + 29;
        g.text(font, Component.translatable("hud.dbtenkaichi.tp", ClientState.trainingPoints), x, infoY, 0xFFCCCCCC, true);
        if (form != null) {
            int mx = x + 64;
            g.text(font, Component.translatable("hud.dbtenkaichi.mastery"), mx, infoY, 0xFFCCCCCC, true);
            bar(g, mx + 42, infoY + 2, 26, 4, visual.mastery, 0xFFFFE25A, 0xFFB08A1A, 0);
        }
        if (ClientState.fatigue > 0.3f) {
            g.text(font, Component.translatable("hud.dbtenkaichi.fatigue", Math.round(ClientState.fatigue * 100)), x, infoY + 10, 0xFFFF9A6A, true);
        }
    }

    private static int darker(int argb) {
        int r = (argb >> 16 & 255) * 3 / 5;
        int gr = (argb >> 8 & 255) * 3 / 5;
        int b = (argb & 255) * 3 / 5;
        return 0xFF000000 | r << 16 | gr << 8 | b;
    }

    private static void bar(GuiGraphicsExtractor g, int x, int y, int w, int h, float ratio, int top, int bottom, int background) {
        float clamped = Math.max(0.0f, Math.min(1.0f, Float.isNaN(ratio) ? 0.0f : ratio));
        if (background != 0) {
            g.fill(x, y, x + (int) (w * clamped), y + h, background);
            return;
        }
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xCC101010);
        g.fill(x, y, x + w, y + h, 0xFF2A2A2A);
        int filled = (int) (w * clamped);
        if (filled > 0) {
            g.fillGradient(x, y, x + filled, y + h, top, bottom);
            g.fill(x, y, x + filled, y + 1, 0x66FFFFFF);
        }
    }

    private static void skills(GuiGraphicsExtractor g, Minecraft mc, PlayerData data) {
        Font font = mc.font;
        int size = 20;
        int gap = 3;
        int total = PlayerData.SKILL_SLOTS * size + (PlayerData.SKILL_SLOTS - 1) * gap;
        int x = g.guiWidth() / 2 + 98;
        int y = g.guiHeight() - size - 3;
        if (x + total > g.guiWidth() - 4) {
            x = g.guiWidth() - total - 4;
            y -= 26;
        }
        boolean charging = ClientState.localHas(RuntimeState.FLAG_SKILL_CHARGING);
        for (int i = 0; i < PlayerData.SKILL_SLOTS; i++) {
            int sx = x + i * (size + gap);
            String id = data.skillInSlot(i);
            Technique technique = id.isEmpty() ? null : Techniques.get(id);
            g.fill(sx, y, sx + size, y + size, 0xAA000000);
            g.outline(sx, y, size, size, technique == null ? 0x44FFFFFF : 0xAAFFD45A);
            if (technique != null) {
                String label = abbreviation(technique);
                g.centeredText(font, label, sx + size / 2, y + 6, technique.type() == Technique.Type.BEAM ? 0xFF7FD6FF : 0xFFFFFFFF);
                if (ClientState.ki < technique.kiCost()) {
                    g.fill(sx + 1, y + 1, sx + size - 1, y + size - 1, 0x88400000);
                }
                if (charging && technique.type() == Technique.Type.BEAM && Keybinds.SKILLS[i].isDown()) {
                    float ratio = Math.min(1.0f, (float) ClientState.chargeTicks / Math.max(1, technique.maxCharge()));
                    int color = ClientState.chargeTicks < technique.minCharge() ? 0xFFFF8040 : 0xFF40E0FF;
                    g.fill(sx, y + size + 1, sx + (int) (size * ratio), y + size + 3, color);
                }
            }
            String key = Keybinds.SKILLS[i].getTranslatedKeyMessage().getString();
            if (key.length() > 3) {
                key = key.substring(0, 3);
            }
            g.text(font, key, sx + 1, y - 9, 0xFFAAAAAA, true);
        }
    }

    private static String abbreviation(Technique technique) {
        if (technique == Techniques.KAMEHAMEHA) {
            return "KH";
        }
        if (technique == Techniques.KI_BLAST) {
            return "KB";
        }
        String id = technique.id();
        int colon = id.indexOf(':');
        return id.substring(colon + 1, Math.min(id.length(), colon + 3)).toUpperCase(Locale.ROOT);
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
        float spin = (ClientState.clientTicks + partial) * 0.08f;
        int r = 10;
        int color = 0xFFFF4848;
        for (int i = 0; i < 4; i++) {
            double a = spin + Math.PI / 2 * i;
            int px = cx + (int) (Math.cos(a) * r);
            int py = cy + (int) (Math.sin(a) * r);
            g.fill(px - 2, py - 2, px + 2, py + 2, color);
        }
        g.fill(cx - 1, cy - 1, cx + 1, cy + 1, 0xFFFFFFFF);
        float hp = target.getHealth() / Math.max(1.0f, target.getMaxHealth());
        g.fill(cx - 16, cy + 14, cx + 16, cy + 17, 0xAA000000);
        g.fill(cx - 16, cy + 14, cx - 16 + (int) (32 * hp), cy + 17, 0xFFE8343F);
        g.centeredText(mc.font, target.getDisplayName(), cx, cy + 19, 0xFFFFFFFF);
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
        int x = Math.max(g.guiWidth() / 2 - width / 2, 150);
        int y = 10;
        boolean first = ClientState.clashFirst == mc.player.getId();
        boolean involved = first || ClientState.clashSecond == mc.player.getId();
        float balance = first ? ClientState.clashBalance : -ClientState.clashBalance;
        g.fill(x - 2, y - 2, x + width + 2, y + 12, 0xCC000000);
        int mid = x + width / 2;
        int marker = mid + (int) (balance * width / 2);
        g.fillGradient(x, y, marker, y + 10, 0xFF3FA8FF, 0xFF1F5FAF);
        g.fillGradient(marker, y, x + width, y + 10, 0xFFFF6A3A, 0xFFAF2F1A);
        g.fill(marker - 1, y - 3, marker + 1, y + 13, 0xFFFFFFFF);
        int phase = (int) ((ClientState.clientTicks - ClientState.clashUpdated + ClientState.clashPulse) % BeamClashManager.PULSE_PERIOD);
        boolean window = phase <= BeamClashManager.PULSE_WINDOW || phase >= BeamClashManager.PULSE_PERIOD - BeamClashManager.PULSE_WINDOW;
        float ring = 1.0f - (float) phase / BeamClashManager.PULSE_PERIOD;
        int rs = 6 + (int) (ring * 18);
        int rc = window ? 0xFF7CFF7C : 0x88FFFFFF;
        int centerX = x + width / 2;
        g.outline(centerX - rs, y + 22 - rs / 2, rs * 2, rs, rc);
        if (involved) {
            g.centeredText(mc.font, Component.translatable("hud.dbtenkaichi.clash_hint", Keybinds.CHARGE.getTranslatedKeyMessage()), centerX, y + 36, window ? 0xFF7CFF7C : 0xFFDDDDDD);
        }
    }

    private static void indicators(GuiGraphicsExtractor g, Minecraft mc) {
        int cx = g.guiWidth() / 2;
        int cy = g.guiHeight() / 2;
        Font font = mc.font;
        int y = cy + 12;
        if (ClientState.localHas(RuntimeState.FLAG_FLYING)) {
            Component text = Component.translatable(ClientState.localHas(RuntimeState.FLAG_FAST_FLIGHT) ? "hud.dbtenkaichi.flight_fast" : "hud.dbtenkaichi.flight");
            g.centeredText(font, text, cx, y, 0xFF9FD8FF);
            y += 10;
        }
        if (ClientController.inStance()) {
            g.centeredText(font, Component.translatable("hud.dbtenkaichi.stance"), cx, y, 0xFFFFC870);
            y += 10;
        }
        int hold = ClientController.attackHoldTicks();
        if (hold >= 6) {
            float ratio = Math.min(1.0f, (hold - 6) / 20.0f);
            g.fill(cx - 15, y, cx + 15, y + 3, 0xAA000000);
            g.fill(cx - 15, y, cx - 15 + (int) (30 * ratio), y + 3, 0xFFFF8040);
        }
        if (ClientState.localHas(RuntimeState.FLAG_EXHAUSTED)) {
            g.centeredText(font, Component.translatable("hud.dbtenkaichi.exhausted"), cx, cy - 24, 0xFFFF6A6A);
        }
    }

    private static void cinematic(GuiGraphicsExtractor g, Minecraft mc, EntityVisual visual) {
        long elapsed = ClientState.clientTicks - visual.cinematicStart;
        if (!visual.cinematicFull || elapsed >= visual.cinematicTicks) {
            return;
        }
        float t = Math.min(1.0f, elapsed / 8.0f) * Math.min(1.0f, (visual.cinematicTicks - elapsed) / 8.0f);
        int bar = (int) (g.guiHeight() * 0.11f * t);
        g.fill(0, 0, g.guiWidth(), bar, 0xFF000000);
        g.fill(0, g.guiHeight() - bar, g.guiWidth(), g.guiHeight(), 0xFF000000);
        FormDefinition form = com.dbtenkaichi.form.FormRegistry.get(visual.cinematicForm, true);
        if (form != null && elapsed > visual.cinematicTicks * 0.6f) {
            g.centeredText(mc.font, Component.translatable(form.name), g.guiWidth() / 2, g.guiHeight() - bar - 14, 0xFF000000 | FormDefinition.parseColor(form.visual.auraColor, 0xFFFFFF));
        }
    }
}
