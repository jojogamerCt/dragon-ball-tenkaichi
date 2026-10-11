package com.dbtenkaichi.combat;

import com.dbtenkaichi.anim.AnimClip;

public enum MeleeMove {
    JAB(2.0, 0.22, 0.0, 9, 0.0, AnimClip.PUNCH_LEFT, false),
    CROSS(2.2, 0.25, 0.0, 9, 0.0, AnimClip.PUNCH_RIGHT, false),
    HOOK(2.6, 0.32, 0.05, 10, 0.0, AnimClip.HOOK, false),
    KNEE(2.8, 0.3, 0.18, 11, 0.0, AnimClip.KNEE, false),
    ROUNDHOUSE(4.0, 1.45, 0.25, 16, 2.0, AnimClip.ROUNDHOUSE, true),
    LAUNCHER(5.0, 0.35, 1.15, 22, 10.0, AnimClip.UPPERCUT, true),
    SMASH(5.5, 0.6, -1.5, 26, 12.0, AnimClip.AXE_KICK, true),
    RUSH(4.5, 1.2, 0.2, 14, 6.0, AnimClip.RUSH, true),
    GUARD_BREAK(4.0, 0.9, 0.1, 18, 9.0, AnimClip.PALM_STRIKE, true),
    COUNTER(5.0, 1.3, 0.3, 18, 0.0, AnimClip.COUNTER, true);

    public static final MeleeMove[] LIGHT_CHAIN = {JAB, CROSS, HOOK, KNEE, ROUNDHOUSE};

    public final double damage;
    public final double knockback;
    public final double lift;
    public final int hitstun;
    public final double staminaCost;
    public final AnimClip clip;
    public final boolean heavy;

    MeleeMove(double damage, double knockback, double lift, int hitstun, double staminaCost, AnimClip clip, boolean heavy) {
        this.damage = damage;
        this.knockback = knockback;
        this.lift = lift;
        this.hitstun = hitstun;
        this.staminaCost = staminaCost;
        this.clip = clip;
        this.heavy = heavy;
    }
}
