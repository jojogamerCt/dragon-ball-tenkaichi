package com.dbtenkaichi.entity;

import com.dbtenkaichi.config.ConfigManager;
import com.dbtenkaichi.entity.ai.BrainHost;
import com.dbtenkaichi.entity.ai.FighterMovement;
import com.dbtenkaichi.entity.ai.RaiderBrain;
import com.dbtenkaichi.form.FormDefinition;
import com.dbtenkaichi.form.FormRegistry;
import com.dbtenkaichi.ki.Technique;
import com.dbtenkaichi.ki.Techniques;
import com.dbtenkaichi.network.Packets;
import com.dbtenkaichi.platform.Services;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.registry.ModEntities;
import com.dbtenkaichi.server.PlayerManager;
import com.dbtenkaichi.stats.StatBlock;
import com.dbtenkaichi.stats.StatType;
import com.dbtenkaichi.story.StoryCharacters;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class StoryNpc extends FighterNpc implements BrainHost {
    public static final int ROLE_ACTOR = 0;
    public static final int ROLE_ENEMY = 1;
    public static final int ROLE_ALLY = 2;

    private static final EntityDataAccessor<String> CHARACTER = SynchedEntityData.defineId(StoryNpc.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> ROLE = SynchedEntityData.defineId(StoryNpc.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> TAIL = SynchedEntityData.defineId(StoryNpc.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HIDDEN = SynchedEntityData.defineId(StoryNpc.class, EntityDataSerializers.BOOLEAN);

    private final RaiderBrain brain = new RaiderBrain(this);
    private @Nullable UUID owner;
    private float bossThreshold = -1.0f;
    private boolean defeated;
    private boolean tailCut;
    private int lifetime = -1;
    private @Nullable Vec3 moveTarget;
    private double moveSpeed = 0.4;
    private int lookTarget = -1;
    private String tag = "";
    private @Nullable Vec3 fleeCenter;
    private double fleeRadius;
    private boolean fleeFly;

    public StoryNpc(EntityType<? extends StoryNpc> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2)
                .add(Attributes.FLYING_SPEED, 0.6);
    }

    public static @Nullable StoryNpc spawn(ServerLevel level, StoryCharacters.Character character, Vec3 pos, int role, @Nullable ServerPlayer owner, double powerScale) {
        StoryNpc npc = new StoryNpc(ModEntities.STORY_NPC.get(), level);
        npc.setPos(pos.x, pos.y, pos.z);
        npc.setup(character, role, owner, powerScale);
        if (!level.addFreshEntity(npc)) {
            return null;
        }
        return npc;
    }

    public void setup(StoryCharacters.Character character, int role, @Nullable ServerPlayer owner, double powerScale) {
        entityData.set(CHARACTER, character.id());
        entityData.set(ROLE, role);
        entityData.set(TAIL, character.appearance().tail);
        this.owner = owner == null ? null : owner.getUUID();
        StatBlock block = new StatBlock();
        int reference = 6;
        if (owner != null) {
            PlayerData data = PlayerManager.data(owner);
            if (data.isCreated()) {
                reference = Math.max(4, (int) Math.round(data.stats().total() / (double) StatType.COUNT));
            }
        }
        double scale = character.power() * powerScale * ConfigManager.gameplay().npcDifficulty;
        for (StatType type : StatType.all()) {
            block.set(type, (int) Math.max(2, Math.round(reference * scale)));
        }
        applyStats(block, character.race());
        setCustomName(net.minecraft.network.chat.Component.translatable(character.translationKey()));
        setCustomNameVisible(role != ROLE_ACTOR);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CHARACTER, "");
        builder.define(ROLE, ROLE_ACTOR);
        builder.define(TAIL, false);
        builder.define(HIDDEN, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 12.0f));
    }

    public String characterId() {
        return entityData.get(CHARACTER);
    }

    public StoryCharacters.@Nullable Character character() {
        return StoryCharacters.get(characterId());
    }

    public int role() {
        return entityData.get(ROLE);
    }

    public void setRole(int role) {
        entityData.set(ROLE, role);
        setCustomNameVisible(role != ROLE_ACTOR);
        if (role == ROLE_ACTOR) {
            setTarget(null);
            fighterRuntime().guarding = false;
            fighterRuntime().charging = false;
        }
    }

    public boolean hostile() {
        return role() == ROLE_ENEMY;
    }

    public boolean hasStoryTail() {
        return entityData.get(TAIL) && !tailCut;
    }

    public boolean hidden() {
        return entityData.get(HIDDEN);
    }

    public void setHidden(boolean hidden) {
        entityData.set(HIDDEN, hidden);
        setInvisible(hidden);
    }

    public @Nullable UUID owner() {
        return owner;
    }

    public String tag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag == null ? "" : tag;
    }

    public void setBossThreshold(float threshold) {
        this.bossThreshold = threshold;
        this.defeated = false;
    }

    public boolean isBoss() {
        return bossThreshold >= 0;
    }

    public boolean defeated() {
        return defeated;
    }

    public void setLifetime(int ticks) {
        this.lifetime = ticks;
    }

    public void setFlee(Vec3 center, double radius, boolean fly) {
        this.fleeCenter = center;
        this.fleeRadius = radius;
        this.fleeFly = fly;
        setFighterFlying(fly);
    }

    private void fleeTick(ServerLevel level) {
        ServerPlayer player = owner == null ? null : level.getServer().getPlayerList().getPlayer(owner);
        if (player == null || player.level() != level || fleeCenter == null) {
            return;
        }
        Vec3 here = position();
        double distance = player.distanceTo(this);
        if (distance > 9.0 && moveTarget == null) {
            return;
        }
        Vec3 away = here.subtract(player.position()).multiply(1, 0, 1);
        if (away.lengthSqr() < 1.0e-3) {
            away = new Vec3(getRandom().nextDouble() - 0.5, 0, getRandom().nextDouble() - 0.5);
        }
        Vec3 want = here.add(away.normalize().scale(6.0));
        Vec3 rel = want.subtract(fleeCenter).multiply(1, 0, 1);
        if (rel.length() > fleeRadius) {
            double angle = Math.atan2(rel.z, rel.x) + (getRandom().nextBoolean() ? 1.6 : -1.6);
            want = fleeCenter.add(Math.cos(angle) * fleeRadius * 0.7, 0, Math.sin(angle) * fleeRadius * 0.7);
        }
        want = new Vec3(want.x, fleeFly ? fleeCenter.y + 1.5 : here.y, want.z);
        moveTo(want, fleeFly ? 0.45 : 0.42, fleeFly);
        if (moveTarget != null && moveTarget.distanceTo(here) < 0.8) {
            moveTarget = null;
        }
    }

    public void moveTo(Vec3 target, double speed, boolean fly) {
        this.moveTarget = target;
        this.moveSpeed = speed;
        setFighterFlying(fly);
    }

    public void stopMoving() {
        this.moveTarget = null;
        getNavigation().stop();
        setDeltaMovement(Vec3.ZERO);
    }

    public void lookAtEntity(int entityId) {
        this.lookTarget = entityId;
    }

    public void setStoryForm(@Nullable String form) {
        setFighterForm(form);
        FormDefinition def = form == null ? null : FormRegistry.get(form, false);
        double giant = FormDefinition.passive(def, com.dbtenkaichi.form.FormPassive.GIANT);
        var attr = getAttribute(Attributes.SCALE);
        if (attr != null) {
            attr.setBaseValue(giant > 0 ? giant : 1.0);
        }
    }

    public void playCinematic(String form, int ticks) {
        Services.NETWORK.sendToTracking(this, new Packets.Cinematic(getId(), form, ticks, true));
        fighterRuntime().transformingUntil = level().getGameTime() + ticks;
    }

    public void cutTail() {
        if (!hasStoryTail()) {
            return;
        }
        tailCut = true;
        entityData.set(TAIL, false);
        if (fighterForm() != null) {
            FormDefinition def = FormRegistry.get(fighterForm(), false);
            if (def != null && def.has(com.dbtenkaichi.form.FormPassive.TAIL_BOUND)) {
                setStoryForm(null);
            }
        }
        if (level() instanceof ServerLevel server) {
            com.dbtenkaichi.mission.MissionManager.onStoryEvent(server, this, "tail_cut");
        }
    }

    @Override
    public double moveSpeed(boolean enraged) {
        double base = 0.55 + stat(StatType.SPEED) * 0.006;
        return Math.min(ConfigManager.gameplay().maxFlightSpeed, base * (enraged ? 1.3 : 1.0));
    }

    @Override
    public void applyRage() {
        for (StatType type : new StatType[]{StatType.STRENGTH, StatType.SPEED, StatType.KI_OUTPUT}) {
            stats.add(type, Math.max(1, stats.get(type) / 6));
        }
        ki = maxKi();
    }

    @Override
    public boolean canEnrage() {
        return isBoss();
    }

    @Override
    public Technique signatureBeam() {
        StoryCharacters.Character c = character();
        Technique t = c == null ? null : c.signature();
        return t == null || t.type() != Technique.Type.BEAM ? Techniques.KAMEHAMEHA : t;
    }

    @Override
    public int blastColor() {
        StoryCharacters.Character c = character();
        return c == null ? 0xFFD24A : c.blastColor();
    }

    @Override
    public double outgoingDamageScale() {
        return role() == ROLE_ALLY ? 0.5 : 0.75;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (role() == ROLE_ACTOR || defeated || hidden()) {
            return false;
        }
        if (role() == ROLE_ALLY && source.getEntity() instanceof Player) {
            return false;
        }
        if (isBoss() && getHealth() - damage <= getMaxHealth() * bossThreshold) {
            boolean result = super.hurtServer(level, source, Math.max(0.0f, getHealth() - getMaxHealth() * bossThreshold));
            setHealth(Math.max(1.0f, getMaxHealth() * bossThreshold));
            defeated = true;
            setTarget(null);
            fighterRuntime().invulnerableUntil = level.getGameTime() + 100000L;
            com.dbtenkaichi.mission.MissionManager.onStoryEvent(level, this, "defeated");
            return result;
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (lifetime >= 0 && --lifetime <= 0) {
            discard();
            return;
        }
        if (fleeCenter != null && !defeated) {
            fleeTick(level);
            if (moveTarget != null) {
                if (isFighterFlying()) {
                    FighterMovement.flyToward(this, moveTarget, moveSpeed, 0.35);
                } else {
                    getNavigation().moveTo(moveTarget.x, moveTarget.y, moveTarget.z, moveSpeed * 2.8);
                }
            }
            return;
        }
        int role = role();
        if (role == ROLE_ACTOR || defeated) {
            if (lookTarget >= 0 && level.getEntity(lookTarget) instanceof LivingEntity look) {
                lookAtPoint(look.getEyePosition());
            }
            if (moveTarget != null) {
                Vec3 to = moveTarget.subtract(position());
                if (to.length() < 0.6) {
                    stopMoving();
                } else if (isFighterFlying()) {
                    FighterMovement.flyToward(this, moveTarget, moveSpeed, 0.3);
                } else {
                    getNavigation().moveTo(moveTarget.x, moveTarget.y, moveTarget.z, moveSpeed * 2.5);
                }
            } else if (isFighterFlying()) {
                FighterMovement.hover(this);
            }
            return;
        }
        LivingEntity target = getTarget();
        if (role == ROLE_ENEMY && (target == null || !target.isAlive()) && owner != null) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
            if (player != null && player.level() == level && player.distanceTo(this) < 96) {
                setTarget(player);
            }
        }
        if (role == ROLE_ALLY && (target == null || !target.isAlive())) {
            StoryNpc foe = null;
            double best = Double.MAX_VALUE;
            for (StoryNpc other : level.getEntitiesOfClass(StoryNpc.class, getBoundingBox().inflate(48.0), n -> n.hostile() && n.isAlive() && !n.defeated())) {
                double d = other.distanceToSqr(this);
                if (d < best) {
                    best = d;
                    foe = other;
                }
            }
            setTarget(foe);
            if (foe == null && owner != null) {
                ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
                if (player != null && player.distanceTo(this) > 6) {
                    FighterMovement.flyToward(this, player.position().add(2, 1, 2), moveSpeed(false), 0.25);
                }
            }
        }
        brain.tick(level);
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
    }
}
