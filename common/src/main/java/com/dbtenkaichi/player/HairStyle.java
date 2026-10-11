package com.dbtenkaichi.player;

import java.util.Locale;

public enum HairStyle {
    WILD_SPIKES,
    FLAME_UP,
    LONG_MANE,
    SHORT_NEAT,
    SIDE_SWEPT,
    PONYTAIL,
    BALD,
    SUPER_SPIKES,
    ASCENDED_SPIKES,
    LEGENDARY_MANE;

    public static final int PLAYER_SELECTABLE = 7;

    private static final HairStyle[] VALUES = values();

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static HairStyle byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : SHORT_NEAT;
    }

    public static HairStyle byId(String id) {
        for (HairStyle style : VALUES) {
            if (style.id().equals(id)) {
                return style;
            }
        }
        return null;
    }
}
