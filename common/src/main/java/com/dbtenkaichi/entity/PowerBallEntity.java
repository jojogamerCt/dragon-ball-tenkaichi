package com.dbtenkaichi.entity;

import com.dbtenkaichi.registry.ModEntities;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class PowerBallEntity extends Entity {
    public static final double RANGE = 96.0;
    public static final int LIFETIME = 1200;
    private static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(PowerBallEntity.class, EntityDataSerializers.FLOAT);

    private int life;
    private double targetY;

    public PowerBallEntity(EntityType<? extends PowerBallEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static PowerBallEntity create(ServerLevel level, LivingEntity owner) {
        PowerBallEntity ball = new PowerBallEntity(ModEntities.POWER_BALL.get(), level);
        Vec3 look = owner.getViewVector(1.0f);
        ball.setPos(owner.getEyePosition().add(look.scale(1.0)));
        ball.targetY = Math.min(level.getMaxY() - 8, owner.getY() + 38.0);
        return ball;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SIZE, 0.4f);
    }

    public float size() {
        return entityData.get(SIZE);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            return;
        }
        life++;
        if (life > LIFETIME) {
            discard();
            return;
        }
        if (getY() < targetY) {
            setPos(getX(), Math.min(targetY, getY() + 0.9), getZ());
        }
        float size = Math.min(7.0f, 0.4f + life * 0.12f);
        if (Math.abs(size - entityData.get(SIZE)) > 0.05f) {
            entityData.set(SIZE, size);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }
}
