package com.dbtenkaichi.client.anim;

import com.dbtenkaichi.anim.AnimClip;
import com.dbtenkaichi.client.ClientState;
import com.dbtenkaichi.client.fx.CameraEffects;
import com.dbtenkaichi.client.input.ClientController;
import com.dbtenkaichi.player.RuntimeState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public final class FirstPersonCombat {
    private static final Strike LEFT = new Strike();
    private static final Strike RIGHT = new Strike();
    private static long lastStrike = -1000;

    private FirstPersonCombat() {
    }

    private static final class Strike {
        long start = -1000;
        int duration = 6;
        float power = 1.0f;
        boolean upper;
    }

    public static void onLocalAnim(AnimClip clip, int duration) {
        switch (clip) {
            case PUNCH_LEFT, HOOK, KI_BLAST_LEFT -> strike(LEFT, duration, clip == AnimClip.HOOK ? 1.15f : 1.0f, false);
            case PUNCH_RIGHT, KI_BLAST_RIGHT -> strike(RIGHT, duration, 1.0f, false);
            case UPPERCUT -> strike(RIGHT, duration, 1.3f, true);
            case PALM_STRIKE, COUNTER, RUSH -> {
                strike(RIGHT, duration, 1.4f, false);
                strike(LEFT, duration, 0.6f, false);
            }
            case BEAM_FIRE, CLASH_PUSH -> {
                strike(LEFT, Math.max(8, duration), 1.1f, false);
                strike(RIGHT, Math.max(8, duration), 1.1f, false);
            }
            case KNEE, ROUNDHOUSE, AXE_KICK -> {
                lastStrike = ClientState.clientTicks;
                CameraEffects.addShake(clip == AnimClip.KNEE ? 0.12f : 0.22f);
            }
            default -> {
            }
        }
    }

    private static void strike(Strike strike, int duration, float power, boolean upper) {
        strike.start = ClientState.clientTicks;
        strike.duration = Math.max(4, Math.min(14, duration));
        strike.power = power;
        strike.upper = upper;
        lastStrike = ClientState.clientTicks;
    }

    public static boolean active() {
        return ClientController.inStance() || ClientState.clientTicks - lastStrike < 30;
    }

    private static float progress(Strike strike, float partial) {
        float t = (ClientState.clientTicks - strike.start + partial) / strike.duration;
        return t >= 0.0f && t < 1.0f ? t : -1.0f;
    }

    public static void transform(PoseStack pose, HumanoidArm arm, float partial) {
        float invert = arm == HumanoidArm.RIGHT ? 1.0f : -1.0f;
        float bob = Mth.sin((ClientState.clientTicks + partial) * 0.12f + (invert > 0 ? 0.0f : 1.7f)) * 0.012f;
        if (ClientState.localHas(RuntimeState.FLAG_GUARDING)) {
            pose.translate(-invert * 0.3f, 0.3f + bob, 0.05f);
            pose.rotateDegrees(Axis.ZP, invert * 35.0f);
            pose.rotateDegrees(Axis.YP, -invert * 20.0f);
            return;
        }
        pose.translate(-invert * 0.14f, 0.16f + bob, 0.1f);
        pose.rotateDegrees(Axis.YP, -invert * 10.0f);
        Strike strike = arm == HumanoidArm.RIGHT ? RIGHT : LEFT;
        float t = progress(strike, partial);
        if (t < 0.0f) {
            return;
        }
        float out = t < 0.35f ? t / 0.35f : 1.0f - (t - 0.35f) / 0.65f;
        float ext = Mth.sin(out * Mth.HALF_PI) * strike.power;
        if (strike.upper) {
            pose.translate(-invert * 0.1f * ext, 0.28f * ext, -0.3f * ext);
            pose.rotateDegrees(Axis.XP, -25.0f * ext);
        } else {
            pose.translate(-invert * 0.2f * ext, 0.06f * ext, -0.6f * ext);
            pose.rotateDegrees(Axis.YP, -invert * 12.0f * ext);
        }
    }
}
