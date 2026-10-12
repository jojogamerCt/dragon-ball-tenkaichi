package com.dbtenkaichi.entity;

import com.dbtenkaichi.combat.Fighter;
import com.dbtenkaichi.config.ConfigManager;
import com.dbtenkaichi.entity.ai.RaiderBrain;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.race.Race;
import com.dbtenkaichi.registry.ModSounds;
import com.dbtenkaichi.server.PlayerManager;
import com.dbtenkaichi.stats.StatBlock;
import com.dbtenkaichi.stats.StatType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.Nullable;

public class RaiderEntity extends FighterNpc implements Enemy {
    public static final String RAGE_FORM = "dbtenkaichi:raider_rage";

    private final RaiderBrain brain = new RaiderBrain(this);
    private boolean statsRolled;

    public RaiderEntity(EntityType<? extends RaiderEntity> type, Level level) {
        super(type, level);
        this.xpReward = Enemy.XP_REWARD_LARGE;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2)
                .add(Attributes.FLYING_SPEED, 0.6);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 16.0f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true,
                (target, level) -> !(target instanceof Player player && com.dbtenkaichi.race.RaceMechanics.hiddenFromSenses(player, PlayerManager.data(player).race(), this))));
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        rollStats(level.getLevel());
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    private void rollStats(ServerLevel level) {
        if (statsRolled) {
            return;
        }
        statsRolled = true;
        int reference = 6;
        Player nearest = level.getNearestPlayer(this, 64.0);
        if (nearest != null) {
            PlayerData data = PlayerManager.data(nearest);
            if (data.isCreated()) {
                reference = Math.max(4, (int) Math.round(data.stats().total() / (double) StatType.COUNT));
            }
        }
        double scale = difficultyScale();
        StatBlock block = new StatBlock();
        for (StatType type : StatType.all()) {
            double jitter = 0.85 + getRandom().nextDouble() * 0.35;
            block.set(type, (int) Math.max(2, Math.round(reference * 0.92 * jitter * scale)));
        }
        block.add(StatType.STRENGTH, 2);
        block.add(StatType.KI_OUTPUT, 2);
        applyStats(block, Race.SAIYAN);
    }

    public double moveSpeed(boolean enraged) {
        double base = 0.55 + stat(StatType.SPEED) * 0.006;
        return Math.min(ConfigManager.gameplay().maxFlightSpeed, base * (enraged ? 1.35 : 1.0));
    }

    public void applyRage() {
        setFighterForm(RAGE_FORM);
        for (StatType type : new StatType[]{StatType.STRENGTH, StatType.SPEED, StatType.KI_OUTPUT, StatType.AGILITY}) {
            stats.add(type, Math.max(2, stats.get(type) / 4));
        }
        ki = maxKi();
        heal(getMaxHealth() * 0.15f);
    }

    @Override
    public double outgoingDamageScale() {
        return 0.6;
    }

    public RaiderBrain brain() {
        return brain;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (!statsRolled) {
            rollStats(level);
        }
        brain.tick(level);
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return getTarget() != null ? ModSounds.VOICE_GRUNT.get() : null;
    }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource source) {
        super.die(source);
        if (level() instanceof ServerLevel serverLevel) {
            com.dbtenkaichi.server.ServerEvents.onFighterDefeated(serverLevel, this, Fighter.of(this).battlePower());
        }
    }
}
