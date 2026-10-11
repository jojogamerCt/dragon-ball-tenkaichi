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
        StatType trainedStat
) {
    public enum Type {
        BLAST,
        BEAM
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
