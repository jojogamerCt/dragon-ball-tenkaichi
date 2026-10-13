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
            24.0, 45.0, 70.0, 20, 70, 2.8, 0.7, 1.0, 90, 44, 3.5, 3.0, 0.15, true, true, true, 100,
            0xFFFFF0, 0xFFE640, StatType.KI_OUTPUT));

    public static final Technique BIG_BANG_ATTACK = add(new Technique("dbtenkaichi:big_bang_attack", Technique.Type.BLAST,
            18.0, 30.0, 0.0, 0, 0, 1.3, 0.75, 0.0, 64, 80, 4.5, 0.0, 0.02, false, false, false, 80,
            0xFFFFFF, 0x7FC8FF, StatType.KI_OUTPUT));

    public static final Technique DEATH_BEAM = add(new Technique("dbtenkaichi:death_beam", Technique.Type.BLAST,
            9.0, 12.0, 0.0, 0, 0, 3.6, 0.12, 0.0, 64, 40, 0.6, 0.0, 0.0, true, false, false, 25,
            0xFFE0FF, 0xFF5AD8, StatType.KI_CONTROL));

    public record Unlock(Technique technique, long battlePower, int price, java.util.Set<String> races) {
        public boolean allows(com.dbtenkaichi.race.Race race) {
            return races.isEmpty() || races.contains(race.id());
        }
    }

    public static final java.util.List<Unlock> UNLOCKS = java.util.List.of(
            new Unlock(MASENKO, 3_000L, 250, java.util.Set.of("half_saiyan", "namekian")),
            new Unlock(DEATH_BEAM, 5_000L, 300, java.util.Set.of("frieza_race", "bio_android")),
            new Unlock(GALICK_GUN, 8_000L, 400, java.util.Set.of("saiyan")),
            new Unlock(SPECIAL_BEAM_CANNON, 10_000L, 450, java.util.Set.of("namekian")),
            new Unlock(BIG_BANG_ATTACK, 40_000L, 700, java.util.Set.of("saiyan", "half_saiyan", "android")),
            new Unlock(FINAL_FLASH, 120_000L, 1000, java.util.Set.of("saiyan", "bio_android")));

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
