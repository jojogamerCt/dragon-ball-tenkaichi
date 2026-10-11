package com.dbtenkaichi.entity;

import com.dbtenkaichi.combat.CombatSystem;
import com.dbtenkaichi.combat.TenkaichiFighter;
import com.dbtenkaichi.config.ConfigManager;
import com.dbtenkaichi.ki.BeamClashManager;
import com.dbtenkaichi.player.RuntimeState;
import com.dbtenkaichi.race.Race;
import com.dbtenkaichi.stats.DerivedStats;
import com.dbtenkaichi.stats.StatBlock;
import com.dbtenkaichi.stats.StatType;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class FighterNpc extends PathfinderMob implements TenkaichiFighter {
    private static final EntityDataAccessor<String> FORM = SynchedEntityData.defineId(FighterNpc.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> FLAGS = SynchedEntityData.defineId(FighterNpc.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> KI_RATIO = SynchedEntityData.defineId(FighterNpc.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> FLYING = SynchedEntityData.defineId(FighterNpc.class, EntityDataSerializers.BOOLEAN);

    protected final RuntimeState runtime = new RuntimeState();
    protected StatBlock stats = new StatBlock();
    protected Race race = Race.HUMAN;
    protected double ki = 100.0;

    protected FighterNpc(EntityType<? extends FighterNpc> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FORM, "");
        builder.define(FLAGS, 0);
        builder.define(KI_RATIO, 1.0f);
        builder.define(FLYING, false);
    }

    public int visualFlags() {
        return entityData.get(FLAGS);
    }

    public String visualForm() {
        return entityData.get(FORM);
    }

    public float visualKiRatio() {
        return entityData.get(KI_RATIO);
    }

    public boolean isFighterFlying() {
        return entityData.get(FLYING);
    }

    public void setFighterFlying(boolean flying) {
        entityData.set(FLYING, flying);
        setNoGravity(flying);
        runtime.flying = flying;
        if (flying) {
            resetFallDistance();
        }
    }

    protected void setFighterForm(@Nullable String form) {
        entityData.set(FORM, form == null ? "" : form);
    }

    public double maxKi() {
        return DerivedStats.maxKi(stats, race, ConfigManager.balance());
    }

    public double ki() {
        return ki;
    }

    @Override
    public StatBlock fighterStats() {
        return stats;
    }

    @Override
    public Race fighterRace() {
        return race;
    }

    @Override
    public @Nullable String fighterForm() {
        String form = entityData.get(FORM);
        return form.isEmpty() ? null : form;
    }

    @Override
    public float fighterMastery() {
        return 0.5f;
    }

    @Override
    public double fighterKiRatio() {
        return ki / Math.max(1.0, maxKi());
    }

    @Override
    public RuntimeState fighterRuntime() {
        return runtime;
    }

    @Override
    public boolean spendFighterKi(double amount) {
        if (ki < amount) {
            return false;
        }
        ki -= amount;
        return true;
    }

    @Override
    public void onFighterHit(LivingEntity attacker, double damage) {
        BeamClashManager.onAttacked(this, level().getGameTime());
    }

    protected void applyStats(StatBlock block, Race race) {
        this.stats = block;
        this.race = race;
        double hp = 20.0 + DerivedStats.bonusHealth(block, ConfigManager.balance());
        var attr = getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(hp);
        }
        setHealth(getMaxHealth());
        ki = maxKi();
    }

    protected double difficultyScale() {
        double base = ConfigManager.gameplay().npcDifficulty;
        int players = level().getEntitiesOfClass(net.minecraft.world.entity.player.Player.class, getBoundingBox().inflate(32.0)).size();
        return base * (1.0 + Math.max(0, players - 1) * ConfigManager.gameplay().npcMultiplayerScaling);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            long now = level().getGameTime();
            CombatSystem.tickFighter(this, runtime, now);
            double regen = DerivedStats.kiRegenPerSecond(stats, race, ConfigManager.balance()) / 20.0;
            if (runtime.charging) {
                regen = DerivedStats.kiChargePerSecond(stats, race, ConfigManager.balance()) / 20.0;
            }
            ki = Math.min(maxKi(), ki + regen);
            if (runtime.flying) {
                resetFallDistance();
            }
            int flags = runtime.flags(now);
            if (flags != entityData.get(FLAGS)) {
                entityData.set(FLAGS, flags);
            }
            float ratio = (float) fighterKiRatio();
            if (Math.abs(ratio - entityData.get(KI_RATIO)) > 0.02f) {
                entityData.set(KI_RATIO, ratio);
            }
        }
    }

    public boolean isStunned() {
        return runtime.stunnedUntil > level().getGameTime();
    }

    public void lookAtPoint(Vec3 target) {
        Vec3 eye = getEyePosition();
        Vec3 d = target.subtract(eye);
        double horizontal = Math.sqrt(d.x * d.x + d.z * d.z);
        float yaw = (float) (Math.toDegrees(Math.atan2(d.z, d.x)) - 90.0);
        float pitch = (float) (-Math.toDegrees(Math.atan2(d.y, horizontal)));
        setYRot(yaw);
        setYHeadRot(yaw);
        setYBodyRot(yaw);
        setXRot(pitch);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (runtime.invulnerableUntil > level.getGameTime()) {
            return false;
        }
        boolean result = super.hurtServer(level, source, damage);
        if (result && source.getEntity() instanceof LivingEntity attacker) {
            runtime.lastCombat = level.getGameTime();
            BeamClashManager.onAttacked(this, level.getGameTime());
        }
        return result;
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putIntArray("tenkaichi_stats", stats.toArray());
        output.putString("tenkaichi_race", race.id());
        output.putDouble("tenkaichi_ki", ki);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getIntArray("tenkaichi_stats").ifPresent(arr -> stats = new StatBlock(arr));
        Race loaded = Race.byId(input.getStringOr("tenkaichi_race", race.id()));
        if (loaded != null) {
            race = loaded;
        }
        ki = input.getDoubleOr("tenkaichi_ki", maxKi());
    }

    public int stat(StatType type) {
        return stats.get(type);
    }
}
