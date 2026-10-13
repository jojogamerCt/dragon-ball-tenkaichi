package com.dbtenkaichi.entity;

import com.dbtenkaichi.training.Arena;

public enum MasterKind {
    MASTER("master", Arena.MOUNTAIN),
    ROSHI("roshi", Arena.ISLAND),
    KARIN("karin", Arena.TOWER),
    KAMI("kami", Arena.TIME_CHAMBER);

    private static final MasterKind[] VALUES = values();

    private final String id;
    private final Arena arena;

    MasterKind(String id, Arena arena) {
        this.id = id;
        this.arena = arena;
    }

    public String id() {
        return id;
    }

    public Arena arena() {
        return arena;
    }

    public String translationKey() {
        return this == MASTER ? "entity.dbtenkaichi.master" : "entity.dbtenkaichi.master." + id;
    }

    public static MasterKind byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : MASTER;
    }

    public static MasterKind byId(String id) {
        for (MasterKind kind : VALUES) {
            if (kind.id.equals(id)) {
                return kind;
            }
        }
        return null;
    }
}
