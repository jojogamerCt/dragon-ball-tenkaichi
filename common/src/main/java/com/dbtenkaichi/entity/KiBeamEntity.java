package com.dbtenkaichi.entity;

import com.dbtenkaichi.combat.CombatSystem;
import com.dbtenkaichi.combat.DamageMath;
import com.dbtenkaichi.combat.Fighter;
import com.dbtenkaichi.combat.TenkaichiDamage;
import com.dbtenkaichi.config.BalanceConfig;
import com.dbtenkaichi.config.ConfigManager;
import com.dbtenkaichi.ki.BeamClashManager;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KiBeamEntity extends Entity {
    public static final byte STATE_FIRING = 1;
    public static final byte STATE_CLASHING = 2;
    public static final byte STATE_FADING = 3;

    private static final EntityDataAccessor<Integer> OWNER = SynchedEntityData.defineId(KiBeamEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> TECHNIQUE = SynchedEntityData.defineId(KiBeamEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Byte> STATE = SynchedEntityData.defineId(KiBeamEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Float> LENGTH = SynchedEntityData.defineId(KiBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(KiBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_X = SynchedEntityData.defineId(KiBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Y = SynchedEntityData.defineId(KiBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Z = SynchedEntityData.defineId(KiBeamEntity.class, EntityDataSerializers.FLOAT);
    public String techniqueId() {
        return entityData.get(TECHNIQUE);
    }

    private static final EntityDataAccessor<Integer> CORE = SynchedEntityData.defineId(KiBeamEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GLOW = SynchedEntityData.defineId(KiBeamEntity.class, EntityDataSerializers.INT);

    private final Map<Integer, Long> hitCooldowns = new HashMap<>();
    private double chargeRatio;
    private int firingTicks;
    private int fadeTicks;
    private int blockedTicks;
    private long nextExplosion;
    public float clientLength;
    public float clientLengthO;

    public KiBeamEntity(EntityType<? extends KiBeamEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static KiBeamEntity create(ServerLevel level, LivingEntity owner, Technique technique, double chargeRatio) {
        KiBeamEntity beam = new KiBeamEntity(ModEntities.KI_BEAM.get(), level);
        beam.chargeRatio = chargeRatio;
        beam.entityData.set(OWNER, owner.getId());
        beam.entityData.set(TECHNIQUE, technique.id());
        beam.entityData.set(STATE, STATE_FIRING);
        beam.entityData.set(RADIUS, (float) technique.radiusAt(chargeRatio));
        Vec3 look = owner.getViewVector(1.0f);
        beam.setDirection(look);
        beam.entityData.set(CORE, technique.coreColor());
        beam.entityData.set(GLOW, technique.glowColor());
        beam.setPos(origin(owner, look));
        return beam;
    }

    private static Vec3 origin(LivingEntity owner, Vec3 look) {
        return owner.getEyePosition().add(0, -0.35 * owner.getScale(), 0).add(look.scale(0.9 * owner.getScale()));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER, -1);
        builder.define(TECHNIQUE, Techniques.KAMEHAMEHA.id());
        builder.define(STATE, STATE_FIRING);
        builder.define(LENGTH, 0.0f);
        builder.define(RADIUS, 0.5f);
        builder.define(DIR_X, 0.0f);
        builder.define(DIR_Y, 0.0f);
        builder.define(DIR_Z, 1.0f);
        builder.define(CORE, 0xFFFFFF);
        builder.define(GLOW, 0x3FA8FF);
    }

    public int ownerId() {
        return entityData.get(OWNER);
    }

    public @Nullable LivingEntity owner() {
        return level().getEntity(ownerId()) instanceof LivingEntity living ? living : null;
    }

    public Technique technique() {
        Technique technique = Techniques.get(entityData.get(TECHNIQUE));
        return technique == null ? Techniques.KAMEHAMEHA : technique;
    }

    public byte state() {
        return entityData.get(STATE);
    }

    public void setState(byte state) {
        entityData.set(STATE, state);
    }

    public float length() {
        return entityData.get(LENGTH);
    }

    public void setLength(float length) {
        entityData.set(LENGTH, Math.max(0.0f, length));
    }

    public float radius() {
        return entityData.get(RADIUS);
    }

    public int coreColor() {
        return entityData.get(CORE);
    }

    public int glowColor() {
        return entityData.get(GLOW);
    }

    public double chargeRatio() {
        return chargeRatio;
    }

    public Vec3 direction() {
        return new Vec3(entityData.get(DIR_X), entityData.get(DIR_Y), entityData.get(DIR_Z));
    }

    public void setDirection(Vec3 dir) {
        Vec3 n = dir.lengthSqr() < 1.0e-6 ? new Vec3(0, 0, 1) : dir.normalize();
        entityData.set(DIR_X, (float) n.x);
        entityData.set(DIR_Y, (float) n.y);
        entityData.set(DIR_Z, (float) n.z);
    }

    public Vec3 head() {
        return position().add(direction().scale(length()));
    }

    public void fade() {
        if (state() != STATE_FADING) {
            setState(STATE_FADING);
            fadeTicks = 0;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            clientLengthO = clientLength;
            float target = length();
            clientLength = clientLength + (target - clientLength) * 0.6f;
            if (Math.abs(target - clientLength) < 0.05f) {
                clientLength = target;
            }
            return;
        }
        ServerLevel level = (ServerLevel) level();
        LivingEntity owner = owner();
        Technique technique = technique();
        if (state() == STATE_FADING) {
            if (++fadeTicks > 8) {
                release(owner);
                discard();
            }
            return;
        }
        if (owner == null || !owner.isAlive() || owner.level() != level) {
            fade();
            return;
        }
        Vec3 look = owner.getViewVector(1.0f);
        Vec3 dir = direction();
        double steer = technique.steer() * 0.12;
        setDirection(dir.add(look.subtract(dir).scale(steer)));
        dir = direction();
        setPos(origin(owner, dir));
        if (state() == STATE_CLASHING) {
            return;
        }
        firingTicks++;
        if (firingTicks > technique.duration() + (int) (chargeRatio * 20)) {
            fade();
            return;
        }
        float length = Math.min(technique.maxLength(), length() + (float) technique.speed());
        Vec3 start = position();
        Vec3 end = start.add(dir.scale(length));
        BlockHitResult blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        boolean blocked = blockHit.getType() == HitResult.Type.BLOCK;
        if (blocked) {
            length = (float) start.distanceTo(blockHit.getLocation());
        }
        LivingEntity stopper = hurtAlong(level, owner, technique, start, dir, length);
        if (stopper != null && !technique.penetratesEntities()) {
            length = (float) Math.max(0.5, start.distanceTo(stopper.getBoundingBox().getCenter()));
            explodeAt(level, owner, technique, start.add(dir.scale(length)));
            fade();
            setLength(length);
            return;
        }
        setLength(length);
        if (blocked) {
            blockedTicks++;
            long now = level.getGameTime();
            if (now >= nextExplosion) {
                nextExplosion = now + (technique.trench() ? 5 : 12);
                explodeAt(level, owner, technique, blockHit.getLocation());
            }
            if (!technique.trench() && blockedTicks > 20) {
                fade();
            }
        }
        if (technique.clashable()) {
            BeamClashManager.detect(level, this);
        }
        if (firingTicks % 10 == 0) {
            level.playSound(null, head().x, head().y, head().z, ModSounds.BEAM_LOOP.get(), SoundSource.PLAYERS, 1.2f, 1.0f);
        }
    }

    private @Nullable LivingEntity hurtAlong(ServerLevel level, LivingEntity owner, Technique technique, Vec3 start, Vec3 dir, double length) {
        double radius = radius();
        Vec3 end = start.add(dir.scale(length));
        AABB box = new AABB(start, end).inflate(radius + 0.5);
        List<LivingEntity> victims = level.getEntitiesOfClass(LivingEntity.class, box, e -> e != owner && e.isAlive() && !e.isSpectator());
        long now = level.getGameTime();
        LivingEntity first = null;
        double firstDist = Double.MAX_VALUE;
        BalanceConfig cfg = ConfigManager.balance();
        Fighter attacker = Fighter.of(owner);
        for (LivingEntity victim : victims) {
            if (!CombatSystem.canHarm(owner, victim)) {
                continue;
            }
            Vec3 center = victim.getBoundingBox().getCenter();
            double t = Math.max(0.0, Math.min(length, center.subtract(start).dot(dir)));
            Vec3 closest = start.add(dir.scale(t));
            double reach = radius + victim.getBbWidth() * 0.5;
            if (closest.distanceToSqr(center) > reach * reach + victim.getBbHeight() * 0.25) {
                continue;
            }
            if (t < firstDist) {
                firstDist = t;
                first = victim;
            }
            Long last = hitCooldowns.get(victim.getId());
            if (last != null && now - last < 5) {
                continue;
            }
            hitCooldowns.put(victim.getId(), now);
            RuntimeState rt = CombatSystem.runtime(victim);
            if (rt != null && rt.invulnerableUntil > now) {
                continue;
            }
            Fighter defender = Fighter.of(victim);
            DamageMath.GuardState guard = rt != null && rt.guarding && CombatSystem.facing(victim, owner) ? DamageMath.GuardState.GUARD : DamageMath.GuardState.NONE;
            double raw = DamageMath.ki(technique.baseDamage(), attacker.stat(StatType.KI_OUTPUT), chargeRatio, attacker.kiMultiplier(cfg), cfg) * 0.3;
            double damage = DamageMath.finalDamage(raw, attacker.battlePower(), defender.battlePower(), defender.stat(StatType.DEFENSE), defender.defenseMultiplier(cfg), guard, defender.fatigue(), cfg);
            damage *= 1.0 - Math.min(0.5, defender.stat(StatType.SPIRIT) * 0.002);
            if (guard == DamageMath.GuardState.GUARD && victim instanceof net.minecraft.server.level.ServerPlayer sp) {
                var data = com.dbtenkaichi.server.PlayerManager.data(sp);
                if (!com.dbtenkaichi.server.PlayerManager.spendStamina(data, 4.0 + raw * 0.5)) {
                    rt.guarding = false;
                    rt.stunnedUntil = now + 20;
                    rt.stateDirty = true;
                }
            }
            DamageSource source = TenkaichiDamage.source(level, TenkaichiDamage.KI_BEAM, this, owner);
            TenkaichiDamage.hurt(victim, level, source, (float) damage);
            CombatSystem.knockback(victim, dir.scale(guard == DamageMath.GuardState.GUARD ? 0.25 : 0.7).add(0, 0.05, 0));
            if (rt != null) {
                rt.lastCombat = now;
                rt.charging = false;
                rt.stunnedUntil = Math.max(rt.stunnedUntil, now + (guard == DamageMath.GuardState.GUARD ? 3 : 8));
            }
            RuntimeState ownerRt = CombatSystem.runtime(owner);
            if (ownerRt != null) {
                ownerRt.lastCombat = now;
                ownerRt.damageDealt.merge(victim.getId(), damage, Double::sum);
            }
            if (victim instanceof com.dbtenkaichi.combat.TenkaichiFighter npc) {
                npc.onFighterHit(owner, damage);
            }
        }
        return first;
    }

    private void explodeAt(ServerLevel level, LivingEntity owner, Technique technique, Vec3 pos) {
        DestructionManager.explode(level, pos, technique.explosionAt(chargeRatio), owner, technique.baseDamage() * 0.6, chargeRatio);
    }

    public void onClashLost() {
        fade();
    }

    public void onClashWon(double remaining) {
        setState(STATE_FIRING);
        firingTicks = Math.max(0, technique().duration() - 20);
        chargeRatio = Math.max(0.2, Math.min(1.0, remaining));
    }

    private void release(@Nullable LivingEntity owner) {
        if (owner == null) {
            return;
        }
        RuntimeState rt = CombatSystem.runtime(owner);
        if (rt != null && rt.activeBeam == getId()) {
            rt.activeBeam = -1;
            rt.stateDirty = true;
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide()) {
            release(owner());
            BeamClashManager.onBeamRemoved(this);
        }
        super.remove(reason);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 256.0 * 256.0;
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
    public boolean isPushable() {
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
