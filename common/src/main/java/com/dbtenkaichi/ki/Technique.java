package com.dbtenkaichi.ki;

import com.dbtenkaichi.stats.StatType;

public record Technique(
        String id,
        Type type,
        double baseDamage,
        double kiCost,
        double kiCostPerCharge,
        int minCharge,
        int maxCharge,
        double speed,
        double radius,
        double radiusPerCharge,
        int maxLength,
        int duration,
        double explosionRadius,
        double explosionPerCharge,
        double steer,
        boolean penetratesEntities,
        boolean clashable,
        boolean trench,
        int cooldownTicks,
        int coreColor,
        int glowColor,
        StatType trainedStat,
        String shape,
        String effect
) {
    public Technique(String id, Type type, double baseDamage, double kiCost, double kiCostPerCharge, int minCharge, int maxCharge, double speed,
                     double radius, double radiusPerCharge, int maxLength, int duration, double explosionRadius, double explosionPerCharge, double steer,
                     boolean penetratesEntities, boolean clashable, boolean trench, int cooldownTicks, int coreColor, int glowColor, StatType trainedStat) {
        this(id, type, baseDamage, kiCost, kiCostPerCharge, minCharge, maxCharge, speed, radius, radiusPerCharge, maxLength, duration, explosionRadius,
                explosionPerCharge, steer, penetratesEntities, clashable, trench, cooldownTicks, coreColor, glowColor, trainedStat, "ball", "");
    }

    public enum Type {
        BLAST,
        BEAM,
        SPHERE,
        BARRAGE,
        SUPPORT
    }

    public boolean charged() {
        return type == Type.BEAM || type == Type.SPHERE;
    }

    public boolean hasEffect(String name) {
        return effect != null && effect.equals(name);
    }

    public String translationKey() {
        return "technique." + id.replace(':', '.');
    }

    public double costAt(double chargeRatio) {
        return kiCost + kiCostPerCharge * Math.max(0.0, Math.min(1.0, chargeRatio));
    }

    public double radiusAt(double chargeRatio) {
        return radius + radiusPerCharge * Math.max(0.0, Math.min(1.0, chargeRatio));
    }

    public double explosionAt(double chargeRatio) {
        return explosionRadius + explosionPerCharge * Math.max(0.0, Math.min(1.0, chargeRatio));
    }
}
