package com.dbtenkaichi.client.anim;

import com.dbtenkaichi.anim.AnimClip;

public final class AnimPlayer {
    public static final int BASE_STATES = BaseState.values().length;

    public AnimClip clip = AnimClip.NONE;
    public float start;
    public int duration = 1;
    public int variant;
    public final float[] weights = new float[BASE_STATES];
    public final float[] weightsO = new float[BASE_STATES];
    public float frozenUntil;
    public float frozenAt;
    private float offset;

    public void play(AnimClip clip, int duration, int variant, float now) {
        this.clip = clip;
        this.start = effectiveTime(now);
        this.duration = Math.max(1, duration);
        this.variant = variant;
    }

    public float progress(float now) {
        float t = effectiveTime(now);
        return Math.max(0.0f, Math.min(1.0f, (t - start) / duration));
    }

    public boolean active(float now) {
        return clip != AnimClip.NONE && effectiveTime(now) - start < duration;
    }

    public float effectiveTime(float now) {
        if (now < frozenUntil) {
            return frozenAt;
        }
        return now - offset;
    }

    public void freeze(float now, float ticks) {
        if (now < frozenUntil) {
            return;
        }
        frozenAt = effectiveTime(now);
        frozenUntil = now + ticks;
        offset += ticks;
    }

    public float envelope(float now) {
        if (!active(now)) {
            return 0.0f;
        }
        float t = effectiveTime(now) - start;
        float in = Math.min(1.0f, t / 1.5f);
        float out = Math.min(1.0f, (duration - t) / 3.0f);
        return Math.max(0.0f, Math.min(in, out));
    }

    public void tickWeights(boolean[] activeStates) {
        for (int i = 0; i < BASE_STATES; i++) {
            weightsO[i] = weights[i];
            float target = activeStates[i] ? 1.0f : 0.0f;
            weights[i] += (target - weights[i]) * 0.35f;
            if (Math.abs(weights[i] - target) < 0.01f) {
                weights[i] = target;
            }
        }
    }

    public float weight(BaseState state, float partial) {
        int i = state.ordinal();
        return weightsO[i] + (weights[i] - weightsO[i]) * partial;
    }

    public enum BaseState {
        STANCE,
        HOVER,
        FLY,
        FLY_FAST,
        CHARGE,
        GUARD,
        SKILL_CHARGE,
        BEAM_FIRE,
        STUNNED,
        TRANSFORM,
        MEDITATE
    }
}
