package com.dbtenkaichi.client.anim;

import com.dbtenkaichi.anim.AnimClip;

import java.util.EnumMap;
import java.util.Map;

public final class Clips {
    private static final Map<AnimClip, Frame[]> CLIPS = new EnumMap<>(AnimClip.class);
    private static final Map<AnimPlayer.BaseState, LimbPose> BASE = new EnumMap<>(AnimPlayer.BaseState.class);

    static {
        put(AnimClip.PUNCH_LEFT,
                f(0.0f, p().leftArm(-0.7f, 0.0f, -0.1f).body(0, 0.18f, 0)),
                f(0.3f, p().leftArm(-1.68f, 0.12f, 0.0f).body(0, -0.38f, 0)),
                f(0.65f, p().leftArm(-1.55f, 0.05f, 0.0f).body(0, -0.3f, 0)),
                f(1.0f, p().leftArm(-1.0f, 0.2f, -0.15f).body(0, 0, 0)));
        put(AnimClip.PUNCH_RIGHT,
                f(0.0f, p().rightArm(-0.7f, 0.0f, 0.1f).body(0, -0.18f, 0)),
                f(0.3f, p().rightArm(-1.68f, -0.12f, 0.0f).body(0, 0.38f, 0)),
                f(0.65f, p().rightArm(-1.55f, -0.05f, 0.0f).body(0, 0.3f, 0)),
                f(1.0f, p().rightArm(-1.0f, -0.2f, 0.15f).body(0, 0, 0)));
        put(AnimClip.HOOK,
                f(0.0f, p().rightArm(-1.1f, 0.4f, 0.95f).body(0, -0.3f, 0)),
                f(0.35f, p().rightArm(-1.55f, -0.55f, 0.35f).body(0, 0.55f, 0)),
                f(0.7f, p().rightArm(-1.45f, -0.75f, 0.25f).body(0, 0.6f, 0)),
                f(1.0f, p().rightArm(-1.0f, -0.2f, 0.2f).body(0, 0.1f, 0)));
        put(AnimClip.KNEE,
                f(0.0f, p().rightLeg(-0.3f, 0, 0).leftArm(-1.1f, 0.2f, -0.2f).rightArm(-1.1f, -0.2f, 0.2f)),
                f(0.35f, p().rightLeg(-1.55f, 0, 0.05f).leftArm(-0.7f, 0.0f, -0.4f).rightArm(-0.7f, 0.0f, 0.4f).body(0.12f, 0, 0)),
                f(0.7f, p().rightLeg(-1.4f, 0, 0.05f).body(0.1f, 0, 0)),
                f(1.0f, p().rightLeg(-0.2f, 0, 0).body(0, 0, 0)));
        put(AnimClip.ROUNDHOUSE,
                f(0.0f, p().rightLeg(-0.3f, 0, 0.3f).body(0, -0.3f, 0)),
                f(0.25f, p().rightLeg(-1.0f, 0.3f, 1.0f).body(0, 0.35f, -0.15f).leftArm(-0.3f, 0, -0.9f).rightArm(0.2f, 0, 0.6f)),
                f(0.5f, p().rightLeg(-1.45f, 0.6f, 1.45f).body(0, 0.7f, -0.25f).leftArm(-0.2f, 0, -1.1f).rightArm(0.3f, 0, 0.8f)),
                f(0.75f, p().rightLeg(-1.1f, 0.7f, 1.2f).body(0, 0.8f, -0.2f)),
                f(1.0f, p().rightLeg(-0.2f, 0, 0.1f).body(0, 0, 0)));
        put(AnimClip.UPPERCUT,
                f(0.0f, p().rightArm(0.3f, 0, 0.3f).body(0.2f, -0.2f, 0).rightLeg(0.3f, 0, 0).leftLeg(-0.3f, 0, 0)),
                f(0.35f, p().rightArm(-2.7f, -0.1f, 0.1f).body(-0.25f, 0.35f, 0).head(-0.35f, 0, 0)),
                f(0.7f, p().rightArm(-2.9f, -0.1f, 0.05f).body(-0.2f, 0.3f, 0).head(-0.4f, 0, 0)),
                f(1.0f, p().rightArm(-1.1f, -0.2f, 0.2f).body(0, 0, 0).head(0, 0, 0)));
        put(AnimClip.AXE_KICK,
                f(0.0f, p().rightLeg(-2.6f, 0, 0.1f).leftArm(-0.4f, 0, -0.9f).rightArm(-0.4f, 0, 0.9f).body(-0.2f, 0, 0)),
                f(0.4f, p().rightLeg(-2.8f, 0, 0.1f).body(-0.25f, 0, 0)),
                f(0.6f, p().rightLeg(0.25f, 0, 0.0f).body(0.35f, 0, 0).head(0.3f, 0, 0)),
                f(1.0f, p().rightLeg(0.0f, 0, 0).body(0, 0, 0).head(0, 0, 0)));
        put(AnimClip.RUSH,
                f(0.0f, p().rightArm(0.6f, 0, 0.3f).leftArm(-1.1f, 0.2f, -0.2f).body(0.2f, -0.3f, 0)),
                f(0.35f, p().rightArm(-1.75f, -0.05f, 0).leftArm(0.5f, 0, -0.3f).body(0.3f, 0.45f, 0).rightLeg(0.6f, 0, 0).leftLeg(-0.6f, 0, 0)),
                f(0.8f, p().rightArm(-1.65f, 0, 0).body(0.25f, 0.4f, 0)),
                f(1.0f, p().rightArm(-1.0f, -0.2f, 0.2f).body(0, 0, 0)));
        put(AnimClip.PALM_STRIKE,
                f(0.0f, p().rightArm(-0.6f, 0.3f, 0.2f).leftArm(-0.6f, -0.3f, -0.2f).body(-0.1f, 0, 0)),
                f(0.35f, p().rightArm(-1.62f, 0.18f, 0).leftArm(-1.62f, -0.18f, 0).body(0.15f, 0, 0)),
                f(0.75f, p().rightArm(-1.55f, 0.18f, 0).leftArm(-1.55f, -0.18f, 0)),
                f(1.0f, p().rightArm(-1.0f, -0.2f, 0.2f).leftArm(-1.0f, 0.2f, -0.2f).body(0, 0, 0)));
        put(AnimClip.COUNTER,
                f(0.0f, p().rightArm(-1.6f, -0.9f, 1.0f).body(0, -0.5f, 0)),
                f(0.35f, p().rightArm(-1.9f, 0.6f, 0.9f).body(0, 0.6f, 0)),
                f(1.0f, p().rightArm(-1.0f, -0.2f, 0.2f).body(0, 0, 0)));
        put(AnimClip.GUARD_IMPACT,
                f(0.0f, p().rightArm(-1.75f, -0.65f, 0.1f).leftArm(-1.75f, 0.65f, -0.1f).head(0.25f, 0, 0).body(0.1f, 0, 0)),
                f(0.4f, p().rightArm(-1.3f, -0.55f, 0.15f).leftArm(-1.3f, 0.55f, -0.15f).head(0.1f, 0, 0).body(-0.12f, 0, 0)),
                f(1.0f, p().rightArm(-1.6f, -0.65f, 0.1f).leftArm(-1.6f, 0.65f, -0.1f).head(0.15f, 0, 0).body(0, 0, 0)));
        put(AnimClip.HIT_LIGHT,
                f(0.0f, p().head(-0.25f, 0.25f, 0).body(0, 0.2f, 0)),
                f(0.3f, p().head(-0.45f, 0.35f, 0.1f).body(-0.1f, 0.3f, 0).rightArm(0.2f, 0, 0.35f).leftArm(0.1f, 0, -0.3f)),
                f(1.0f, p().head(0, 0, 0).body(0, 0, 0)));
        put(AnimClip.HIT_HEAVY,
                f(0.0f, p().head(-0.6f, 0, 0).body(-0.2f, 0, 0).rightArm(0.4f, 0, 0.7f).leftArm(0.4f, 0, -0.7f)),
                f(0.4f, p().head(-0.7f, 0, 0.1f).body(-0.3f, 0, 0).rightArm(0.6f, 0, 0.9f).leftArm(0.6f, 0, -0.9f).rightLeg(-0.5f, 0, 0.1f).leftLeg(0.2f, 0, -0.1f)),
                f(1.0f, p().head(0, 0, 0).body(0, 0, 0)));
        put(AnimClip.LAUNCHED,
                f(0.0f, p().head(-0.8f, 0, 0).rightArm(-0.5f, 0, 1.2f).leftArm(-0.5f, 0, -1.2f).rightLeg(-0.4f, 0, 0.2f).leftLeg(0.3f, 0, -0.2f)),
                f(0.5f, p().head(-0.5f, 0, 0).rightArm(-1.0f, 0, 1.4f).leftArm(-0.8f, 0, -1.4f).rightLeg(-0.7f, 0, 0.3f).leftLeg(-0.2f, 0, -0.3f)),
                f(1.0f, p().head(0, 0, 0)));
        put(AnimClip.KNOCKDOWN,
                f(0.0f, p().rightArm(-0.2f, 0, 1.3f).leftArm(-0.2f, 0, -1.3f).rightLeg(0.0f, 0, 0.35f).leftLeg(0.0f, 0, -0.35f).head(-0.3f, 0, 0)),
                f(0.8f, p().rightArm(-0.6f, 0, 0.6f).leftArm(-0.6f, 0, -0.6f).rightLeg(-0.6f, 0, 0.2f).leftLeg(-0.3f, 0, -0.2f).head(0.1f, 0, 0)),
                f(1.0f, p().head(0, 0, 0)));
        put(AnimClip.DODGE_LEFT,
                f(0.0f, p().body(0, 0.4f, 0.25f).rightArm(-0.4f, 0, 0.9f).leftArm(-0.6f, 0, -0.5f).rightLeg(0, 0, 0.35f).leftLeg(0, 0, 0.1f)),
                f(1.0f, p().body(0, 0, 0)));
        put(AnimClip.DODGE_RIGHT,
                f(0.0f, p().body(0, -0.4f, -0.25f).leftArm(-0.4f, 0, -0.9f).rightArm(-0.6f, 0, 0.5f).leftLeg(0, 0, -0.35f).rightLeg(0, 0, -0.1f)),
                f(1.0f, p().body(0, 0, 0)));
        put(AnimClip.DODGE_BACK,
                f(0.0f, p().body(-0.25f, 0, 0).head(0.2f, 0, 0).rightArm(-1.2f, -0.5f, 0.2f).leftArm(-1.2f, 0.5f, -0.2f).rightLeg(-0.5f, 0, 0).leftLeg(0.2f, 0, 0)),
                f(1.0f, p().body(0, 0, 0).head(0, 0, 0)));
        put(AnimClip.DASH,
                f(0.0f, p().rightArm(0.7f, 0, 0.25f).leftArm(0.7f, 0, -0.25f).rightLeg(0.45f, 0, 0).leftLeg(0.2f, 0, 0)),
                f(1.0f, p().rightArm(0.5f, 0, 0.2f).leftArm(0.5f, 0, -0.2f)));
        put(AnimClip.VANISH,
                f(0.0f, p().rightArm(-0.3f, 0, 0.1f).leftArm(-0.3f, 0, -0.1f)),
                f(1.0f, p().rightArm(-1.0f, -0.2f, 0.2f).leftArm(-1.0f, 0.2f, -0.2f)));
        put(AnimClip.KI_BLAST_LEFT,
                f(0.0f, p().leftArm(-1.0f, 0.3f, -0.2f).body(0, 0.15f, 0)),
                f(0.3f, p().leftArm(-1.65f, 0.05f, 0).body(0, -0.25f, 0)),
                f(1.0f, p().leftArm(-1.1f, 0.2f, -0.15f).body(0, 0, 0)));
        put(AnimClip.KI_BLAST_RIGHT,
                f(0.0f, p().rightArm(-1.0f, -0.3f, 0.2f).body(0, -0.15f, 0)),
                f(0.3f, p().rightArm(-1.65f, -0.05f, 0).body(0, 0.25f, 0)),
                f(1.0f, p().rightArm(-1.1f, -0.2f, 0.15f).body(0, 0, 0)));
        put(AnimClip.BEAM_FIRE,
                f(0.0f, p().rightArm(-1.58f, -0.18f, 0).leftArm(-1.58f, 0.18f, 0).body(0, 0, 0).rightLeg(0.35f, 0, 0.15f).leftLeg(-0.45f, 0, -0.1f)),
                f(1.0f, p().rightArm(-1.55f, -0.15f, 0).leftArm(-1.55f, 0.15f, 0).rightLeg(0.35f, 0, 0.15f).leftLeg(-0.45f, 0, -0.1f)));
        put(AnimClip.TRANSFORM,
                f(0.0f, p().rightArm(0.25f, 0, 0.55f).leftArm(0.25f, 0, -0.55f).head(0.3f, 0, 0).rightLeg(0, 0, 0.25f).leftLeg(0, 0, -0.25f)),
                f(0.55f, p().rightArm(0.35f, 0, 0.75f).leftArm(0.35f, 0, -0.75f).head(-0.7f, 0, 0).body(-0.15f, 0, 0).rightLeg(0, 0, 0.32f).leftLeg(0, 0, -0.32f)),
                f(0.85f, p().rightArm(-0.6f, 0, 1.1f).leftArm(-0.6f, 0, -1.1f).head(-0.45f, 0, 0).body(-0.2f, 0, 0)),
                f(1.0f, p().rightArm(0.1f, 0, 0.4f).leftArm(0.1f, 0, -0.4f).head(0, 0, 0).body(0, 0, 0)));
        put(AnimClip.POWER_UP,
                f(0.0f, p().rightArm(-0.3f, 0, 0.9f).leftArm(-0.3f, 0, -0.9f).head(0.2f, 0, 0)),
                f(0.6f, p().rightArm(0.3f, 0, 0.6f).leftArm(0.3f, 0, -0.6f).head(-0.3f, 0, 0)),
                f(1.0f, p().head(0, 0, 0)));
        put(AnimClip.LAND,
                f(0.0f, p().rightLeg(-0.7f, 0, 0.15f).leftLeg(-0.45f, 0, -0.15f).rightArm(-0.5f, 0, 0.8f).leftArm(-0.5f, 0, -0.8f).body(0.3f, 0, 0)),
                f(1.0f, p().rightLeg(0, 0, 0).leftLeg(0, 0, 0).body(0, 0, 0)));
        put(AnimClip.MEDITATE,
                f(0.0f, meditate()),
                f(1.0f, meditate()));
        put(AnimClip.CLASH_PUSH,
                f(0.0f, p().rightArm(-1.62f, -0.15f, 0).leftArm(-1.62f, 0.15f, 0).head(0.25f, 0, 0).rightLeg(0.5f, 0, 0.15f).leftLeg(-0.6f, 0, -0.1f)),
                f(0.5f, p().rightArm(-1.5f, -0.2f, 0.05f).leftArm(-1.5f, 0.2f, -0.05f).head(0.35f, 0, 0)),
                f(1.0f, p().rightArm(-1.6f, -0.15f, 0).leftArm(-1.6f, 0.15f, 0).head(0.25f, 0, 0)));

        BASE.put(AnimPlayer.BaseState.STANCE, p().rightArm(-1.05f, -0.35f, 0.25f).leftArm(-1.25f, 0.35f, -0.25f));
        BASE.put(AnimPlayer.BaseState.HOVER, p().rightArm(0.12f, 0, 0.28f).leftArm(0.12f, 0, -0.28f).rightLeg(-0.3f, 0, 0.08f).leftLeg(-0.05f, 0, -0.08f));
        BASE.put(AnimPlayer.BaseState.FLY, p().rightArm(0.45f, 0, 0.22f).leftArm(0.45f, 0, -0.22f).rightLeg(0.15f, 0, 0.04f).leftLeg(0.05f, 0, -0.04f));
        BASE.put(AnimPlayer.BaseState.FLY_FAST, p().rightArm(0.28f, 0, 0.12f).leftArm(0.28f, 0, -0.12f).rightLeg(0.06f, 0, 0.02f).leftLeg(0.06f, 0, -0.02f));
        BASE.put(AnimPlayer.BaseState.CHARGE, p().rightArm(0.18f, 0, 0.6f).leftArm(0.18f, 0, -0.6f).rightLeg(0, 0, 0.26f).leftLeg(0, 0, -0.26f).head(0.25f, 0, 0));
        BASE.put(AnimPlayer.BaseState.GUARD, p().rightArm(-1.62f, -0.68f, 0.1f).leftArm(-1.62f, 0.68f, -0.1f).head(0.18f, 0, 0));
        BASE.put(AnimPlayer.BaseState.SKILL_CHARGE, p().body(0.18f, 0.0f, 0.0f).rightArm(-0.28f, 0.0f, -0.22f).leftArm(-0.36f, 0.0f, 0.9f).rightLeg(0.12f, 0.0f, 0.38f).leftLeg(-0.12f, 0.0f, -0.38f));
        BASE.put(AnimPlayer.BaseState.BEAM_FIRE, p().rightArm(-1.58f, -0.16f, 0).leftArm(-1.58f, 0.16f, 0).rightLeg(0.35f, 0, 0.15f).leftLeg(-0.45f, 0, -0.1f).body(0, 0, 0));
        BASE.put(AnimPlayer.BaseState.STUNNED, p().head(-0.25f, 0, 0.12f).rightArm(0.25f, 0, 0.35f).leftArm(0.2f, 0, -0.35f));
        BASE.put(AnimPlayer.BaseState.TRANSFORM, p().rightArm(0.32f, 0, 0.62f).leftArm(0.32f, 0, -0.62f).head(-0.55f, 0, 0).rightLeg(0, 0, 0.3f).leftLeg(0, 0, -0.3f));
        BASE.put(AnimPlayer.BaseState.MEDITATE, meditate());
    }

