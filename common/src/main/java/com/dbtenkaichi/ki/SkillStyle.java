package com.dbtenkaichi.ki;

public enum SkillStyle {
    KAMEHAMEHA(78.0f),
    GALICK_GUN(-72.0f),
    MASENKO(0.0f),
    SPECIAL_BEAM_CANNON(24.0f),
    FINAL_FLASH(0.0f),
    BIG_BANG(0.0f),
    DEATH_BEAM(0.0f),
    KI_BLAST(0.0f),
    DISC(0.0f),
    RAISED(0.0f),
    FACE(0.0f),
    TRIANGLE(0.0f),
    GRAB(0.0f),
    BARRIER(0.0f);

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
        int colon = techniqueId.indexOf(':');
        String path = colon >= 0 ? techniqueId.substring(colon + 1) : techniqueId;
        return switch (path) {
            case "galick_gun", "double_sunday" -> GALICK_GUN;
            case "masenko" -> MASENKO;
            case "special_beam_cannon", "instant_transmission", "kai_kai" -> SPECIAL_BEAM_CANNON;
            case "final_flash", "hells_flash" -> FINAL_FLASH;
            case "big_bang_attack", "light_grenade", "vanishing_ball", "hakai", "power_blitz", "evil_impulse" -> BIG_BANG;
            case "death_beam", "dodon_ray", "eye_laser", "spirit_ball", "bomber_dx" -> DEATH_BEAM;
            case "ki_blast", "scattering_bullet", "human_extinction_attack" -> KI_BLAST;
            case "destructo_disc", "death_saucer", "power_ball" -> DISC;
            case "spirit_bomb", "death_ball", "planet_burst", "lightning_of_absolution", "hellzone_grenade" -> RAISED;
            case "solar_flare", "stone_spit", "candy_beam" -> FACE;
            case "tri_beam" -> TRIANGLE;
            case "absorption", "energy_absorption", "telekinesis", "restoration", "cell_junior", "wolf_fang_fist" -> GRAB;
            case "barrier" -> BARRIER;
            default -> KAMEHAMEHA;
        };
    }
}
