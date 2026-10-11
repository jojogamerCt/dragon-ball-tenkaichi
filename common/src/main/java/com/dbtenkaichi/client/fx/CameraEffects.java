package com.dbtenkaichi.client.fx;

import com.dbtenkaichi.client.ClientState;
import com.dbtenkaichi.client.EntityVisual;
import com.dbtenkaichi.config.ConfigManager;
import net.minecraft.client.Minecraft;

public final class CameraEffects {
    private static float shake;
    private static float shakeO;
    private static float seed;

    private CameraEffects() {
    }

    public static void addShake(float amount) {
        float scaled = amount * ConfigManager.client().cameraShake;
        shake = Math.min(3.0f, Math.max(shake, scaled));
    }

    public static void tick() {
        shakeO = shake;
        shake *= 0.8f;
        if (shake < 0.01f) {
            shake = 0.0f;
        }
        seed += 1.7f;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            EntityVisual visual = ClientState.existing(mc.player.getId());
            if (visual != null && visual.cinematicFull && ClientState.clientTicks - visual.cinematicStart < visual.cinematicTicks) {
                float progress = (float) (ClientState.clientTicks - visual.cinematicStart) / Math.max(1, visual.cinematicTicks);
                addShake(0.15f + progress * 0.6f);
            }
            if (visual != null && (visual.flags & com.dbtenkaichi.player.RuntimeState.FLAG_CHARGING) != 0) {
                addShake(0.06f);
            }
            if (ClientState.clashId >= 0 && (ClientState.clashFirst == mc.player.getId() || ClientState.clashSecond == mc.player.getId())) {
                addShake(0.25f);
            }
        }
    }

    public static float yawOffset(float partial) {
        float s = shakeO + (shake - shakeO) * partial;
        if (s <= 0.0f) {
            return 0.0f;
        }
        float t = seed + partial * 1.7f;
        return (float) (Math.sin(t * 2.3) + Math.sin(t * 5.1) * 0.5) * s * 0.9f;
    }

    public static float pitchOffset(float partial) {
        float s = shakeO + (shake - shakeO) * partial;
        if (s <= 0.0f) {
            return 0.0f;
        }
        float t = seed + partial * 1.7f;
        return (float) (Math.cos(t * 2.9) + Math.sin(t * 6.3) * 0.5) * s * 0.7f;
    }
}
