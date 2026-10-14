package com.dbtenkaichi.ki;

public enum SkillStyle {
    KAMEHAMEHA(78.0f),
    GALICK_GUN(-72.0f),
    MASENKO(0.0f),
    SPECIAL_BEAM_CANNON(24.0f),
    FINAL_FLASH(0.0f),
    BIG_BANG(0.0f),
    DEATH_BEAM(0.0f),
    KI_BLAST(0.0f);

    public static final int SHIFT = 17;
    public static final int MASK = 15 << SHIFT;
    private static final SkillStyle[] VALUES = values();

    private final float turn;

    SkillStyle(float turn) {
        this.turn = turn;
    }

    public float turn() {
        return turn;
    }

    public static SkillStyle fromFlags(int flags) {
        int index = (flags & MASK) >> SHIFT;
        return index < VALUES.length ? VALUES[index] : KAMEHAMEHA;
    }

    public static SkillStyle of(String techniqueId) {
        if (techniqueId == null) {
            return KAMEHAMEHA;
        }
        return switch (techniqueId) {
            case "dbtenkaichi:galick_gun" -> GALICK_GUN;
            case "dbtenkaichi:masenko" -> MASENKO;
            case "dbtenkaichi:special_beam_cannon" -> SPECIAL_BEAM_CANNON;
            case "dbtenkaichi:final_flash" -> FINAL_FLASH;
            case "dbtenkaichi:big_bang_attack" -> BIG_BANG;
            case "dbtenkaichi:death_beam" -> DEATH_BEAM;
            case "dbtenkaichi:ki_blast" -> KI_BLAST;
            default -> KAMEHAMEHA;
        };
    }
}
