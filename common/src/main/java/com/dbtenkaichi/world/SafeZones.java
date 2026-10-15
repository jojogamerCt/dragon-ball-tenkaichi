package com.dbtenkaichi.world;

import com.dbtenkaichi.combat.TenkaichiFighter;
import com.dbtenkaichi.config.ConfigManager;
import com.dbtenkaichi.entity.MasterEntity;
import com.dbtenkaichi.training.TrialManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class SafeZones {
    private static final List<Zone> ZONES = new ArrayList<>();

    private SafeZones() {
    }

    public record Zone(String id, ResourceKey<Level> dimension, int x, int z, int radius, int minY, int maxY) {
        public boolean contains(double px, double py, double pz) {
            if (py < minY || py > maxY + 1) {
                return false;
            }
            double dx = px - (x + 0.5);
            double dz = pz - (z + 0.5);
            return dx * dx + dz * dz <= (radius + 0.5) * (radius + 0.5);
        }

        public AABB bounds() {
            return new AABB(x - radius, minY, z - radius, x + radius + 1, maxY + 1, z + radius + 1);
        }
    }

    static synchronized void set(List<Zone> zones) {
        ZONES.clear();
        ZONES.addAll(zones);
    }

    public static synchronized List<Zone> zones() {
        return List.copyOf(ZONES);
    }

    public static boolean enabled() {
        return ConfigManager.gameplay().protectLandmarks;
    }

    public static synchronized boolean isProtected(Level level, BlockPos pos) {
        return isProtected(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public static synchronized boolean isProtected(Level level, double x, double y, double z) {
        if (!enabled() || ZONES.isEmpty()) {
            return false;
        }
        ResourceKey<Level> dimension = level.dimension();
        for (Zone zone : ZONES) {
            if (zone.dimension == dimension && zone.contains(x, y, z)) {
                return true;
            }
        }
        return false;
    }

    public static synchronized boolean touches(Level level, Vec3 center, double radius) {
        if (!enabled() || ZONES.isEmpty()) {
            return false;
        }
        for (Zone zone : ZONES) {
            if (zone.dimension != level.dimension()) {
                continue;
            }
            if (center.y + radius < zone.minY || center.y - radius > zone.maxY + 1) {
                continue;
            }
            double dx = center.x - (zone.x + 0.5);
            double dz = center.z - (zone.z + 0.5);
            double reach = zone.radius + radius + 1.0;
            if (dx * dx + dz * dz <= reach * reach) {
                return true;
            }
        }
        return false;
    }

    public static boolean blocksPlayer(Level level, Entity entity, BlockPos pos) {
        if (level.isClientSide() || !(entity instanceof Player)) {
            return false;
        }
        if (level instanceof ServerLevel server && TrialManager.isRealm(server)) {
            return enabled();
        }
        return isProtected(level, pos);
    }

    public static boolean deniesSpawn(ServerLevel level, Entity entity) {
        if (!(entity instanceof Mob) || entity instanceof MasterEntity) {
            return false;
        }
        return isProtected(level, entity.getX(), entity.getY(), entity.getZ());
    }

    public static boolean isResident(Entity entity) {
        return entity instanceof MasterEntity || entity instanceof TenkaichiFighter;
    }

    private static boolean keeps(Mob mob) {
        if (isResident(mob) || mob.hasCustomName() || mob.isLeashed() || mob.isPassenger() || mob.isVehicle()) {
            return true;
        }
        return mob instanceof OwnableEntity ownable && ownable.getOwnerReference() != null;
    }

    public static int sweep(ServerLevel level) {
        if (!enabled()) {
            return 0;
        }
        int removed = 0;
        for (Zone zone : zones()) {
            if (zone.dimension != level.dimension()) {
                continue;
            }
            for (Mob mob : level.getEntitiesOfClass(Mob.class, zone.bounds(), m -> m.isAlive() && !keeps(m))) {
                if (zone.contains(mob.getX(), mob.getY(), mob.getZ())) {
                    mob.discard();
                    removed++;
                }
            }
        }
        return removed;
    }
}
