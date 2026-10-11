package com.dbtenkaichi.stats;

import com.dbtenkaichi.config.BalanceConfig;
import com.dbtenkaichi.race.Race;
import com.dbtenkaichi.race.RacePassive;

public final class DerivedStats {
    private DerivedStats() {
    }

    public static double maxKi(StatBlock s, Race race, BalanceConfig cfg) {
        double base = cfg.baseKi + s.get(StatType.KI_CAPACITY) * cfg.kiPerCapacity * cfg.statMultiplier;
        if (race.has(RacePassive.ENERGY_RESERVE)) {
            base *= 0.8;
        }
        return base;
    }

    public static double maxStamina(StatBlock s, BalanceConfig cfg) {
        return cfg.baseStamina + s.get(StatType.STAMINA) * cfg.staminaPerLevel * cfg.statMultiplier;
    }

    public static double bonusHealth(StatBlock s, BalanceConfig cfg) {
        return s.get(StatType.CONSTITUTION) * cfg.healthPerConstitution * cfg.statMultiplier;
    }

    public static double kiRegenPerSecond(StatBlock s, Race race, BalanceConfig cfg) {
        return (cfg.kiRegenBase + s.get(StatType.KI_CONTROL) * cfg.kiRegenPerControl) * race.kiRegenMultiplier();
    }

    public static double kiChargePerSecond(StatBlock s, Race race, BalanceConfig cfg) {
        double max = maxKi(s, race, cfg);
        return cfg.kiChargeBase + s.get(StatType.KI_CONTROL) * cfg.kiChargePerControl + max * (0.06 + s.get(StatType.KI_CONTROL) * 0.0008);
    }

    public static double staminaRegenPerSecond(StatBlock s, BalanceConfig cfg) {
        return cfg.staminaRegenBase + s.get(StatType.STAMINA) * cfg.staminaRegenPerLevel;
    }

    public static double kiCostMultiplier(StatBlock s, Race race, BalanceConfig cfg) {
        double reduction = Math.min(cfg.maxKiCostReduction, s.get(StatType.KI_CONTROL) * cfg.kiCostReductionPerControl);
        double mult = (1.0 - reduction) * cfg.kiCostMultiplier;
        if (race.has(RacePassive.KI_EFFICIENCY)) {
            mult *= 0.85;
        }
        return mult;
    }

    public static double movementBonus(StatBlock s) {
        return Math.min(0.6, s.get(StatType.SPEED) * 0.0025);
    }

    public static int attackIntervalTicks(StatBlock s) {
        return Math.max(3, 7 - s.get(StatType.AGILITY) / 40);
    }

    public static double flightSpeed(StatBlock s, boolean fast, double formSpeed, double baseSpeed, double fastSpeed, double perLevel, double cap) {
        double base = fast ? fastSpeed : baseSpeed;
        double value = (base + s.get(StatType.SPEED) * perLevel * (fast ? 2.0 : 1.0)) * formSpeed;
        return Math.min(cap, value);
    }
}
