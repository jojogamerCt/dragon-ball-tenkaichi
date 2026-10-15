package com.dbtenkaichi.client.cinematic;

import com.dbtenkaichi.client.ClientState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class CameraSolver {
    private static final float[] YAWS = {0.0f, 25.0f, -25.0f, 50.0f, -50.0f, 80.0f, -80.0f, 115.0f, -115.0f, 150.0f, -150.0f, 180.0f};
    private static final float[] RAISES = {0.0f, 15.0f, 30.0f, 48.0f};
    private static final double[] HEIGHTS = {0.0, 0.7, -0.5};
    private static final Map<String, float[]> CACHE = new HashMap<>();

    private CameraSolver() {
    }

    public static void reset() {
        CACHE.clear();
    }

    public static Vec3 solve(Level level, Vec3 focus, Vec3 desired, @Nullable Entity ignore, String key) {
        Vec3 offset = desired.subtract(focus);
        double distance = offset.length();
        if (distance < 0.5) {
            return desired;
        }
        long now = ClientState.clientTicks;
        float[] cached = CACHE.get(key);
        if (cached != null) {
            Vec3 candidate = apply(focus, offset, cached[0], cached[1]);
            if (now - (long) cached[2] < 12 || clearance(level, focus, candidate, ignore) >= distance - 0.05) {
                if (now - (long) cached[2] >= 12) {
                    cached[2] = now;
                }
                return pull(level, focus, candidate, ignore);
            }
        }
        float bestYaw = 0.0f;
        float bestRaise = 0.0f;
        double best = -1.0;
        search:
        for (float raise : RAISES) {
            for (float yaw : YAWS) {
                Vec3 candidate = apply(focus, offset, yaw, raise);
                double free = clearance(level, focus, candidate, ignore);
                if (free >= distance - 0.05) {
                    bestYaw = yaw;
                    bestRaise = raise;
                    best = free;
                    break search;
                }
                if (free > best + 0.25) {
                    best = free;
                    bestYaw = yaw;
                    bestRaise = raise;
                }
            }
        }
        CACHE.put(key, new float[]{bestYaw, bestRaise, now});
        if (CACHE.size() > 256) {
            CACHE.clear();
        }
        return pull(level, focus, apply(focus, offset, bestYaw, bestRaise), ignore);
    }

    private static Vec3 apply(Vec3 focus, Vec3 offset, float yawDegrees, float raiseDegrees) {
        double yaw = Math.toRadians(yawDegrees);
        double x = offset.x * Math.cos(yaw) - offset.z * Math.sin(yaw);
        double z = offset.x * Math.sin(yaw) + offset.z * Math.cos(yaw);
        double horizontal = Math.sqrt(x * x + z * z);
        double length = offset.length();
        double elevation = Math.atan2(offset.y, Math.max(1.0e-4, horizontal));
        double raised = Math.min(Math.toRadians(80.0), elevation + Math.toRadians(raiseDegrees));
        double h = Math.cos(raised) * length;
        double scale = horizontal < 1.0e-4 ? 0.0 : h / horizontal;
        return focus.add(x * scale, Math.sin(raised) * length, z * scale);
    }

    private static Vec3 pull(Level level, Vec3 focus, Vec3 camera, @Nullable Entity ignore) {
        double distance = camera.distanceTo(focus);
        double free = clearance(level, focus, camera, ignore);
        if (free >= distance - 0.05) {
            return camera;
        }
        double allowed = Math.max(0.8, free - 0.4);
        return focus.add(camera.subtract(focus).normalize().scale(Math.min(distance, allowed)));
    }

    private static double clearance(Level level, Vec3 focus, Vec3 camera, @Nullable Entity ignore) {
        double distance = camera.distanceTo(focus);
        double free = distance;
        for (double dy : HEIGHTS) {
            Vec3 from = focus.add(0, dy, 0);
            Vec3 to = camera.add(0, dy * 0.5, 0);
            HitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.VISUAL, ClipContext.Fluid.ANY, ignore == null ? net.minecraft.world.phys.shapes.CollisionContext.empty() : net.minecraft.world.phys.shapes.CollisionContext.of(ignore)));
            if (hit.getType() != HitResult.Type.MISS) {
                free = Math.min(free, hit.getLocation().distanceTo(from));
            }
        }
        if (solidAround(level, camera)) {
            free = Math.min(free, distance - 0.6);
        }
        return free;
    }

    private static boolean solidAround(Level level, Vec3 camera) {
        for (double dx = -0.3; dx <= 0.3; dx += 0.6) {
            for (double dy = -0.3; dy <= 0.3; dy += 0.6) {
                for (double dz = -0.3; dz <= 0.3; dz += 0.6) {
                    BlockPos pos = BlockPos.containing(camera.x + dx, camera.y + dy, camera.z + dz);
                    BlockState state = level.getBlockState(pos);
                    if (!state.isAir() && (!state.getCollisionShape(level, pos).isEmpty() || !state.getFluidState().isEmpty())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
