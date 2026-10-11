package com.dbtenkaichi.client.input;

import com.dbtenkaichi.config.ConfigManager;
import com.dbtenkaichi.network.ActionType;
import com.dbtenkaichi.network.Packets;
import com.dbtenkaichi.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class LockOn {
    private static int target = -1;
    private static int lostTicks;

    private LockOn() {
    }

    public static int target() {
        return target;
    }

    public static @Nullable LivingEntity targetEntity() {
        Minecraft mc = Minecraft.getInstance();
        if (target < 0 || mc.level == null) {
            return null;
        }
        return mc.level.getEntity(target) instanceof LivingEntity living ? living : null;
    }

    public static void toggle() {
        if (target >= 0) {
            release();
            return;
        }
        List<LivingEntity> candidates = candidates();
        if (!candidates.isEmpty()) {
            set(candidates.get(0).getId());
        }
    }

    public static void next() {
        List<LivingEntity> candidates = candidates();
        if (candidates.isEmpty()) {
            return;
        }
        int index = 0;
        for (int i = 0; i < candidates.size(); i++) {
            if (candidates.get(i).getId() == target) {
                index = (i + 1) % candidates.size();
                break;
            }
        }
        set(candidates.get(index).getId());
    }

    public static void reset() {
        target = -1;
        lostTicks = 0;
    }

    public static void release() {
        if (target >= 0) {
            target = -1;
            Services.NETWORK.sendToServer(new Packets.Action(ActionType.LOCK_TARGET.ordinal(), -1));
        }
    }

    private static void set(int id) {
        target = id;
        lostTicks = 0;
        Services.NETWORK.sendToServer(new Packets.Action(ActionType.LOCK_TARGET.ordinal(), id));
    }

    private static List<LivingEntity> candidates() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        List<LivingEntity> out = new ArrayList<>();
        if (player == null || mc.level == null) {
            return out;
        }
        double range = ConfigManager.gameplay().maxLockOnDistance;
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0f);
        AABB box = player.getBoundingBox().inflate(range);
        for (Entity entity : mc.level.getEntities(player, box, e -> e instanceof LivingEntity living && living.isAlive() && !e.isSpectator())) {
            LivingEntity living = (LivingEntity) entity;
            Vec3 to = living.getBoundingBox().getCenter().subtract(eye);
            double dist = to.length();
            if (dist > range || dist < 0.5) {
                continue;
            }
            if (to.scale(1.0 / dist).dot(look) < 0.82) {
                continue;
            }
            if (!visible(player, living)) {
                continue;
            }
            out.add(living);
        }
        out.sort(Comparator.comparingDouble(e -> -e.getBoundingBox().getCenter().subtract(eye).normalize().dot(look) + e.distanceTo(player) * 0.002));
        return out;
    }

    private static boolean visible(LocalPlayer player, Entity entity) {
        Vec3 start = player.getEyePosition();
        Vec3 end = entity.getBoundingBox().getCenter();
        HitResult hit = player.level().clip(new ClipContext(start, end, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.MISS;
    }

    public static void tick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (target < 0 || player == null) {
            return;
        }
        LivingEntity entity = targetEntity();
        double range = ConfigManager.gameplay().maxLockOnDistance + 8.0;
        if (entity == null || !entity.isAlive() || entity.distanceTo(player) > range) {
            release();
            return;
        }
        if (!visible(player, entity)) {
            if (++lostTicks > 40) {
                release();
                return;
            }
        } else {
            lostTicks = 0;
        }
        Vec3 to = entity.getBoundingBox().getCenter().subtract(player.getEyePosition());
        float desiredYaw = (float) (Mth.atan2(to.z, to.x) * (180.0 / Math.PI)) - 90.0f;
        float desiredPitch = (float) (-(Mth.atan2(to.y, to.horizontalDistance()) * (180.0 / Math.PI)));
        float strength = Math.max(0.05f, Math.min(1.0f, ConfigManager.client().lockOnCameraStrength));
        float yaw = player.getYRot() + Mth.wrapDegrees(desiredYaw - player.getYRot()) * strength;
        float pitch = player.getXRot() + (desiredPitch - player.getXRot()) * strength;
        player.setYRot(yaw);
        player.setXRot(Mth.clamp(pitch, -89.0f, 89.0f));
        player.setYHeadRot(yaw);
    }
}
