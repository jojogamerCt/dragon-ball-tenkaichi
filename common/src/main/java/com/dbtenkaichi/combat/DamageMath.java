package com.dbtenkaichi.combat;

import com.dbtenkaichi.config.BalanceConfig;

public final class DamageMath {
    private DamageMath() {
    }

    public static double powerRatioFactor(long attackerBp, long defenderBp, BalanceConfig cfg) {
        double ratio = (double) Math.max(1L, attackerBp) / (double) Math.max(1L, defenderBp);
        double factor = Math.pow(ratio, cfg.powerRatioExponent);
        return clamp(factor, cfg.powerRatioMinFactor, cfg.powerRatioMaxFactor);
    }

    public static double melee(double moveBase, int strength, int martialArts, double formAttackMultiplier, double comboScaling, BalanceConfig cfg) {
        double scale = 1.0 + strength * cfg.meleeStrengthScale * cfg.statMultiplier + martialArts * cfg.meleeMartialArtsScale * cfg.statMultiplier;
        return moveBase * scale * formAttackMultiplier * clamp(comboScaling, 0.1, 1.0);
    }

    public static double ki(double techniqueBase, int kiOutput, double chargeRatio, double formKiMultiplier, BalanceConfig cfg) {
        double scale = 1.0 + kiOutput * cfg.kiOutputScale * cfg.statMultiplier;
        double charge = 0.35 + 0.65 * clamp(chargeRatio, 0.0, 1.0);
        return techniqueBase * scale * charge * formKiMultiplier;
    }

    public static double mitigate(double raw, int defense, double formDefenseMultiplier, GuardState guard, double fatigue, BalanceConfig cfg) {
        double effectiveDefense = defense * cfg.defenseScale * cfg.statMultiplier * formDefenseMultiplier;
        double result = raw * cfg.defenseReference / (cfg.defenseReference + effectiveDefense);
        result *= switch (guard) {
            case NONE -> 1.0;
            case GUARD -> 1.0 - cfg.guardReduction;
            case PERFECT -> 1.0 - cfg.perfectGuardReduction;
        };
        result *= 1.0 + cfg.fatigueCombatPenalty * clamp(fatigue, 0.0, 1.0);
        return result;
    }

    public static double finalDamage(double raw, long attackerBp, long defenderBp, int defense, double formDefenseMultiplier, GuardState guard, double defenderFatigue, BalanceConfig cfg) {
        double scaled = raw * powerRatioFactor(attackerBp, defenderBp, cfg);
        double mitigated = mitigate(scaled, defense, formDefenseMultiplier, guard, defenderFatigue, cfg);
        if (guard == GuardState.PERFECT) {
            return 0.0;
        }
        return Math.max(cfg.minimumDamage, mitigated);
    }

    public static double comboScaling(int hitIndex) {
        if (hitIndex <= 2) {
            return 1.0;
        }
        return Math.max(0.35, 1.0 - (hitIndex - 2) * 0.08);
    }

    static double clamp(double v, double min, double max) {
        return v < min ? min : Math.min(v, max);
    }

    public enum GuardState {
        NONE,
        GUARD,
        PERFECT
    }
}
