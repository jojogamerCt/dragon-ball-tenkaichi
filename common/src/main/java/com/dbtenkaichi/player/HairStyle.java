package com.dbtenkaichi.player;

import java.util.Locale;

public enum HairStyle {
    GOKU,
    VEGETA,
    GOHAN,
    TRUNKS,
    YAMCHA,
    RADITZ,
    BALD,
    ANDROID_18,
    VIDEL,
    CHI_CHI,
    SUPER_SPIKES,
    ASCENDED_SPIKES,
    LEGENDARY_MANE,
    SSJ_VEGETA;

    public static final int PLAYER_SELECTABLE = 10;
    private static final int[] LEGACY = {0, 1, 5, 4, 2, 9, 6};

    public static int fromLegacy(int ordinal) {
        return ordinal >= 0 && ordinal < LEGACY.length ? LEGACY[ordinal] : GOKU.ordinal();
    }

    public static HairStyle transformed(HairStyle base, HairStyle form) {
        if (form == null) {
            return base;
        }
        if (base == BALD) {
            return BALD;
        }
        return switch (form) {
            case SUPER_SPIKES -> switch (base) {
                case VEGETA -> SSJ_VEGETA;
                case GOHAN -> ASCENDED_SPIKES;
                case RADITZ -> RADITZ;
                default -> SUPER_SPIKES;
            };
            case ASCENDED_SPIKES -> switch (base) {
                case VEGETA -> SSJ_VEGETA;
                case RADITZ -> RADITZ;
                default -> ASCENDED_SPIKES;
            };
            default -> form;
        };
    }

    private static final HairStyle[] VALUES = values();

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static HairStyle byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : GOKU;
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
