package com.dbtenkaichi.stats;

import java.util.Locale;

public enum StatType {
    STRENGTH,
    DEFENSE,
    CONSTITUTION,
    KI_CONTROL,
    KI_CAPACITY,
    KI_OUTPUT,
    SPEED,
    AGILITY,
    STAMINA,
    MARTIAL_ARTS,
    SPIRIT,
    POTENTIAL;

    private static final StatType[] VALUES = values();
    public static final int COUNT = VALUES.length;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "stat.dbtenkaichi." + id();
    }

    public static StatType byId(String id) {
        for (StatType type : VALUES) {
            if (type.id().equals(id)) {
                return type;
            }
        }
        return null;
    }

    public static StatType byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < COUNT ? VALUES[ordinal] : null;
    }

    public static StatType[] all() {
        return VALUES.clone();
    }
}
