package com.dbtenkaichi.entity;

import com.dbtenkaichi.combat.CombatSystem;
import com.dbtenkaichi.combat.DamageMath;
import com.dbtenkaichi.combat.Fighter;
import com.dbtenkaichi.combat.TenkaichiDamage;
import com.dbtenkaichi.combat.TenkaichiFighter;
import com.dbtenkaichi.config.BalanceConfig;
import com.dbtenkaichi.config.ConfigManager;
import com.dbtenkaichi.ki.Technique;
import com.dbtenkaichi.ki.Techniques;
import com.dbtenkaichi.player.RuntimeState;
import com.dbtenkaichi.registry.ModEntities;
import com.dbtenkaichi.registry.ModSounds;
import com.dbtenkaichi.stats.StatType;
import com.dbtenkaichi.world.DestructionManager;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class KiBlastEntity extends Entity {
    private static final EntityDataAccessor<Integer> OWNER = SynchedEntityData.defineId(KiBlastEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(KiBlastEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(KiBlastEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> TRAINING = SynchedEntityData.defineId(KiBlastEntity.class, EntityDataSerializers.BOOLEAN);

    private int life;
    private int homingTarget = -1;
    private double power = 1.0;
    private Technique technique = Techniques.KI_BLAST;

    public KiBlastEntity(EntityType<? extends KiBlastEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static KiBlastEntity create(ServerLevel level, LivingEntity owner, Technique technique, int color, int homingTarget, double power) {
        KiBlastEntity blast = new KiBlastEntity(ModEntities.KI_BLAST.get(), level);
        Vec3 look = owner.getViewVector(1.0f);
        Vec3 side = look.cross(new Vec3(0, 1, 0));
        side = side.lengthSqr() < 1.0e-6 ? Vec3.ZERO : side.normalize().scale((owner.getRandom().nextBoolean() ? 0.3 : -0.3) * owner.getScale());
        blast.setPos(owner.getEyePosition().add(0, -0.3 * owner.getScale(), 0).add(look.scale(0.8)).add(side));
        blast.setDeltaMovement(look.scale(technique.speed()));
        blast.entityData.set(OWNER, owner.getId());
        blast.entityData.set(COLOR, color);
        blast.entityData.set(SIZE, (float) technique.radius());
        blast.homingTarget = homingTarget;
        blast.power = power;
        blast.technique = technique;
        return blast;
    }

    public void setTraining(boolean training) {
        entityData.set(TRAINING, training);
    }

    public boolean isTraining() {
        return entityData.get(TRAINING);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER, -1);
        builder.define(COLOR, 0x9FD8FF);
        builder.define(SIZE, 0.28f);
        builder.define(TRAINING, false);
    }

    public int color() {
        return entityData.get(COLOR);
    }

    public float size() {
        return entityData.get(SIZE);
    }

    public @Nullable LivingEntity owner() {
        return level().getEntity(entityData.get(OWNER)) instanceof LivingEntity living ? living : null;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = getDeltaMovement();
        if (level().isClientSide()) {
            setPos(position().add(motion));
            return;
        }
        ServerLevel level = (ServerLevel) level();
        if (++life > technique.duration()) {
            discard();
            return;
        }
        if (homingTarget >= 0 && level.getEntity(homingTarget) instanceof LivingEntity target && target.isAlive()) {
            Vec3 want = target.getBoundingBox().getCenter().subtract(position()).normalize().scale(motion.length());
            motion = motion.add(want.subtract(motion).scale(technique.steer()));
            setDeltaMovement(motion);
        }
        Vec3 start = position();
        Vec3 end = start.add(motion);
        BlockHitResult blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        Vec3 stop = blockHit.getType() == HitResult.Type.BLOCK ? blockHit.getLocation() : end;
        LivingEntity owner = owner();
        AABB sweep = new AABB(start, stop).inflate(size() + 0.3);
        LivingEntity hit = null;
        double best = Double.MAX_VALUE;
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, sweep, e -> e != owner && e.isAlive() && !e.isSpectator())) {
            if (owner != null && !CombatSystem.canHarm(owner, candidate)) {
                continue;
            }
            var clip = candidate.getBoundingBox().inflate(size()).clip(start, stop);
            if (clip.isPresent()) {
                double d = start.distanceToSqr(clip.get());
                if (d < best) {
                    best = d;
                    hit = candidate;
                }
            }
        }
        if (hit != null) {
            impact(level, owner, hit, hit.getBoundingBox().getCenter());
            return;
        }
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            impact(level, owner, null, blockHit.getLocation());
            return;
        }
        setPos(end);
    }

    private void impact(ServerLevel level, @Nullable LivingEntity owner, @Nullable LivingEntity target, Vec3 pos) {
        long now = level.getGameTime();
        if (target != null) {
            RuntimeState rt = CombatSystem.runtime(target);
            if (rt != null && rt.invulnerableUntil > now) {
                setPos(pos.add(getDeltaMovement()));
                return;
            }
            BalanceConfig cfg = ConfigManager.balance();
            Fighter defender = Fighter.of(target);
            DamageMath.GuardState guard = rt != null && rt.guarding && owner != null && CombatSystem.facing(target, owner) ? DamageMath.GuardState.GUARD : DamageMath.GuardState.NONE;
            if (isTraining()) {
                if (rt != null) {
                    rt.trainingHits++;
                }
                TenkaichiDamage.hurt(target, level, TenkaichiDamage.source(level, TenkaichiDamage.KI_BLAST, this, owner), 1.0f);
            } else {
                Fighter attacker = owner == null ? defender : Fighter.of(owner);
                double raw = DamageMath.ki(technique.baseDamage() * power, attacker.stat(StatType.KI_OUTPUT), 1.0, attacker.kiMultiplier(cfg), cfg);
                double damage = DamageMath.finalDamage(raw, attacker.battlePower(), defender.battlePower(), defender.stat(StatType.DEFENSE), defender.defenseMultiplier(cfg), guard, defender.fatigue(), cfg);
                DamageSource source = TenkaichiDamage.source(level, TenkaichiDamage.KI_BLAST, this, owner);
                TenkaichiDamage.hurt(target, level, source, (float) damage);
                CombatSystem.knockback(target, getDeltaMovement().normalize().scale(guard == DamageMath.GuardState.GUARD ? 0.1 : 0.35).add(0, 0.08, 0));
                if (rt != null) {
                    rt.lastCombat = now;
                    rt.charging = false;
                    if (rt.poiseUntil <= now) {
                        rt.stunnedUntil = Math.max(rt.stunnedUntil, now + (guard == DamageMath.GuardState.GUARD ? 2 : 5));
                    }
                }
                if (owner != null) {
                    RuntimeState ownerRt = CombatSystem.runtime(owner);
                    if (ownerRt != null) {
                        ownerRt.lastCombat = now;
                        ownerRt.damageDealt.merge(target.getId(), damage, Double::sum);
                    }
                }
                if (target instanceof TenkaichiFighter npc && owner != null) {
                    npc.onFighterHit(owner, damage);
                }
            }
        }
        if (!isTraining()) {
            if (com.dbtenkaichi.ki.SkillStyle.of(technique.id()) == com.dbtenkaichi.ki.SkillStyle.BIG_BANG) {
                DestructionManager.crater(level, pos, technique.explosionRadius(), owner, technique.baseDamage() * 0.5, 1.0);
            } else {
                DestructionManager.explode(level, pos, technique.explosionRadius(), owner, 0.0, 0.0);
            }
        } else {
            level.playSound(null, pos.x, pos.y, pos.z, ModSounds.KI_IMPACT.get(), SoundSource.PLAYERS, 0.6f, 1.4f);
        }
        discard();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
