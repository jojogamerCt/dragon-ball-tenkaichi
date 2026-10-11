package com.dbtenkaichi.stats;

import java.util.Arrays;

public final class StatBlock {
    public static final int MIN_LEVEL = 1;

    private final int[] levels;

    public StatBlock() {
        this.levels = new int[StatType.COUNT];
        Arrays.fill(this.levels, MIN_LEVEL);
    }

    public StatBlock(int[] levels) {
        this();
        for (int i = 0; i < Math.min(levels.length, StatType.COUNT); i++) {
            this.levels[i] = Math.max(MIN_LEVEL, levels[i]);
        }
    }

    public int get(StatType type) {
        return levels[type.ordinal()];
    }

    public void set(StatType type, int level) {
        levels[type.ordinal()] = Math.max(MIN_LEVEL, level);
    }

    public void add(StatType type, int amount) {
        set(type, get(type) + amount);
    }

    public int total() {
        int sum = 0;
        for (int level : levels) {
            sum += level;
        }
        return sum;
    }

    public int[] toArray() {
        return levels.clone();
    }

    public StatBlock copy() {
        return new StatBlock(levels);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof StatBlock other && Arrays.equals(levels, other.levels);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(levels);
    }

    @Override
    public String toString() {
        return "StatBlock" + Arrays.toString(levels);
    }
}
