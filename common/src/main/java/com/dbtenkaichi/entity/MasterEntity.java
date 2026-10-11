package com.dbtenkaichi.entity;

import com.dbtenkaichi.anim.AnimClip;
import com.dbtenkaichi.combat.CombatSystem;
import com.dbtenkaichi.entity.ai.FighterMovement;
import com.dbtenkaichi.ki.Techniques;
import com.dbtenkaichi.network.Packets;
import com.dbtenkaichi.platform.Services;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.player.RuntimeState;
import com.dbtenkaichi.race.Race;
import com.dbtenkaichi.registry.ModSounds;
import com.dbtenkaichi.server.PlayerManager;
import com.dbtenkaichi.stats.StatBlock;
import com.dbtenkaichi.stats.StatType;
import com.dbtenkaichi.training.TrainingSystem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class MasterEntity extends FighterNpc {
    private int sparringPartner = -1;
    private int drillPartner = -1;
    private long nextDrillShot;
    private int comboHits;

    public MasterEntity(EntityType<? extends MasterEntity> type, Level level) {
        super(type, level);
        StatBlock block = new StatBlock();
        for (StatType type2 : StatType.all()) {
            block.set(type2, 40);
        }
        applyStats(block, Race.HUMAN);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerData data = PlayerManager.data(serverPlayer);
            if (!data.isCreated()) {
                PlayerManager.openCreator(serverPlayer);
                return InteractionResult.SUCCESS;
            }
            CompoundTag tag = new CompoundTag();
            tag.putString("mode", "menu");
            tag.putInt("master", getId());
            tag.putBoolean("kamehameha_known", data.hasSkill(Techniques.KAMEHAMEHA.id()));
            tag.putBoolean("kamehameha_ready", TrainingSystem.canLearnKamehameha(data));
            tag.putInt("sessions", data.trainingSessions());
            tag.putFloat("fatigue", (float) data.fatigue());
            Services.NETWORK.sendToPlayer(serverPlayer, new Packets.OpenScreen(PlayerManager.SCREEN_TRAINING, tag));
        }
        return InteractionResult.SUCCESS;
    }

    public void startSparring(ServerPlayer player) {
        sparringPartner = player.getId();
        comboHits = 0;
        setTarget(player);
        player.sendSystemMessage(Component.translatable("message.dbtenkaichi.sparring_start"));
    }

    public void startDrill(ServerPlayer player) {
        drillPartner = player.getId();
        nextDrillShot = level().getGameTime() + 40;
        player.sendSystemMessage(Component.translatable("message.dbtenkaichi.drill_start"));
    }

    public void stopSession(ServerPlayer player) {
        if (sparringPartner == player.getId()) {
            sparringPartner = -1;
            setTarget(null);
        }
        if (drillPartner == player.getId()) {
            drillPartner = -1;
        }
        runtime.guarding = false;
        setFighterFlying(false);
    }

    private @Nullable ServerPlayer partner(int id) {
        return id >= 0 && level().getEntity(id) instanceof ServerPlayer player && player.isAlive() ? player : null;
    }

    @Override
    public double outgoingDamageScale() {
        return 0.2;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        long now = level.getGameTime();
        ServerPlayer sparring = partner(sparringPartner);
        if (sparringPartner >= 0 && sparring == null) {
            sparringPartner = -1;
        }
        ServerPlayer drill = partner(drillPartner);
        if (drillPartner >= 0 && drill == null) {
            drillPartner = -1;
        }
        if (sparring != null) {
            sparTick(level, sparring, now);
        } else if (drill != null) {
            drillTick(level, drill, now);
        } else {
            if (getHealth() < getMaxHealth() && now % 10 == 0) {
                heal(2.0f);
            }
            if (isFighterFlying()) {
                setFighterFlying(false);
            }
        }
    }

    private void sparTick(ServerLevel level, ServerPlayer partner, long now) {
        if (isStunned()) {
            return;
        }
        lookAtPoint(partner.getBoundingBox().getCenter());
        double distance = distanceTo(partner);
        boolean fly = !partner.onGround() || Math.abs(partner.getY() - getY()) > 2.0;
        if (fly != isFighterFlying()) {
            setFighterFlying(fly);
        }
        RuntimeState partnerRt = PlayerManager.data(partner).runtime();
        if (distance > 3.0) {
            if (fly) {
                FighterMovement.flyToward(this, partner.getBoundingBox().getCenter(), 0.6, 0.3);
            } else {
                getNavigation().moveTo(partner, 1.1);
            }
            return;
        }
        getNavigation().stop();
        if (partnerRt.lastAttack >= now - 2 && getRandom().nextFloat() < 0.35f) {
            runtime.guarding = true;
            runtime.guardStart = now;
            return;
        }
        runtime.guarding = false;
        if (now >= runtime.nextAttackAllowed && partner.getHealth() > partner.getMaxHealth() * 0.35f) {
            if (comboHits >= 3) {
                comboHits = 0;
                runtime.nextAttackAllowed = now + 30;
                CombatSystem.anim(this, AnimClip.POWER_UP, 10, 0);
                return;
            }
            if (CombatSystem.lightAttack(this, now)) {
                comboHits++;
                runtime.nextAttackAllowed += 6;
            }
        }
    }

    private void drillTick(ServerLevel level, ServerPlayer partner, long now) {
        lookAtPoint(partner.getBoundingBox().getCenter());
        if (distanceTo(partner) < 6.0) {
            Vec3 away = position().subtract(partner.position()).multiply(1, 0, 1).normalize();
            getNavigation().moveTo(getX() + away.x * 4, getY(), getZ() + away.z * 4, 1.0);
        }
        if (now >= nextDrillShot) {
            nextDrillShot = now + 16 + getRandom().nextInt(10);
            KiBlastEntity blast = KiBlastEntity.create(level, this, Techniques.KI_BLAST, 0x7CFFB0, -1, 0.0);
            blast.setDeltaMovement(blast.getDeltaMovement().scale(0.45));
            blast.setTraining(true);
            level.addFreshEntity(blast);
            CombatSystem.anim(this, getRandom().nextBoolean() ? AnimClip.KI_BLAST_LEFT : AnimClip.KI_BLAST_RIGHT, 6, 0);
            CombatSystem.playSound(level, this, ModSounds.KI_BLAST.get(), 0.6f, 1.3f);
            PlayerManager.data(partner).runtime().trainingScore++;
        }
    }

    @Override
    public void onFighterHit(LivingEntity attacker, double damage) {
        super.onFighterHit(attacker, damage);
        if (attacker.getId() == sparringPartner && attacker instanceof ServerPlayer player) {
            PlayerManager.data(player).runtime().trainingScore++;
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.getEntity() == null || source.getEntity().getId() != sparringPartner) {
            return false;
        }
        float capped = Math.min(damage, Math.max(0.0f, getHealth() - 1.0f));
        if (capped <= 0.0f) {
            return false;
        }
        return super.hurtServer(level, source, capped);
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return sparringPartner >= 0;
    }
}
