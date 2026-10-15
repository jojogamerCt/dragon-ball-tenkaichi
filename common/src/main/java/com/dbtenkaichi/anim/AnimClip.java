package com.dbtenkaichi.anim;

public enum AnimClip {
    NONE(0),
    PUNCH_LEFT(6),
    PUNCH_RIGHT(6),
    HOOK(7),
    KNEE(7),
    ROUNDHOUSE(10),
    UPPERCUT(10),
    AXE_KICK(11),
    RUSH(9),
    PALM_STRIKE(9),
    COUNTER(9),
    GUARD_IMPACT(6),
    HIT_LIGHT(7),
    HIT_HEAVY(12),
    LAUNCHED(16),
    KNOCKDOWN(24),
    DODGE_LEFT(7),
    DODGE_RIGHT(7),
    DODGE_BACK(7),
    DASH(8),
    VANISH(5),
    KI_BLAST_LEFT(6),
    KI_BLAST_RIGHT(6),
    BEAM_FIRE(20),
    TRANSFORM(40),
    POWER_UP(20),
    LAND(8),
    MEDITATE(40),
    CLASH_PUSH(10),
    BIG_BANG(14),
    DEATH_BEAM(12),
    DISC_THROW(12),
    SOLAR_FLARE(14),
    BARRIER_CAST(16),
    RAISE_THROW(14),
    GRAB(12);

    private static final AnimClip[] VALUES = values();

    public final int defaultDuration;

    AnimClip(int defaultDuration) {
        this.defaultDuration = defaultDuration;
    }

    public static AnimClip byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : NONE;
    }
}
