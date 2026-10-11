package com.dbtenkaichi.config;

public final class TenkaichiConfig {
    public int schemaVersion = ConfigManager.COMMON_SCHEMA;
    public BalanceConfig balance = new BalanceConfig();
    public GameplayConfig gameplay = new GameplayConfig();

    public void validate() {
        if (balance == null) {
            balance = new BalanceConfig();
        }
        if (gameplay == null) {
            gameplay = new GameplayConfig();
        }
        BalanceConfig defaults = new BalanceConfig();
        if (balance.bpWeights == null || balance.bpWeights.length != defaults.bpWeights.length) {
            balance.bpWeights = defaults.bpWeights;
        }
        balance.statMultiplier = clamp(balance.statMultiplier, 0.01, 100.0);
        balance.tpGainMultiplier = clamp(balance.tpGainMultiplier, 0.0, 100.0);
        balance.trainingSpeedMultiplier = clamp(balance.trainingSpeedMultiplier, 0.0, 100.0);
        balance.kiCostMultiplier = clamp(balance.kiCostMultiplier, 0.0, 100.0);
        balance.statHardCap = (int) clamp(balance.statHardCap, 10, 100000);
        gameplay.minHeight = (float) clamp(gameplay.minHeight, 0.5, 1.0);
        gameplay.maxHeight = (float) clamp(gameplay.maxHeight, 1.0, 1.5);
        gameplay.maxExplosionRadius = (int) clamp(gameplay.maxExplosionRadius, 0, 16);
        gameplay.maxBlocksPerExplosion = (int) clamp(gameplay.maxBlocksPerExplosion, 0, 4096);
        gameplay.maxActionsPerSecond = (int) clamp(gameplay.maxActionsPerSecond, 5, 200);
        gameplay.maxFlightSpeed = clamp(gameplay.maxFlightSpeed, 0.2, 8.0);
        gameplay.maxLockOnDistance = (int) clamp(gameplay.maxLockOnDistance, 4, 128);
        if (gameplay.enabledRaces == null || gameplay.enabledRaces.isEmpty()) {
            gameplay.enabledRaces = new GameplayConfig().enabledRaces;
        }
        if (gameplay.destructionDimensionBlacklist == null) {
            gameplay.destructionDimensionBlacklist = new java.util.ArrayList<>();
        }
    }

    public DestructionMode destruction() {
        return DestructionMode.parse(gameplay.destructionMode);
    }

    private static double clamp(double v, double min, double max) {
        if (Double.isNaN(v)) {
            return min;
        }
        return v < min ? min : Math.min(v, max);
    }

    public enum DestructionMode {
        OFF,
        VISUAL,
        REAL,
        REGENERATING;

        public static DestructionMode parse(String value) {
            if (value == null) {
                return REAL;
            }
            return switch (value.toLowerCase(java.util.Locale.ROOT)) {
                case "off", "disabled", "none" -> OFF;
                case "visual" -> VISUAL;
                case "regen", "regenerating", "regenerate" -> REGENERATING;
                default -> REAL;
            };
        }
    }
}
