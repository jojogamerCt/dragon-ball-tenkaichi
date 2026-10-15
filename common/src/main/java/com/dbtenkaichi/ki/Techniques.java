package com.dbtenkaichi.ki;

import com.dbtenkaichi.stats.StatType;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Techniques {
    private static final Map<String, Technique> ALL = new LinkedHashMap<>();

    public static final Technique KI_BLAST = add(new Technique("dbtenkaichi:ki_blast", Technique.Type.BLAST,
            3.0, 4.0, 0.0, 0, 0, 1.9, 0.28, 0.0, 48, 60, 0.9, 0.0, 0.08, false, false, false, 5,
            0xFFFFFF, 0x9FD8FF, StatType.KI_OUTPUT));

    public static final Technique KAMEHAMEHA = add(new Technique("dbtenkaichi:kamehameha", Technique.Type.BEAM,
            14.0, 25.0, 45.0, 10, 50, 2.4, 0.5, 0.75, 72, 40, 2.5, 2.5, 0.25, true, true, true, 40,
            0xF4FBFF, 0x3FA8FF, StatType.KI_OUTPUT));

    public static final Technique RAIDER_CANNON = add(new Technique("dbtenkaichi:raider_cannon", Technique.Type.BEAM,
            12.0, 30.0, 40.0, 12, 45, 2.2, 0.55, 0.6, 64, 36, 2.0, 2.0, 0.15, true, true, true, 60,
            0xFFF2FF, 0xB04BFF, StatType.KI_OUTPUT));

    public static final Technique GALICK_GUN = add(new Technique("dbtenkaichi:galick_gun", Technique.Type.BEAM,
            15.0, 28.0, 45.0, 10, 50, 2.4, 0.5, 0.75, 72, 40, 2.6, 2.6, 0.2, true, true, true, 40,
            0xFFF0FF, 0xA040FF, StatType.KI_OUTPUT));

    public static final Technique MASENKO = add(new Technique("dbtenkaichi:masenko", Technique.Type.BEAM,
            11.0, 18.0, 30.0, 8, 40, 2.6, 0.45, 0.6, 64, 32, 2.0, 2.0, 0.3, true, true, true, 30,
            0xFFFBE0, 0xFFD43B, StatType.KI_OUTPUT));

    public static final Technique SPECIAL_BEAM_CANNON = add(new Technique("dbtenkaichi:special_beam_cannon", Technique.Type.BEAM,
            22.0, 32.0, 40.0, 30, 80, 3.2, 0.2, 0.12, 90, 24, 1.2, 1.0, 0.05, true, false, false, 80,
            0xFFFFE6, 0xD070FF, StatType.KI_CONTROL));

    public static final Technique FINAL_FLASH = add(new Technique("dbtenkaichi:final_flash", Technique.Type.BEAM,
            26.0, 45.0, 70.0, 20, 70, 3.0, 1.1, 1.5, 110, 50, 5.5, 4.5, 0.15, true, true, true, 100,
            0xFFFFF0, 0xFFE640, StatType.KI_OUTPUT));

    public static final Technique BIG_BANG_ATTACK = add(new Technique("dbtenkaichi:big_bang_attack", Technique.Type.BLAST,
            18.0, 30.0, 0.0, 0, 0, 1.3, 0.9, 0.0, 64, 80, 7.0, 0.0, 0.02, false, false, false, 80,
            0xFFFFFF, 0x7FC8FF, StatType.KI_OUTPUT));

    public static final Technique DEATH_BEAM = add(new Technique("dbtenkaichi:death_beam", Technique.Type.BLAST,
            9.0, 12.0, 0.0, 0, 0, 3.6, 0.12, 0.0, 64, 40, 0.6, 0.0, 0.0, true, false, false, 25,
            0xFFE0FF, 0xFF5AD8, StatType.KI_CONTROL));

    public static final Technique DOUBLE_SUNDAY = add(new Technique("dbtenkaichi:double_sunday", Technique.Type.BEAM,
            13.0, 24.0, 36.0, 10, 45, 2.4, 0.45, 0.6, 70, 36, 2.2, 2.0, 0.2, true, true, true, 45,
            0xFFF0FF, 0xB04BFF, StatType.KI_OUTPUT, "beam", ""));

    public static final Technique TRI_BEAM = add(new Technique("dbtenkaichi:tri_beam", Technique.Type.BEAM,
            26.0, 30.0, 30.0, 15, 40, 3.0, 0.9, 0.8, 60, 24, 4.0, 3.0, 0.05, true, true, true, 120,
            0xFFFFF0, 0xFFF27A, StatType.KI_CONTROL, "beam", "health_cost"));

    public static final Technique HELLS_FLASH = add(new Technique("dbtenkaichi:hells_flash", Technique.Type.BEAM,
            20.0, 30.0, 50.0, 15, 60, 2.6, 0.75, 0.9, 80, 40, 3.5, 3.0, 0.15, true, true, true, 90,
            0xFFFFE8, 0xFFB43A, StatType.KI_OUTPUT, "beam", ""));

    public static final Technique INNOCENCE_CANNON = add(new Technique("dbtenkaichi:innocence_cannon", Technique.Type.BEAM,
            22.0, 30.0, 45.0, 10, 50, 2.6, 0.6, 0.8, 80, 36, 3.5, 3.0, 0.15, true, true, true, 80,
            0xFFF0F8, 0xFF70C0, StatType.KI_OUTPUT, "beam", ""));

    public static final Technique PERFECT_KAMEHAMEHA = add(new Technique("dbtenkaichi:perfect_kamehameha", Technique.Type.BEAM,
            18.0, 30.0, 50.0, 12, 55, 2.5, 0.6, 0.85, 80, 44, 3.0, 3.0, 0.2, true, true, true, 50,
            0xF4FBFF, 0x3FA8FF, StatType.KI_OUTPUT, "beam", ""));

    public static final Technique SPIRIT_BOMB = add(new Technique("dbtenkaichi:spirit_bomb", Technique.Type.SPHERE,
            34.0, 40.0, 120.0, 30, 120, 0.7, 1.2, 4.0, 80, 140, 7.0, 6.0, 0.04, false, false, false, 300,
            0xEAF6FF, 0x6FC8FF, StatType.KI_CONTROL, "sphere", "spirit_bomb"));

    public static final Technique DEATH_BALL = add(new Technique("dbtenkaichi:death_ball", Technique.Type.SPHERE,
            30.0, 40.0, 90.0, 25, 100, 0.6, 1.0, 3.5, 80, 140, 6.0, 6.0, 0.03, false, false, false, 280,
            0xFFE0A0, 0xFF6A1A, StatType.KI_OUTPUT, "sphere", "crater"));

    public static final Technique PLANET_BURST = add(new Technique("dbtenkaichi:planet_burst", Technique.Type.SPHERE,
            28.0, 40.0, 90.0, 25, 100, 0.7, 1.0, 3.2, 80, 140, 6.0, 5.0, 0.03, false, false, false, 280,
            0xFFE0F0, 0xFF60B0, StatType.KI_OUTPUT, "sphere", "crater"));

    public static final Technique LIGHT_GRENADE = add(new Technique("dbtenkaichi:light_grenade", Technique.Type.BLAST,
            16.0, 26.0, 0.0, 0, 0, 1.5, 0.8, 0.0, 64, 80, 4.5, 0.0, 0.03, false, false, false, 70,
            0xFFFFE0, 0xF4FF8A, StatType.KI_OUTPUT, "ball", ""));

    public static final Technique SPIRIT_BALL = add(new Technique("dbtenkaichi:spirit_ball", Technique.Type.BLAST,
            14.0, 18.0, 0.0, 0, 0, 1.4, 0.5, 0.0, 64, 100, 2.0, 0.0, 0.45, false, false, false, 60,
            0xFFFFFF, 0xFFE89A, StatType.KI_CONTROL, "ball", ""));

    public static final Technique DODON_RAY = add(new Technique("dbtenkaichi:dodon_ray", Technique.Type.BLAST,
            10.0, 12.0, 0.0, 0, 0, 3.4, 0.12, 0.0, 64, 30, 0.8, 0.0, 0.0, false, false, false, 25,
            0xFFFFE0, 0xFFE640, StatType.KI_CONTROL, "laser", ""));

    public static final Technique EYE_LASER = add(new Technique("dbtenkaichi:eye_laser", Technique.Type.BLAST,
            9.0, 8.0, 0.0, 0, 0, 3.8, 0.1, 0.0, 64, 30, 0.6, 0.0, 0.0, false, false, false, 20,
            0xFFE0E0, 0xFF3030, StatType.KI_CONTROL, "laser", ""));

    public static final Technique POWER_BLITZ = add(new Technique("dbtenkaichi:power_blitz", Technique.Type.BLAST,
            12.0, 14.0, 0.0, 0, 0, 2.0, 0.45, 0.0, 64, 60, 2.0, 0.0, 0.08, false, false, false, 40,
            0xFFFFFF, 0xFFD8F0, StatType.KI_OUTPUT, "ball", ""));

    public static final Technique EVIL_IMPULSE = add(new Technique("dbtenkaichi:evil_impulse", Technique.Type.BLAST,
            13.0, 16.0, 0.0, 0, 0, 2.0, 0.5, 0.0, 64, 60, 2.2, 0.0, 0.08, false, false, false, 45,
            0xF0E0FF, 0x9A2AE0, StatType.KI_OUTPUT, "ball", ""));

    public static final Technique VANISHING_BALL = add(new Technique("dbtenkaichi:vanishing_ball", Technique.Type.BLAST,
            18.0, 26.0, 0.0, 0, 0, 1.2, 1.0, 0.0, 64, 90, 4.5, 0.0, 0.03, false, false, false, 90,
            0xFFE8F4, 0xFF70C0, StatType.KI_OUTPUT, "ball", ""));

    public static final Technique DESTRUCTO_DISC = add(new Technique("dbtenkaichi:destructo_disc", Technique.Type.BLAST,
            20.0, 22.0, 0.0, 0, 0, 2.0, 0.9, 0.0, 64, 50, 0.0, 0.0, 0.05, true, false, false, 80,
            0xFFF8D0, 0xFFD24A, StatType.KI_CONTROL, "disc", "cut_tail"));

    public static final Technique DEATH_SAUCER = add(new Technique("dbtenkaichi:death_saucer", Technique.Type.BLAST,
            18.0, 22.0, 0.0, 0, 0, 1.8, 0.85, 0.0, 64, 70, 0.0, 0.0, 0.25, true, false, false, 90,
            0xFFE0F8, 0xFF5AD8, StatType.KI_CONTROL, "disc", "cut_tail"));

    public static final Technique HELLZONE_GRENADE = add(new Technique("dbtenkaichi:hellzone_grenade", Technique.Type.BARRAGE,
            5.0, 40.0, 0.0, 0, 0, 0.9, 0.35, 0.0, 64, 120, 1.6, 0.0, 0.35, false, false, false, 140,
            0xFFFFE0, 0xF4FF8A, StatType.KI_CONTROL, "hellzone", "10"));

    public static final Technique SCATTERING_BULLET = add(new Technique("dbtenkaichi:scattering_bullet", Technique.Type.BARRAGE,
            3.5, 30.0, 0.0, 0, 0, 1.4, 0.3, 0.0, 64, 90, 1.2, 0.0, 0.3, false, false, false, 120,
            0xFFFFFF, 0xFFE89A, StatType.KI_CONTROL, "scatter", "12"));

    public static final Technique HUMAN_EXTINCTION = add(new Technique("dbtenkaichi:human_extinction_attack", Technique.Type.BARRAGE,
            3.5, 50.0, 0.0, 0, 0, 1.5, 0.3, 0.0, 64, 120, 1.4, 0.0, 0.4, false, false, false, 200,
            0xFFE8F4, 0xFF70C0, StatType.KI_OUTPUT, "scatter", "20"));

    public static final Technique LIGHTNING_OF_ABSOLUTION = add(new Technique("dbtenkaichi:lightning_of_absolution", Technique.Type.BARRAGE,
            5.5, 45.0, 0.0, 0, 0, 1.6, 0.4, 0.0, 64, 120, 2.0, 0.0, 0.2, false, false, false, 180,
            0xFFE8FF, 0xC06AFF, StatType.KI_OUTPUT, "rain", "12"));

    public static final Technique SOLAR_FLARE = add(new Technique("dbtenkaichi:solar_flare", Technique.Type.SUPPORT,
            0.0, 20.0, 0.0, 0, 0, 0.0, 0.0, 0.0, 32, 0, 0.0, 0.0, 0.0, false, false, false, 200,
            0xFFFFFF, 0xFFFFF0, StatType.KI_CONTROL, "none", "solar_flare"));

    public static final Technique INSTANT_TRANSMISSION = add(new Technique("dbtenkaichi:instant_transmission", Technique.Type.SUPPORT,
            0.0, 25.0, 0.0, 0, 0, 0.0, 0.0, 0.0, 32, 0, 0.0, 0.0, 0.0, false, false, false, 60,
            0xFFFFFF, 0xE8F4FF, StatType.KI_CONTROL, "none", "teleport"));

    public static final Technique KAI_KAI = add(new Technique("dbtenkaichi:kai_kai", Technique.Type.SUPPORT,
            0.0, 20.0, 0.0, 0, 0, 0.0, 0.0, 0.0, 32, 0, 0.0, 0.0, 0.0, false, false, false, 60,
            0xFFFFFF, 0xF4E8FF, StatType.KI_CONTROL, "none", "teleport"));

    public static final Technique POWER_BALL = add(new Technique("dbtenkaichi:power_ball", Technique.Type.SUPPORT,
            0.0, 60.0, 0.0, 0, 0, 0.0, 0.0, 0.0, 32, 0, 0.0, 0.0, 0.0, false, false, false, 2400,
            0xFFFFFF, 0xF8F8FF, StatType.KI_OUTPUT, "none", "power_ball"));

    public static final Technique BOMBER_DX = add(new Technique("dbtenkaichi:bomber_dx", Technique.Type.SUPPORT,
            20.0, 30.0, 0.0, 0, 0, 0.0, 0.0, 0.0, 32, 0, 0.0, 0.0, 0.0, false, false, false, 100,
            0xFFFFFF, 0xFFD24A, StatType.KI_OUTPUT, "none", "eruption"));

    public static final Technique ABSORPTION = add(new Technique("dbtenkaichi:absorption", Technique.Type.SUPPORT,
            0.0, 20.0, 0.0, 0, 0, 0.0, 0.0, 0.0, 32, 0, 0.0, 0.0, 0.0, false, false, false, 200,
            0xFFFFFF, 0xE070C0, StatType.CONSTITUTION, "none", "absorb"));

    public static final Technique CANDY_BEAM = add(new Technique("dbtenkaichi:candy_beam", Technique.Type.SUPPORT,
            0.0, 18.0, 0.0, 0, 0, 0.0, 0.0, 0.0, 32, 0, 0.0, 0.0, 0.0, false, false, false, 160,
            0xFFFFFF, 0xFF90D0, StatType.KI_CONTROL, "none", "candy"));

    public static final Technique ENERGY_ABSORPTION = add(new Technique("dbtenkaichi:energy_absorption", Technique.Type.SUPPORT,
            0.0, 0.0, 0.0, 0, 0, 0.0, 0.0, 0.0, 32, 0, 0.0, 0.0, 0.0, false, false, false, 120,
            0xFFFFFF, 0x9FD8FF, StatType.KI_CONTROL, "none", "drain"));

    public static final Technique BARRIER = add(new Technique("dbtenkaichi:barrier", Technique.Type.SUPPORT,
            0.0, 30.0, 0.0, 0, 0, 0.0, 0.0, 0.0, 32, 0, 0.0, 0.0, 0.0, false, false, false, 260,
            0xFFFFFF, 0x9FE8FF, StatType.DEFENSE, "none", "barrier"));

    public static final Technique RESTORATION = add(new Technique("dbtenkaichi:restoration", Technique.Type.SUPPORT,
            0.0, 40.0, 0.0, 0, 0, 0.0, 0.0, 0.0, 32, 0, 0.0, 0.0, 0.0, false, false, false, 400,
            0xFFFFFF, 0xFFF6C0, StatType.SPIRIT, "none", "heal"));

    public static final Technique TELEKINESIS = add(new Technique("dbtenkaichi:telekinesis", Technique.Type.SUPPORT,
            0.0, 25.0, 0.0, 0, 0, 0.0, 0.0, 0.0, 32, 0, 0.0, 0.0, 0.0, false, false, false, 240,
            0xFFFFFF, 0xD8B0FF, StatType.KI_CONTROL, "none", "paralyze"));

    public static final Technique STONE_SPIT = add(new Technique("dbtenkaichi:stone_spit", Technique.Type.SUPPORT,
            0.0, 25.0, 0.0, 0, 0, 0.0, 0.0, 0.0, 32, 0, 0.0, 0.0, 0.0, false, false, false, 300,
            0xFFFFFF, 0xA8A8A0, StatType.KI_CONTROL, "none", "petrify"));

    public static final Technique CELL_JUNIOR = add(new Technique("dbtenkaichi:cell_junior", Technique.Type.SUPPORT,
            0.0, 60.0, 0.0, 0, 0, 0.0, 0.0, 0.0, 32, 0, 0.0, 0.0, 0.0, false, false, false, 1200,
            0xFFFFFF, 0x6FC8FF, StatType.KI_OUTPUT, "none", "summon"));

    public static final Technique WOLF_FANG_FIST = add(new Technique("dbtenkaichi:wolf_fang_fist", Technique.Type.SUPPORT,
            6.0, 20.0, 0.0, 0, 0, 0.0, 0.0, 0.0, 32, 0, 0.0, 0.0, 0.0, false, false, false, 120,
            0xFFFFFF, 0xBFE6FF, StatType.MARTIAL_ARTS, "none", "rush"));

    public static final Technique HAKAI = add(new Technique("dbtenkaichi:hakai", Technique.Type.SUPPORT,
            40.0, 80.0, 0.0, 0, 0, 0.0, 0.0, 0.0, 32, 0, 0.0, 0.0, 0.0, false, false, false, 600,
            0xFFFFFF, 0xA040FF, StatType.KI_OUTPUT, "none", "hakai"));

    public record Unlock(Technique technique, long battlePower, int price, java.util.Set<String> races, String requirement) {
        public Unlock(Technique technique, long battlePower, int price, java.util.Set<String> races) {
            this(technique, battlePower, price, races, "");
        }

        public boolean allows(com.dbtenkaichi.race.Race race) {
            return races.isEmpty() || races.contains(race.id());
        }
    }

    public static final java.util.List<Unlock> UNLOCKS = java.util.List.of(
            new Unlock(MASENKO, 3_000L, 250, java.util.Set.of("half_saiyan", "namekian")),
            new Unlock(DEATH_BEAM, 5_000L, 300, java.util.Set.of("frieza_race", "bio_android")),
            new Unlock(GALICK_GUN, 8_000L, 400, java.util.Set.of("saiyan")),
            new Unlock(SPECIAL_BEAM_CANNON, 10_000L, 450, java.util.Set.of("namekian", "half_saiyan")),
            new Unlock(BIG_BANG_ATTACK, 40_000L, 700, java.util.Set.of("saiyan", "half_saiyan", "android")),
            new Unlock(FINAL_FLASH, 120_000L, 1000, java.util.Set.of("saiyan", "bio_android")),
            new Unlock(DOUBLE_SUNDAY, 5_000L, 250, java.util.Set.of("saiyan")),
            new Unlock(BOMBER_DX, 12_000L, 350, java.util.Set.of("saiyan")),
            new Unlock(POWER_BALL, 60_000L, 600, java.util.Set.of("saiyan")),
            new Unlock(INSTANT_TRANSMISSION, 150_000L, 900, java.util.Set.of()),
            new Unlock(HAKAI, 1_200_000L, 3000, java.util.Set.of("saiyan"), "form:dbtenkaichi:ultra_ego"),
            new Unlock(SPIRIT_BOMB, 0L, 0, java.util.Set.of(), "mission:saiyan_saga_06_techniques"),
            new Unlock(WOLF_FANG_FIST, 2_000L, 150, java.util.Set.of("human")),
            new Unlock(DODON_RAY, 3_000L, 200, java.util.Set.of("human")),
            new Unlock(SOLAR_FLARE, 4_000L, 250, java.util.Set.of("human", "saiyan")),
            new Unlock(SPIRIT_BALL, 5_000L, 250, java.util.Set.of("human")),
            new Unlock(DESTRUCTO_DISC, 6_000L, 300, java.util.Set.of("human")),
            new Unlock(TELEKINESIS, 10_000L, 350, java.util.Set.of("human", "kaioshin")),
            new Unlock(SCATTERING_BULLET, 15_000L, 400, java.util.Set.of("human")),
            new Unlock(TRI_BEAM, 20_000L, 500, java.util.Set.of("human")),
            new Unlock(LIGHT_GRENADE, 8_000L, 300, java.util.Set.of("namekian")),
            new Unlock(HELLZONE_GRENADE, 40_000L, 700, java.util.Set.of("namekian")),
            new Unlock(DEATH_SAUCER, 15_000L, 400, java.util.Set.of("frieza_race")),
            new Unlock(DEATH_BALL, 120_000L, 1000, java.util.Set.of("frieza_race")),
            new Unlock(CANDY_BEAM, 8_000L, 350, java.util.Set.of("majin")),
            new Unlock(ABSORPTION, 20_000L, 500, java.util.Set.of("majin", "bio_android")),
            new Unlock(VANISHING_BALL, 30_000L, 500, java.util.Set.of("majin")),
            new Unlock(INNOCENCE_CANNON, 80_000L, 700, java.util.Set.of("majin")),
            new Unlock(HUMAN_EXTINCTION, 150_000L, 900, java.util.Set.of("majin")),
            new Unlock(PLANET_BURST, 300_000L, 1200, java.util.Set.of("majin")),
            new Unlock(EYE_LASER, 3_000L, 200, java.util.Set.of("android")),
            new Unlock(POWER_BLITZ, 5_000L, 250, java.util.Set.of("android")),
            new Unlock(ENERGY_ABSORPTION, 8_000L, 300, java.util.Set.of("android")),
            new Unlock(BARRIER, 25_000L, 500, java.util.Set.of("android", "bio_android")),
            new Unlock(HELLS_FLASH, 40_000L, 700, java.util.Set.of("android")),
            new Unlock(PERFECT_KAMEHAMEHA, 60_000L, 700, java.util.Set.of("bio_android")),
            new Unlock(CELL_JUNIOR, 250_000L, 1200, java.util.Set.of("bio_android")),
            new Unlock(RESTORATION, 20_000L, 500, java.util.Set.of("kaioshin")),
            new Unlock(KAI_KAI, 40_000L, 600, java.util.Set.of("kaioshin")),
            new Unlock(LIGHTNING_OF_ABSOLUTION, 500_000L, 1500, java.util.Set.of("kaioshin"), "form:dbtenkaichi:fused_zamasu"),
            new Unlock(EVIL_IMPULSE, 6_000L, 300, java.util.Set.of("demon")),
            new Unlock(STONE_SPIT, 30_000L, 600, java.util.Set.of("demon")));

    private Techniques() {
    }

    private static Technique add(Technique technique) {
        ALL.put(technique.id(), technique);
        return technique;
    }

    public static Technique get(String id) {
        return ALL.get(id);
    }

    public static Map<String, Technique> all() {
        return Map.copyOf(ALL);
    }
}
