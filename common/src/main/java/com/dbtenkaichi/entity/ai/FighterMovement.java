package com.dbtenkaichi.entity.ai;

import com.dbtenkaichi.entity.FighterNpc;
import net.minecraft.world.phys.Vec3;

public final class FighterMovement {
    private FighterMovement() {
    }

    public static void flyToward(FighterNpc npc, Vec3 target, double speed, double accel) {
        Vec3 to = target.subtract(npc.position());
        double dist = to.length();
        Vec3 desired = dist < 1.0e-3 ? Vec3.ZERO : to.scale(Math.min(speed, dist * 0.35) / dist);
        steer(npc, desired, accel);
    }

    public static void steer(FighterNpc npc, Vec3 desired, double accel) {
        Vec3 current = npc.getDeltaMovement();
        Vec3 next = current.add(desired.subtract(current).scale(Math.max(0.0, Math.min(1.0, accel))));
        npc.setDeltaMovement(next);
    }

    public static void hover(FighterNpc npc) {
        steer(npc, Vec3.ZERO, 0.25);
    }

    public static Vec3 orbitPoint(FighterNpc npc, Vec3 center, double radius, double height, boolean clockwise) {
        Vec3 rel = npc.position().subtract(center).multiply(1, 0, 1);
        if (rel.lengthSqr() < 1.0e-4) {
            rel = new Vec3(1, 0, 0);
        }
        rel = rel.normalize();
        Vec3 tangent = clockwise ? new Vec3(-rel.z, 0, rel.x) : new Vec3(rel.z, 0, -rel.x);
        return center.add(rel.scale(radius)).add(tangent.scale(radius * 0.6)).add(0, height, 0);
    }

    public static boolean needsFlight(FighterNpc npc, Vec3 target) {
        return Math.abs(target.y - npc.getY()) > 2.5 || !npc.onGround() && npc.isFighterFlying();
    }
}