    private Clips() {
    }

    private static LimbPose meditate() {
        return p().rightLeg(-1.45f, 0.45f, 0.25f).leftLeg(-1.45f, -0.45f, -0.25f).rightArm(-0.55f, 0.25f, 0.05f).leftArm(-0.55f, -0.25f, -0.05f).head(0.15f, 0, 0);
    }

    private static LimbPose p() {
        return new LimbPose();
    }

    private static Frame f(float t, LimbPose pose) {
        return new Frame(t, pose);
    }

    private static void put(AnimClip clip, Frame... frames) {
        CLIPS.put(clip, frames);
    }

    public static LimbPose base(AnimPlayer.BaseState state) {
        return BASE.get(state);
    }

    public static LimbPose sample(AnimClip clip, float progress) {
        Frame[] frames = CLIPS.get(clip);
        if (frames == null || frames.length == 0) {
            return null;
        }
        if (progress <= frames[0].t) {
            return frames[0].pose;
        }
        for (int i = 1; i < frames.length; i++) {
            Frame b = frames[i];
            if (progress <= b.t) {
                Frame a = frames[i - 1];
                float local = (progress - a.t) / Math.max(1.0e-4f, b.t - a.t);
                float eased = local * local * (3.0f - 2.0f * local);
                return LimbPose.lerp(a.pose, b.pose, eased);
            }
        }
        return frames[frames.length - 1].pose;
    }

    private record Frame(float t, LimbPose pose) {
    }
}
