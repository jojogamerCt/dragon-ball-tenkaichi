package com.dbtenkaichi.stats;

import com.dbtenkaichi.config.BalanceConfig;

public final class BattlePower {
    private BattlePower() {
    }

    public static double rawPower(StatBlock stats, BalanceConfig cfg) {
        double sum = 0.0;
        for (StatType type : StatType.all()) {
            double weight = type.ordinal() < cfg.bpWeights.length ? cfg.bpWeights[type.ordinal()] : 0.0;
            sum += weight * stats.get(type);
        }
        return sum * cfg.statMultiplier;
    }

    public static long base(StatBlock stats, BalanceConfig cfg) {
        return Math.max(1L, Math.round(cfg.bpScale * Math.pow(rawPower(stats, cfg), cfg.bpExponent)));
    }

    public static long effective(StatBlock stats, BalanceConfig cfg, Condition condition) {
        double value = base(stats, cfg);
        value *= Math.max(0.0, condition.formMultiplier());
        value *= kiFactor(condition.kiRatio());
        value *= healthFactor(condition.healthRatio());
        value *= 1.0 - 0.3 * clamp01(condition.fatigue());
        value *= clamp(condition.suppression(), 0.01, 1.0);
        return Math.max(1L, Math.round(value));
    }

    public static double kiFactor(double kiRatio) {
        return 0.55 + 0.45 * Math.sqrt(clamp01(kiRatio));
    }

    public static double healthFactor(double healthRatio) {
        return 0.75 + 0.25 * clamp01(healthRatio);
    }

    public static double combatMultiplierFromForm(double formBpMultiplier, BalanceConfig cfg) {
        if (formBpMultiplier <= 1.0) {
            return Math.max(0.1, formBpMultiplier);
        }
        return 1.0 + Math.log10(formBpMultiplier) * cfg.formMultiplierCompression;
    }

    static double clamp01(double v) {
        return clamp(v, 0.0, 1.0);
    }

    static double clamp(double v, double min, double max) {
        return v < min ? min : Math.min(v, max);
    }

    public record Condition(double formMultiplier, double kiRatio, double healthRatio, double fatigue, double suppression) {
        public static Condition neutral() {
            return new Condition(1.0, 1.0, 1.0, 0.0, 1.0);
        }
    }
}
