package com.dbtenkaichi.race;

import com.dbtenkaichi.stats.StatType;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import static com.dbtenkaichi.race.RacePassive.*;

public enum Race {
    SAIYAN(builder()
            .affinity(StatType.STRENGTH, 1.3).affinity(StatType.KI_OUTPUT, 1.2).affinity(StatType.SPEED, 1.1)
            .affinity(StatType.MARTIAL_ARTS, 1.1).affinity(StatType.KI_CONTROL, 0.85).affinity(StatType.SPIRIT, 0.9)
            .start(StatType.STRENGTH, 3).start(StatType.CONSTITUTION, 2).start(StatType.KI_OUTPUT, 2)
            .passives(ZENKAI, BATTLE_HUNGER, TAIL)
            .tpGain(1.0).combatTpGain(1.25).kiRegen(0.9).flightCost(1.0)
            .visuals(true, true, false, false)
            .implemented(true)),
    HALF_SAIYAN(builder()
            .affinity(StatType.STRENGTH, 1.15).affinity(StatType.KI_OUTPUT, 1.15).affinity(StatType.POTENTIAL, 1.3)
            .affinity(StatType.KI_CONTROL, 1.05)
            .start(StatType.STRENGTH, 2).start(StatType.KI_OUTPUT, 2).start(StatType.POTENTIAL, 2)
            .passives(ZENKAI, HIDDEN_POTENTIAL)
            .tpGain(1.05).combatTpGain(1.1).kiRegen(1.0).flightCost(1.0)
            .visuals(true, false, false, false)
            .implemented(false)),
    HUMAN(builder()
            .affinity(StatType.KI_CONTROL, 1.3).affinity(StatType.MARTIAL_ARTS, 1.3).affinity(StatType.AGILITY, 1.15)
            .affinity(StatType.STRENGTH, 0.9).affinity(StatType.KI_OUTPUT, 0.95).affinity(StatType.STAMINA, 1.1)
            .start(StatType.KI_CONTROL, 3).start(StatType.MARTIAL_ARTS, 3).start(StatType.AGILITY, 2)
            .passives(TECHNIQUE_MASTERY, KI_EFFICIENCY)
            .tpGain(1.15).combatTpGain(1.0).kiRegen(1.15).flightCost(0.85)
            .visuals(true, false, false, false)
            .implemented(true)),
    NAMEKIAN(builder()
            .affinity(StatType.KI_CONTROL, 1.2).affinity(StatType.SPIRIT, 1.25).affinity(StatType.CONSTITUTION, 1.15)
            .affinity(StatType.SPEED, 0.9)
            .start(StatType.SPIRIT, 3).start(StatType.CONSTITUTION, 2).start(StatType.KI_CONTROL, 2)
            .passives(REGENERATION, STRETCH_LIMBS)
            .tpGain(1.0).combatTpGain(1.0).kiRegen(1.1).flightCost(0.95)
            .visuals(false, false, true, false)
            .implemented(false)),
    FRIEZA_RACE(builder()
            .affinity(StatType.SPEED, 1.2).affinity(StatType.KI_OUTPUT, 1.2).affinity(StatType.DEFENSE, 1.1)
            .affinity(StatType.MARTIAL_ARTS, 0.85)
            .start(StatType.SPEED, 3).start(StatType.KI_OUTPUT, 2).start(StatType.DEFENSE, 2)
            .passives(SPACE_SURVIVAL, TAIL)
            .tpGain(0.95).combatTpGain(1.05).kiRegen(1.0).flightCost(0.9)
            .visuals(false, true, false, true)
            .implemented(false)),
    MAJIN(builder()
            .affinity(StatType.CONSTITUTION, 1.4).affinity(StatType.STAMINA, 1.2).affinity(StatType.KI_CONTROL, 0.8)
            .affinity(StatType.MARTIAL_ARTS, 0.85)
            .start(StatType.CONSTITUTION, 4).start(StatType.STAMINA, 2)
            .passives(REGENERATION, ELASTIC_BODY, ABSORPTION)
            .tpGain(0.95).combatTpGain(1.0).kiRegen(1.05).flightCost(1.0)
            .visuals(false, false, true, false)
            .implemented(false)),
    ANDROID(builder()
            .affinity(StatType.KI_CAPACITY, 0.7).affinity(StatType.STAMINA, 1.4).affinity(StatType.DEFENSE, 1.2)
            .affinity(StatType.SPIRIT, 0.7)
            .start(StatType.STAMINA, 3).start(StatType.DEFENSE, 2).start(StatType.STRENGTH, 2)
            .passives(ENERGY_RESERVE, UNSENSABLE)
            .tpGain(0.9).combatTpGain(1.0).kiRegen(1.6).flightCost(0.8)
            .visuals(true, false, false, false)
            .implemented(false)),
    BIO_ANDROID(builder()
            .affinity(StatType.CONSTITUTION, 1.15).affinity(StatType.KI_OUTPUT, 1.1).affinity(StatType.POTENTIAL, 1.1)
            .start(StatType.CONSTITUTION, 2).start(StatType.KI_OUTPUT, 2)
            .passives(REGENERATION, ABSORPTION, ZENKAI)
            .tpGain(0.95).combatTpGain(1.1).kiRegen(1.0).flightCost(1.0)
            .visuals(false, true, false, true)
            .implemented(false)),
    KAIOSHIN(builder()
            .affinity(StatType.SPIRIT, 1.4).affinity(StatType.KI_CONTROL, 1.3).affinity(StatType.STRENGTH, 0.8)
            .start(StatType.SPIRIT, 4).start(StatType.KI_CONTROL, 2)
            .passives(DIVINE_KI, KI_EFFICIENCY)
            .tpGain(1.0).combatTpGain(0.95).kiRegen(1.25).flightCost(0.8)
            .visuals(true, false, false, false)
            .implemented(false)),
    DEMON(builder()
            .affinity(StatType.KI_OUTPUT, 1.3).affinity(StatType.STRENGTH, 1.1).affinity(StatType.SPIRIT, 0.85)
            .start(StatType.KI_OUTPUT, 3).start(StatType.STRENGTH, 2)
            .passives(DEMONIC_RAGE)
            .tpGain(1.0).combatTpGain(1.1).kiRegen(1.0).flightCost(1.0)
            .visuals(true, false, false, true)
            .implemented(false));

