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
