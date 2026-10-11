package com.dbtenkaichi.combat;

public enum CombatFxKind {
    HIT_LIGHT,
    HIT_HEAVY,
    GUARD,
    PERFECT_GUARD,
    GUARD_BREAK,
    MELEE_CLASH,
    DODGE,
    VANISH,
    DASH,
    WHIFF,
    KI_IMPACT,
    EXPLOSION,
    SHOCKWAVE,
    AURA_BURST,
    LAND,
    SONIC_BOOM,
    ZENKAI,
    LEVEL_UP;

    private static final CombatFxKind[] VALUES = values();

    public static CombatFxKind byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : HIT_LIGHT;
    }
}
