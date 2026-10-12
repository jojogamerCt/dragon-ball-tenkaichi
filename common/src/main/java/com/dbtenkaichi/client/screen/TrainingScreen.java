package com.dbtenkaichi.client.screen;

import com.dbtenkaichi.client.ClientState;
import com.dbtenkaichi.client.input.Keybinds;
import com.dbtenkaichi.ki.Techniques;
import com.dbtenkaichi.network.ActionType;
import com.dbtenkaichi.network.Packets;
import com.dbtenkaichi.platform.Services;
import com.dbtenkaichi.training.TrainingSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.dbtenkaichi.client.ui.TenkaichiButton;
import com.dbtenkaichi.client.ui.TenkaichiUi;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public class TrainingScreen extends Screen {
    private CompoundTag data;
    private String mode;
    private String activity;
    private int master;
    private long started;
    private int presses;
    private boolean charging;

    public TrainingScreen(CompoundTag data) {
        super(Component.translatable("screen.dbtenkaichi.training"));
        update(data);
    }

    public void update(CompoundTag tag) {
        String newMode = tag.getStringOr("mode", "menu");
        if ("band".equals(newMode) && data != null) {
            data.putFloat("band_min", tag.getFloatOr("band_min", 0.4f));
            data.putFloat("band_max", tag.getFloatOr("band_max", 0.55f));
            data.putInt("score", tag.getIntOr("score", 0));
            return;
        }
        this.data = tag.copy();
        this.mode = newMode;
        this.activity = tag.getStringOr("activity", "");
        if (tag.contains("master")) {
            this.master = tag.getIntOr("master", -1);
        }
        if ("run".equals(mode)) {
            started = ClientState.clientTicks;
            presses = 0;
            if (TrainingSystem.SPARRING.equals(activity) || TrainingSystem.DODGE_DRILL.equals(activity)) {
                Minecraft.getInstance().gui.setScreen(null);
                return;
            }
        }
        if (this.minecraft != null) {
            rebuildWidgets();
        }
    }

    private void send(String activity, int phase, int value) {
        Services.NETWORK.sendToServer(new Packets.TrainingInput(activity, phase, value));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = this.height / 2 - 70;
        switch (mode) {
            case "menu" -> {
                String[] activities = {TrainingSystem.MEDITATION, TrainingSystem.KI_CONTROL, TrainingSystem.SPARRING, TrainingSystem.DODGE_DRILL};
                for (String id : activities) {
                    addRenderableWidget(TenkaichiButton.create(Component.translatable("training.dbtenkaichi." + id), b -> send(id, TrainingSystem.PHASE_START, master))
                            .bounds(cx - 100, y, 200, 20).build());
                    y += 24;
                }
                boolean known = data.getBooleanOr("kamehameha_known", false);
                Button learn = addRenderableWidget(TenkaichiButton.create(Component.translatable(known ? "training.dbtenkaichi.learned" : "training.dbtenkaichi.learn",
                                Component.translatable(Techniques.KAMEHAMEHA.translationKey())),
                        b -> {
                            send(Techniques.KAMEHAMEHA.id(), TrainingSystem.PHASE_LEARN, master);
                            onClose();
                        }).bounds(cx - 100, y + 6, 200, 20).build());
                learn.active = !known && data.getBooleanOr("kamehameha_ready", false);
                addRenderableWidget(TenkaichiButton.create(Component.translatable("gui.done"), b -> onClose()).bounds(cx - 100, y + 34, 200, 20).build());
            }
            case "summary" -> addRenderableWidget(TenkaichiButton.create(Component.translatable("gui.done"), b -> onClose()).bounds(cx - 60, this.height / 2 + 40, 120, 20).build());
            default -> addRenderableWidget(TenkaichiButton.create(Component.translatable("training.dbtenkaichi.stop"), b -> {
                send(activity, TrainingSystem.PHASE_STOP, 0);
            }).bounds(cx - 60, this.height / 2 + 60, 120, 20).build());
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if ("run".equals(mode)) {
            if (TrainingSystem.MEDITATION.equals(activity) && event.key() == com.mojang.blaze3d.platform.InputConstants.KEY_SPACE) {
                presses++;
                send(activity, TrainingSystem.PHASE_PRESS, 0);
                return true;
            }
            if (TrainingSystem.KI_CONTROL.equals(activity) && Keybinds.CHARGE.matches(event)) {
                if (!charging) {
                    charging = true;
                    Services.NETWORK.sendToServer(new Packets.Action(ActionType.CHARGE_START.ordinal(), 0));
                }
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if ("run".equals(mode) && TrainingSystem.KI_CONTROL.equals(activity) && Keybinds.CHARGE.matches(event) && charging) {
            charging = false;
            Services.NETWORK.sendToServer(new Packets.Action(ActionType.CHARGE_END.ordinal(), 0));
            return true;
        }
        return super.keyReleased(event);
    }

    @Override
    public void onClose() {
        if ("run".equals(mode)) {
            send(activity, TrainingSystem.PHASE_STOP, 0);
        }
        if (charging) {
            charging = false;
            Services.NETWORK.sendToServer(new Packets.Action(ActionType.CHARGE_END.ordinal(), 0));
        }
        super.onClose();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
        TenkaichiUi.background(g, this.width, this.height);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
        int cx = this.width / 2;
        int cy = this.height / 2;
        TenkaichiUi.panel(g, cx - 154, cy - 116, 308, 210);
        TenkaichiUi.header(g, this.font, this.title, cx, cy - 128);
        super.extractRenderState(g, mouseX, mouseY, partial);
        switch (mode) {
            case "menu" -> {
                g.centeredText(this.font, Component.translatable("screen.dbtenkaichi.training.master"), cx, cy - 100, 0xFFFFD45A);
                g.centeredText(this.font, Component.translatable("screen.dbtenkaichi.training.status", data.getIntOr("sessions", 0), Math.round(data.getFloatOr("fatigue", 0) * 100)), cx, cy - 88, 0xFFBBBBBB);
            }
            case "summary" -> {
                g.centeredText(this.font, Component.translatable("training.dbtenkaichi." + activity), cx, cy - 60, 0xFFFFD45A);
                g.centeredText(this.font, Component.translatable("screen.dbtenkaichi.training.score", data.getIntOr("score", 0)), cx, cy - 36, 0xFFFFFFFF);
                g.centeredText(this.font, Component.translatable("screen.dbtenkaichi.training.reward", String.format(java.util.Locale.ROOT, "%.1f", data.getFloatOr("reward", 0))), cx, cy - 22, 0xFF7CFFB0);
                if (data.getBooleanOr("aborted", false)) {
                    g.centeredText(this.font, Component.translatable("screen.dbtenkaichi.training.aborted"), cx, cy - 8, 0xFFFF7A7A);
                }
            }
            default -> {
                g.centeredText(this.font, Component.translatable("training.dbtenkaichi." + activity), cx, cy - 100, 0xFFFFD45A);
                float elapsed = ClientState.clientTicks - started + partial;
                int duration = data.getIntOr("duration", TrainingSystem.SESSION_TICKS);
                TenkaichiUi.gauge(g, cx - 120, cy + 44, 240, 6, Math.min(1.0f, elapsed / duration), 0.0f, 0xFF9FD8FF);
                if (TrainingSystem.MEDITATION.equals(activity)) {
                    meditation(g, cx, cy, elapsed);
                } else if (TrainingSystem.KI_CONTROL.equals(activity)) {
                    kiControl(g, cx, cy);
                }
            }
        }
    }

    private void meditation(GuiGraphicsExtractor g, int cx, int cy, float elapsed) {
        int period = data.getIntOr("period", TrainingSystem.MEDITATION_PERIOD);
        float phase = (elapsed % period) / period;
        int target = 18;
        int radius = (int) (target + (1.0f - phase) * 50);
        boolean window = phase < 0.1f || phase > 0.9f;
        ring(g, cx, cy - 20, target, 0xFFFFD45A);
        ring(g, cx, cy - 20, radius, window ? 0xFF7CFFB0 : 0xFFFFFFFF);
        g.centeredText(this.font, Component.translatable("screen.dbtenkaichi.training.meditation_hint"), cx, cy + 30, 0xFFBBBBBB);
        g.centeredText(this.font, Component.translatable("screen.dbtenkaichi.training.presses", presses), cx, cy - 86, 0xFFFFFFFF);
    }

    private void kiControl(GuiGraphicsExtractor g, int cx, int cy) {
        float ratio = Math.min(1.0f, ClientState.ki / Math.max(1.0f, ClientState.maxKi));
        float min = data.getFloatOr("band_min", 0.4f);
        float max = data.getFloatOr("band_max", 0.55f);
        int x0 = cx - 120;
        int w = 240;
        int y = cy - 30;
        TenkaichiUi.gauge(g, x0, y, w, 14, ratio, 0.0f, 0xFF3FA8FF);
        g.fill(x0 + (int) (w * min), y - 4, x0 + (int) (w * max), y + 18, 0x667CFFB0);
        g.outline(x0 + (int) (w * min), y - 4, Math.max(2, (int) (w * (max - min))), 22, 0xFF7CFFB0);
        g.centeredText(this.font, Component.translatable("screen.dbtenkaichi.training.ki_hint", Keybinds.CHARGE.getTranslatedKeyMessage()), cx, cy + 4, 0xFFBBBBBB);
        g.centeredText(this.font, Component.translatable("screen.dbtenkaichi.training.score", data.getIntOr("score", 0)), cx, cy - 86, 0xFFFFFFFF);
    }

    private void ring(GuiGraphicsExtractor g, int cx, int cy, int r, int color) {
        int steps = Math.max(24, r * 2);
        for (int i = 0; i < steps; i++) {
            double a = Math.PI * 2 * i / steps;
            int x = cx + (int) Math.round(Math.cos(a) * r);
            int y = cy + (int) Math.round(Math.sin(a) * r);
            g.fill(x, y, x + 2, y + 2, color);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