    private static final Race[] VALUES = values();

    private final double[] affinities;
    private final int[] startBonus;
    private final Set<RacePassive> passives;
    private final double tpGain;
    private final double combatTpGain;
    private final double kiRegen;
    private final double flightCost;
    private final boolean hasHair;
    private final boolean tailByDefault;
    private final boolean antennae;
    private final boolean horns;
    private final boolean implemented;

    Race(Builder b) {
        this.affinities = b.affinities;
        this.startBonus = b.start;
        this.passives = b.passives.isEmpty() ? EnumSet.noneOf(RacePassive.class) : EnumSet.copyOf(b.passives);
        this.tpGain = b.tpGain;
        this.combatTpGain = b.combatTpGain;
        this.kiRegen = b.kiRegen;
        this.flightCost = b.flightCost;
        this.hasHair = b.hasHair;
        this.tailByDefault = b.tail;
        this.antennae = b.antennae;
        this.horns = b.horns;
        this.implemented = b.implemented;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "race.dbtenkaichi." + id();
    }

    public double affinity(StatType type) {
        return affinities[type.ordinal()];
    }

    public int startBonus(StatType type) {
        return startBonus[type.ordinal()];
    }

    public boolean has(RacePassive passive) {
        return passives.contains(passive);
    }

    public Set<RacePassive> passives() {
        return EnumSet.copyOf(passives.isEmpty() ? EnumSet.noneOf(RacePassive.class) : passives);
    }

    public double tpGainMultiplier() {
        return tpGain;
    }

    public double combatTpMultiplier() {
        return combatTpGain;
    }

    public double kiRegenMultiplier() {
        return kiRegen;
    }

    public double flightCostMultiplier() {
        return flightCost;
    }

    public boolean hasHair() {
        return hasHair;
    }

    public boolean tailByDefault() {
        return tailByDefault;
    }

    public boolean hasAntennae() {
        return antennae;
    }

    public boolean hasHorns() {
        return horns;
    }

    public boolean implemented() {
        return implemented;
    }

    public static Race byId(String id) {
        for (Race race : VALUES) {
            if (race.id().equals(id)) {
                return race;
            }
        }
        return null;
    }

    public static Race byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : HUMAN;
    }

    private static Builder builder() {
        return new Builder();
    }

    private static final class Builder {
        final double[] affinities = filled(1.0);
        final int[] start = new int[StatType.COUNT];
        final EnumSet<RacePassive> passives = EnumSet.noneOf(RacePassive.class);
        double tpGain = 1.0;
        double combatTpGain = 1.0;
        double kiRegen = 1.0;
        double flightCost = 1.0;
        boolean hasHair;
        boolean tail;
        boolean antennae;
        boolean horns;
        boolean implemented;

        private static double[] filled(double v) {
            double[] a = new double[StatType.COUNT];
            java.util.Arrays.fill(a, v);
            return a;
        }

        Builder affinity(StatType type, double value) {
            affinities[type.ordinal()] = value;
            return this;
        }

        Builder start(StatType type, int value) {
            start[type.ordinal()] = value;
            return this;
        }

        Builder passives(RacePassive... values) {
            passives.addAll(java.util.List.of(values));
            return this;
        }

        Builder tpGain(double v) {
            tpGain = v;
            return this;
        }

        Builder combatTpGain(double v) {
            combatTpGain = v;
            return this;
        }

        Builder kiRegen(double v) {
            kiRegen = v;
            return this;
        }

        Builder flightCost(double v) {
            flightCost = v;
            return this;
        }

        Builder visuals(boolean hair, boolean tail, boolean antennae, boolean horns) {
            this.hasHair = hair;
            this.tail = tail;
            this.antennae = antennae;
            this.horns = horns;
            return this;
        }

        Builder implemented(boolean v) {
            implemented = v;
            return this;
        }
    }
}
