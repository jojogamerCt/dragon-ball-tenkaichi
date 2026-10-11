package com.dbtenkaichi.progression;

import com.dbtenkaichi.config.BalanceConfig;

public final class ProgressionMath {
    private ProgressionMath() {
    }

    public static long statUpgradeCost(int currentLevel, double affinity, BalanceConfig cfg) {
        double safeAffinity = Math.max(0.25, affinity);
        double cost = cfg.statCostBase * Math.pow(1.0 + currentLevel, cfg.statCostExponent) / safeAffinity;
        return Math.max(1L, Math.round(cost));
    }

    public static int statCap(int potentialLevel, BalanceConfig cfg) {
        double cap = cfg.statCapBase + potentialLevel * cfg.statCapPerPotential;
        return (int) Math.min(cfg.statHardCap, Math.round(cap));
    }

    public static double repetitionFactor(int repetitionsToday, BalanceConfig cfg) {
        return Math.pow(0.5, Math.max(0, repetitionsToday) / Math.max(1.0, cfg.dailyRepetitionHalfLife));
    }

    public static double fatigueFactor(double fatigue, BalanceConfig cfg) {
        double f = Math.max(0.0, Math.min(1.0, fatigue));
        return 1.0 - cfg.fatigueGainPenalty * f;
    }

    public static double trainingGain(double base, double fatigue, int repetitionsToday, BalanceConfig cfg) {
        return base * cfg.tpGainMultiplier * cfg.trainingSpeedMultiplier * repetitionFactor(repetitionsToday, cfg) * fatigueFactor(fatigue, cfg);
    }

    public static double combatReward(long playerBp, long enemyBp, double damageShare, double fatigue, BalanceConfig cfg) {
        double ratio = (double) Math.max(1L, enemyBp) / (double) Math.max(1L, playerBp);
        double clamped = Math.max(cfg.combatTpMinRatio, Math.min(cfg.combatTpMaxRatio, ratio));
        double share = Math.max(0.0, Math.min(1.0, damageShare));
        double scaleByEnemy = Math.log10(10.0 + Math.max(1L, enemyBp)) ;
        return cfg.combatTpBase * Math.pow(clamped, cfg.combatTpRatioExponent) * share * scaleByEnemy * cfg.tpGainMultiplier * fatigueFactor(fatigue, cfg);
    }

    public static int zenkaiLevels(int statLevel, long playerBp, long enemyBp, BalanceConfig cfg) {
        double ratio = (double) Math.max(1L, enemyBp) / (double) Math.max(1L, playerBp);
        if (ratio < 1.0) {
            return 0;
        }
        double gain = statLevel * cfg.zenkaiStatFraction * Math.min(3.0, Math.sqrt(ratio));
        return (int) Math.max(1, Math.min(cfg.zenkaiMaxLevels, Math.round(gain)));
    }
}
