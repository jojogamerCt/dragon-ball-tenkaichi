package com.dbtenkaichi.config;

public final class BalanceConfig {
    public double statMultiplier = 1.0;
    public double tpGainMultiplier = 1.0;
    public double trainingSpeedMultiplier = 1.0;
    public double kiCostMultiplier = 1.0;

    public double bpScale = 5.0;
    public double bpExponent = 1.6;
    public double[] bpWeights = {1.0, 0.6, 0.6, 0.6, 0.5, 1.0, 0.7, 0.5, 0.3, 0.8, 0.4, 0.0};

    public double powerRatioExponent = 0.25;
    public double powerRatioMinFactor = 0.6;
    public double powerRatioMaxFactor = 1.6;

    public double meleeStrengthScale = 0.08;
    public double meleeMartialArtsScale = 0.03;
    public double kiOutputScale = 0.09;
    public double defenseScale = 1.5;
    public double defenseReference = 100.0;
    public double guardReduction = 0.65;
    public double perfectGuardReduction = 1.0;
    public double minimumDamage = 0.5;

    public double baseHealth = 20.0;
    public double healthPerConstitution = 2.0;
    public double baseKi = 100.0;
    public double kiPerCapacity = 12.0;
    public double baseStamina = 100.0;
    public double staminaPerLevel = 6.0;
    public double kiRegenBase = 1.0;
    public double kiRegenPerControl = 0.08;
    public double kiChargeBase = 8.0;
    public double kiChargePerControl = 0.5;
    public double staminaRegenBase = 6.0;
    public double staminaRegenPerLevel = 0.1;
    public double kiCostReductionPerControl = 0.004;
    public double maxKiCostReduction = 0.5;

    public double statCostBase = 10.0;
    public double statCostExponent = 1.35;
    public int statCapBase = 50;
    public double statCapPerPotential = 5.0;
    public int statHardCap = 1000;

    public double dailyRepetitionHalfLife = 12.0;
    public double fatigueGainPenalty = 0.75;
    public double fatigueCombatPenalty = 0.25;

    public double combatTpBase = 4.0;
    public double combatTpRatioExponent = 0.75;
    public double combatTpMaxRatio = 4.0;
    public double combatTpMinRatio = 0.1;

    public double zenkaiStatFraction = 0.04;
    public int zenkaiMaxLevels = 8;
    public long zenkaiCooldownTicks = 24000L;

    public double formMultiplierCompression = 0.6;
}
