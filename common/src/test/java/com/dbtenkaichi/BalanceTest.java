package com.dbtenkaichi;

import com.dbtenkaichi.combat.DamageMath;
import com.dbtenkaichi.config.BalanceConfig;
import com.dbtenkaichi.progression.ProgressionMath;
import com.dbtenkaichi.race.Race;
import com.dbtenkaichi.stats.BattlePower;
import com.dbtenkaichi.stats.StatBlock;
import com.dbtenkaichi.stats.StatType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BalanceTest {
    private final BalanceConfig cfg = new BalanceConfig();

    private static StatBlock uniform(int level) {
        StatBlock block = new StatBlock();
        for (StatType type : StatType.all()) {
            block.set(type, level);
        }
        return block;
    }

    @Test
    void weightsCoverEveryStat() {
        assertEquals(StatType.COUNT, cfg.bpWeights.length);
    }

    @Test
    void battlePowerIsMonotonicInEveryStat() {
        StatBlock base = uniform(10);
        long reference = BattlePower.base(base, cfg);
        for (StatType type : StatType.all()) {
            if (cfg.bpWeights[type.ordinal()] <= 0.0) {
                continue;
            }
            StatBlock raised = base.copy();
            raised.add(type, 5);
            assertTrue(BattlePower.base(raised, cfg) > reference, "raising " + type + " must raise BP");
        }
    }

    @Test
    void startingBattlePowerIsInAReadableRange() {
        long bp = BattlePower.base(uniform(5), cfg);
        assertTrue(bp > 100 && bp < 5000, "starting BP was " + bp);
    }

    @Test
    void lateGameBattlePowerReachesMillionsWithForms() {
        long bp = BattlePower.effective(uniform(400), cfg, new BattlePower.Condition(50.0, 1.0, 1.0, 0.0, 1.0));
        assertTrue(bp > 1_000_000L, "late BP was " + bp);
    }

    @Test
    void conditionLowersPower() {
        StatBlock stats = uniform(50);
        long healthy = BattlePower.effective(stats, cfg, BattlePower.Condition.neutral());
        long hurt = BattlePower.effective(stats, cfg, new BattlePower.Condition(1.0, 0.1, 0.2, 0.8, 1.0));
        assertTrue(hurt < healthy);
        assertTrue(hurt > healthy / 4, "condition must not erase power completely");
    }

    @Test
    void suppressionScalesDisplayedPower() {
        StatBlock stats = uniform(50);
        long full = BattlePower.effective(stats, cfg, BattlePower.Condition.neutral());
        long hidden = BattlePower.effective(stats, cfg, new BattlePower.Condition(1.0, 1.0, 1.0, 0.0, 0.1));
        assertEquals(full / 10.0, hidden, full * 0.01);
    }

    @Test
    void powerRatioFactorIsBounded() {
        assertEquals(cfg.powerRatioMaxFactor, DamageMath.powerRatioFactor(1_000_000_000L, 1L, cfg), 1e-9);
        assertEquals(cfg.powerRatioMinFactor, DamageMath.powerRatioFactor(1L, 1_000_000_000L, cfg), 1e-9);
        assertEquals(1.0, DamageMath.powerRatioFactor(5000L, 5000L, cfg), 1e-9);
    }

    @Test
    void weakerFighterCanStillWinThroughTechnique() {
        double strongHit = DamageMath.finalDamage(DamageMath.melee(4.0, 60, 20, 1.0, 1.0, cfg), 50_000, 25_000, 30, 1.0, DamageMath.GuardState.GUARD, 0.0, cfg);
        double weakHit = DamageMath.finalDamage(DamageMath.melee(4.0, 40, 60, 1.0, 1.0, cfg), 25_000, 50_000, 30, 1.0, DamageMath.GuardState.NONE, 0.0, cfg);
        assertTrue(weakHit > strongHit, "an unguarded opening must matter more than raw power");
    }

    @Test
    void perfectGuardNegatesDamage() {
        assertEquals(0.0, DamageMath.finalDamage(100.0, 10, 10, 10, 1.0, DamageMath.GuardState.PERFECT, 0.0, cfg));
    }

    @Test
    void comboScalingDecaysButNeverVanishes() {
        double previous = 1.0;
        for (int i = 0; i < 30; i++) {
            double value = DamageMath.comboScaling(i);
            assertTrue(value <= previous + 1e-9);
            assertTrue(value >= 0.35);
            previous = value;
        }
    }

    @Test
    void formMultiplierIsCompressedForCombat() {
        double ssj = BattlePower.combatMultiplierFromForm(50.0, cfg);
        assertTrue(ssj > 1.8 && ssj < 2.3, "SSJ combat multiplier was " + ssj);
        assertEquals(1.0, BattlePower.combatMultiplierFromForm(1.0, cfg), 1e-9);
    }

    @Test
    void statCostsGrowSuperLinearly() {
        long early = ProgressionMath.statUpgradeCost(5, 1.0, cfg);
        long late = ProgressionMath.statUpgradeCost(100, 1.0, cfg);
        assertTrue(late > early * 20);
        assertTrue(ProgressionMath.statUpgradeCost(50, 1.3, cfg) < ProgressionMath.statUpgradeCost(50, 0.8, cfg));
    }

    @Test
    void repeatedTrainingConvergesInsteadOfGrowingForever() {
        double total = 0.0;
        for (int i = 0; i < 10_000; i++) {
            total += ProgressionMath.trainingGain(10.0, 0.0, i, cfg);
        }
        double bound = 10.0 / (1.0 - Math.pow(0.5, 1.0 / cfg.dailyRepetitionHalfLife));
        assertTrue(total < bound + 1e-6, "daily training total " + total + " exceeded bound " + bound);
    }

    @Test
    void fatigueReducesGains() {
        assertTrue(ProgressionMath.trainingGain(10.0, 1.0, 0, cfg) < ProgressionMath.trainingGain(10.0, 0.0, 0, cfg));
    }

    @Test
    void farmingWeakEnemiesIsUnrewarding() {
        double weak = ProgressionMath.combatReward(100_000, 10, 1.0, 0.0, cfg);
        double even = ProgressionMath.combatReward(100_000, 100_000, 1.0, 0.0, cfg);
        assertTrue(weak * 5 < even, "weak=" + weak + " even=" + even);
    }

    @Test
    void statCapIsBoundedByHardCap() {
        assertEquals(cfg.statHardCap, ProgressionMath.statCap(1_000_000, cfg));
        assertTrue(ProgressionMath.statCap(1, cfg) >= cfg.statCapBase);
    }

    @Test
    void zenkaiOnlyAgainstStrongerFoesAndIsCapped() {
        assertEquals(0, ProgressionMath.zenkaiLevels(100, 10_000, 5_000, cfg));
        int gain = ProgressionMath.zenkaiLevels(100, 10_000, 1_000_000, cfg);
        assertTrue(gain >= 1 && gain <= cfg.zenkaiMaxLevels);
    }

    @Test
    void racesAreMechanicallyDistinct() {
        for (Race a : Race.values()) {
            for (Race b : Race.values()) {
                if (a == b) {
                    continue;
                }
                boolean differs = !a.passives().equals(b.passives());
                for (StatType type : StatType.all()) {
                    differs |= a.affinity(type) != b.affinity(type);
                }
                assertTrue(differs, a + " and " + b + " must differ");
            }
        }
    }

    @Test
    void allRacesArePlayable() {
        for (Race race : Race.values()) {
            assertTrue(race.implemented(), race + " must be playable");
        }
    }
}
