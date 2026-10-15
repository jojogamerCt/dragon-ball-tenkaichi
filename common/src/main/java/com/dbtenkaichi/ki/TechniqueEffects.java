package com.dbtenkaichi.ki;

import com.dbtenkaichi.anim.AnimClip;
import com.dbtenkaichi.combat.CombatFxKind;
import com.dbtenkaichi.combat.CombatSystem;
import com.dbtenkaichi.combat.DamageMath;
import com.dbtenkaichi.combat.Fighter;
import com.dbtenkaichi.combat.TenkaichiDamage;
import com.dbtenkaichi.combat.TenkaichiFighter;
import com.dbtenkaichi.config.BalanceConfig;
import com.dbtenkaichi.config.ConfigManager;
import com.dbtenkaichi.entity.KiBlastEntity;
import com.dbtenkaichi.entity.PowerBallEntity;
import com.dbtenkaichi.entity.StoryNpc;
import com.dbtenkaichi.form.FormEffects;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.player.RuntimeState;
import com.dbtenkaichi.race.KiSense;
import com.dbtenkaichi.race.TailSystem;
import com.dbtenkaichi.registry.ModEntities;
import com.dbtenkaichi.registry.ModItems;
import com.dbtenkaichi.registry.ModSounds;
import com.dbtenkaichi.server.PlayerManager;
import com.dbtenkaichi.stats.StatType;
import com.dbtenkaichi.world.DestructionManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class TechniqueEffects {
    private TechniqueEffects() {
    }

    public static boolean onCooldown(PlayerData data, Technique technique, long now) {
        Long ready = data.runtime().cooldowns.get(technique.id());
        return ready != null && ready > now;
    }

    public static void startCooldown(PlayerData data, Technique technique, long now) {
        data.runtime().cooldowns.put(technique.id(), now + technique.cooldownTicks());
    }

    public static @Nullable LivingEntity lookTarget(ServerPlayer player, PlayerData data, double range) {
        ServerLevel level = player.level();
        int locked = data.runtime().lockedTarget;
        if (locked >= 0 && level.getEntity(locked) instanceof LivingEntity target && target.isAlive() && target.distanceTo(player) <= range) {
            return target;
        }
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0f);
        Vec3 end = eye.add(look.scale(range));
        HitResult block = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (block.getType() != HitResult.Type.MISS) {
            end = block.getLocation();
        }
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, new AABB(eye, end).inflate(1.5), e -> e != player && e.isAlive() && !e.isSpectator())) {
            var hit = candidate.getBoundingBox().inflate(0.6).clip(eye, end);
            if (hit.isPresent()) {
                double d = eye.distanceToSqr(hit.get());
                if (d < bestDist) {
                    bestDist = d;
                    best = candidate;
                }
            }
        }
        return best;
    }

    private static Vec3 lookPoint(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0f).scale(range));
        HitResult hit = player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();
    }

    private static boolean boss(LivingEntity entity) {
        return entity instanceof StoryNpc npc && npc.isBoss();
    }

    public static void cutTail(LivingEntity target, long now) {
        if (target instanceof ServerPlayer player) {
            PlayerData data = PlayerManager.data(player);
            if (data.isCreated()) {
                TailSystem.cutTail(player, data, now);
            }
        } else if (target instanceof StoryNpc npc) {
            npc.cutTail();
        }
    }

    public static void stun(LivingEntity target, int ticks, boolean blind) {
        long now = target.level().getGameTime();
        RuntimeState rt = CombatSystem.runtime(target);
        if (rt != null) {
            rt.stunnedUntil = Math.max(rt.stunnedUntil, now + ticks);
            rt.charging = false;
            rt.stateDirty = true;
        }
        if (blind && target instanceof Player) {
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, ticks, 0));
        }
        if (target instanceof Mob && !(target instanceof TenkaichiFighter)) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ticks, 6));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, ticks, 2));
            if (blind) {
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, ticks, 0));
            }
        }
    }

    private static double damage(LivingEntity owner, LivingEntity target, double base, StatType stat) {
        BalanceConfig cfg = ConfigManager.balance();
        Fighter attacker = Fighter.of(owner);
        Fighter defender = Fighter.of(target);
        double raw = DamageMath.ki(base, attacker.stat(stat), 1.0, attacker.kiMultiplier(cfg), cfg);
        return DamageMath.finalDamage(raw, attacker.battlePower(), defender.battlePower(), defender.stat(StatType.DEFENSE), defender.defenseMultiplier(cfg),
                DamageMath.GuardState.NONE, defender.fatigue(), cfg);
    }

    private static void hurt(ServerLevel level, LivingEntity owner, LivingEntity target, double amount) {
        TenkaichiDamage.hurt(target, level, TenkaichiDamage.source(level, TenkaichiDamage.KI_BLAST, owner, owner), (float) amount);
        RuntimeState ownerRt = CombatSystem.runtime(owner);
        if (ownerRt != null) {
            ownerRt.lastCombat = level.getGameTime();
            ownerRt.damageDealt.merge(target.getId(), amount, Double::sum);
        }
        if (target instanceof TenkaichiFighter npc) {
            npc.onFighterHit(owner, amount);
        }
    }

    public static void activate(ServerPlayer player, PlayerData data, Technique technique, long now) {
        if (onCooldown(data, technique, now)) {
            player.sendOverlayMessage(Component.translatable("message.dbtenkaichi.technique_cooldown", Component.translatable(technique.translationKey())));
            return;
        }
        if (!PlayerManager.spendKi(player, data, technique.kiCost())) {
            player.sendSystemMessage(Component.translatable("message.dbtenkaichi.no_ki"));
            return;
        }
        boolean used = switch (technique.effect()) {
            case "solar_flare" -> solarFlare(player);
            case "teleport" -> teleport(player, data);
            case "power_ball" -> powerBall(player);
            case "eruption" -> eruption(player, data, technique);
            case "absorb" -> absorb(player, data);
            case "candy" -> candy(player, data);
            case "drain" -> drain(player, data);
            case "barrier" -> barrier(player, data, now);
            case "heal" -> heal(player, data);
            case "paralyze" -> paralyze(player, data);
            case "petrify" -> petrify(player, data, now);
            case "summon" -> summon(player, data);
            case "rush" -> rush(player, data, technique);
            case "hakai" -> hakai(player, data, technique);
            default -> false;
        };
        if (!used) {
            data.setKi(Math.min(data.runtime().cachedMaxKi, data.ki() + technique.kiCost() * 0.8));
            data.runtime().vitalsDirty = true;
            player.sendOverlayMessage(Component.translatable("message.dbtenkaichi.technique_no_target"));
            return;
        }
        startCooldown(data, technique, now);
        data.runtime().lastCombat = Math.max(data.runtime().lastCombat, now - 60);
        float mastery = data.skillMastery(technique.id());
        data.setSkillMastery(technique.id(), mastery + 0.01f);
        com.dbtenkaichi.mission.MissionManager.onTechnique(player, data, technique.id());
    }

    private static boolean solarFlare(ServerPlayer player) {
        ServerLevel level = player.level();
        CombatSystem.anim(player, AnimClip.SOLAR_FLARE, AnimClip.SOLAR_FLARE.defaultDuration, 0);
        CombatSystem.fx(player, CombatFxKind.AURA_BURST, player, player, player.getEyePosition(), 2.5f);
        CombatSystem.playSound(level, player, ModSounds.AURA_BURST.get(), 1.4f, 1.8f);
        Vec3 look = player.getViewVector(1.0f);
        for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(16.0), e -> e != player && e.isAlive())) {
            Vec3 to = other.getEyePosition().subtract(player.getEyePosition());
            if (to.length() < 16.0 && to.normalize().dot(look) > 0.2 && CombatSystem.hasLineOfSight(player, other) && !boss(other)) {
                stun(other, 60, true);
            } else if (to.length() < 16.0 && to.normalize().dot(look) > 0.2 && boss(other)) {
                stun(other, 25, false);
            }
        }
        return true;
    }

    private static boolean teleport(ServerPlayer player, PlayerData data) {
        ServerLevel level = player.level();
        LivingEntity target = lookTarget(player, data, 96.0);
        if (target == null && data.runtime().sensing) {
            double best = Double.MAX_VALUE;
            for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(KiSense.range(data, false)),
                    e -> e != player && e.isAlive() && (e instanceof Player || e instanceof TenkaichiFighter))) {
                if (!KiSense.canSense(player, candidate, false)) {
                    continue;
                }
                double d = candidate.distanceToSqr(player);
                if (d < best) {
                    best = d;
                    target = candidate;
                }
            }
        }
        if (target == null) {
            return false;
        }
        Vec3 behind = target.position().subtract(target.getViewVector(1.0f).multiply(1, 0, 1).normalize().scale(1.8));
        CombatSystem.fx(player, CombatFxKind.VANISH, player, player, player.position().add(0, 1, 0), 1.0f);
        player.teleportTo(behind.x, target.getY(), behind.z);
        player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
        CombatSystem.fx(player, CombatFxKind.VANISH, player, player, player.position().add(0, 1, 0), 1.0f);
        CombatSystem.playSound(level, player, ModSounds.VANISH.get(), 1.0f, 1.1f);
        return true;
    }

    private static boolean powerBall(ServerPlayer player) {
        ServerLevel level = player.level();
        CombatSystem.anim(player, AnimClip.DISC_THROW, AnimClip.DISC_THROW.defaultDuration, 0);
        level.addFreshEntity(PowerBallEntity.create(level, player));
        CombatSystem.playSound(level, player, ModSounds.KI_BLAST.get(), 1.2f, 0.6f);
        return true;
    }

    private static boolean eruption(ServerPlayer player, PlayerData data, Technique technique) {
        LivingEntity target = lookTarget(player, data, 36.0);
        Vec3 point = target != null ? target.position() : lookPoint(player, 36.0);
        CombatSystem.anim(player, AnimClip.DEATH_BEAM, AnimClip.DEATH_BEAM.defaultDuration, 0);
        DestructionManager.explode(player.level(), point, 3.5, player, technique.baseDamage(), 1.0);
        return true;
    }

    private static boolean absorb(ServerPlayer player, PlayerData data) {
        LivingEntity target = lookTarget(player, data, 4.5);
        if (target == null || boss(target) || !CombatSystem.canHarm(player, target)) {
            return false;
        }
        float ratio = target.getHealth() / Math.max(1.0f, target.getMaxHealth());
        boolean fighter = target instanceof Player || target instanceof TenkaichiFighter;
        if (ratio > (fighter ? 0.3f : 0.5f)) {
            player.sendOverlayMessage(Component.translatable("message.dbtenkaichi.absorb_too_strong"));
            return false;
        }
        ServerLevel level = player.level();
        long now = level.getGameTime();
        long targetBp = Fighter.of(target).battlePower();
        long selfBp = Fighter.of(player).battlePower();
        data.runtime().absorbingUntil = now + 24;
        data.runtime().stateDirty = true;
        CombatSystem.anim(player, AnimClip.GRAB, AnimClip.GRAB.defaultDuration, 0);
        CombatSystem.fx(player, CombatFxKind.AURA_BURST, player, target, target.position().add(0, 1, 0), 1.6f);
        CombatSystem.playSound(level, player, ModSounds.AURA_BURST.get(), 1.0f, 0.6f);
        hurt(level, player, target, target.getHealth() + 1000.0f);
        player.heal(player.getMaxHealth() * 0.4f);
        data.setKi(Math.min(data.runtime().cachedMaxKi, data.ki() + data.runtime().cachedMaxKi * 0.3));
        if (fighter && targetBp >= selfBp / 2) {
            data.addAbsorption();
            data.setAbsorbedStacks(data.absorbedStacks() + 1);
            player.sendSystemMessage(Component.translatable("message.dbtenkaichi.absorbed_power", data.absorptions()));
        } else {
            player.sendOverlayMessage(Component.translatable("message.dbtenkaichi.absorbed"));
        }
        data.runtime().dataDirty = true;
        data.runtime().vitalsDirty = true;
        PlayerManager.refreshCaches(player, data);
        return true;
    }

    private static boolean candy(ServerPlayer player, PlayerData data) {
        LivingEntity target = lookTarget(player, data, 24.0);
        if (target == null || boss(target) || !CombatSystem.canHarm(player, target)) {
            return false;
        }
        ServerLevel level = player.level();
        CombatSystem.anim(player, AnimClip.SOLAR_FLARE, AnimClip.SOLAR_FLARE.defaultDuration, 0);
        CombatSystem.fx(player, CombatFxKind.KI_IMPACT, player, target, target.position().add(0, 1, 0), 1.2f);
        CombatSystem.playSound(level, player, ModSounds.KI_IMPACT.get(), 1.0f, 1.8f);
        boolean weak = target.getHealth() <= target.getMaxHealth() * 0.5f;
        if (target instanceof Player || target instanceof TenkaichiFighter || !weak) {
            stun(target, 50, false);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2));
            return true;
        }
        int count = 1 + player.getRandom().nextInt(3);
        ItemEntity drop = new ItemEntity(level, target.getX(), target.getY() + 0.5, target.getZ(), new ItemStack(ModItems.CANDY.get(), count));
        level.addFreshEntity(drop);
        target.discard();
        return true;
    }

    private static boolean drain(ServerPlayer player, PlayerData data) {
        LivingEntity target = lookTarget(player, data, 4.0);
        if (target == null || !CombatSystem.canHarm(player, target)) {
            return false;
        }
        ServerLevel level = player.level();
        double steal = data.runtime().cachedMaxKi * 0.3;
        FormEffects.drainKi(target, steal);
        data.setKi(Math.min(data.runtime().cachedMaxKi, data.ki() + steal));
        player.heal(player.getMaxHealth() * 0.1f);
        data.runtime().absorbingUntil = level.getGameTime() + 20;
        data.runtime().stateDirty = true;
        data.runtime().vitalsDirty = true;
        CombatSystem.anim(player, AnimClip.GRAB, AnimClip.GRAB.defaultDuration, 0);
        CombatSystem.fx(player, CombatFxKind.AURA_BURST, target, player, player.position().add(0, 1, 0), 1.0f);
        stun(target, 20, false);
        return true;
    }

    private static boolean barrier(ServerPlayer player, PlayerData data, long now) {
        RuntimeState rt = data.runtime();
        rt.barrierUntil = now + 80;
        rt.stateDirty = true;
        ServerLevel level = player.level();
        CombatSystem.anim(player, AnimClip.BARRIER_CAST, AnimClip.BARRIER_CAST.defaultDuration, 0);
        CombatSystem.fx(player, CombatFxKind.SHOCKWAVE, player, player, player.position().add(0, 1, 0), 1.6f);
        CombatSystem.playSound(level, player, ModSounds.AURA_BURST.get(), 1.0f, 1.5f);
        for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(3.5), e -> e != player && e.isAlive())) {
            Vec3 push = other.position().subtract(player.position()).multiply(1, 0, 1);
            if (push.lengthSqr() > 1.0e-4) {
                CombatSystem.knockback(other, push.normalize().scale(1.2).add(0, 0.3, 0));
            }
        }
        for (KiBlastEntity blast : level.getEntitiesOfClass(KiBlastEntity.class, player.getBoundingBox().inflate(4.0))) {
            if (blast.owner() != player) {
                blast.discard();
            }
        }
        return true;
    }

    private static boolean heal(ServerPlayer player, PlayerData data) {
        LivingEntity target = lookTarget(player, data, 6.0);
        if (target == null || CombatSystem.canHarm(player, target) && !(target instanceof Player)) {
            target = player;
        }
        target.heal(target.getMaxHealth() * 0.35f);
        RuntimeState rt = CombatSystem.runtime(target);
        if (rt != null) {
            rt.stunnedUntil = 0;
            rt.petrifiedUntil = 0;
            rt.stateDirty = true;
        }
        CombatSystem.anim(player, AnimClip.GRAB, AnimClip.GRAB.defaultDuration, 0);
        CombatSystem.fx(player, CombatFxKind.LEVEL_UP, player, target, target.position(), 1.2f);
        CombatSystem.playSound(player.level(), player, ModSounds.TRAINING_SUCCESS.get(), 0.8f, 1.4f);
        return true;
    }

    private static boolean paralyze(ServerPlayer player, PlayerData data) {
        LivingEntity target = lookTarget(player, data, 24.0);
        if (target == null || !CombatSystem.canHarm(player, target)) {
            return false;
        }
        CombatSystem.anim(player, AnimClip.GRAB, AnimClip.GRAB.defaultDuration, 0);
        stun(target, boss(target) ? 25 : 60, false);
        CombatSystem.fx(player, CombatFxKind.GUARD, player, target, target.position().add(0, 1, 0), 1.0f);
        return true;
    }

    private static boolean petrify(ServerPlayer player, PlayerData data, long now) {
        LivingEntity target = lookTarget(player, data, 12.0);
        if (target == null || !CombatSystem.canHarm(player, target)) {
            return false;
        }
        int ticks = boss(target) ? 30 : 100;
        RuntimeState rt = CombatSystem.runtime(target);
        if (rt != null) {
            rt.petrifiedUntil = now + ticks;
        }
        stun(target, ticks, false);
        target.setDeltaMovement(Vec3.ZERO);
        CombatSystem.anim(player, AnimClip.SOLAR_FLARE, AnimClip.SOLAR_FLARE.defaultDuration, 0);
        CombatSystem.fx(player, CombatFxKind.GUARD_BREAK, player, target, target.position().add(0, 1, 0), 1.2f);
        CombatSystem.playSound(player.level(), player, ModSounds.GUARD_BREAK.get(), 1.0f, 0.6f);
        return true;
    }

    private static boolean summon(ServerPlayer player, PlayerData data) {
        ServerLevel level = player.level();
        LivingEntity target = lookTarget(player, data, 32.0);
        CombatSystem.anim(player, AnimClip.GRAB, AnimClip.GRAB.defaultDuration, 0);
        for (int i = 0; i < 3; i++) {
            double a = Math.PI * 2 * i / 3;
            Vec3 at = player.position().add(Math.cos(a) * 2.0, 0.2, Math.sin(a) * 2.0);
            StoryNpc junior = StoryNpc.spawn(level, com.dbtenkaichi.story.StoryCharacters.CELL_JR, at, StoryNpc.ROLE_ALLY, player, 0.45);
            if (junior != null) {
                junior.setLifetime(600);
                if (target != null && target != player) {
                    junior.setTarget(target);
                }
            }
        }
        CombatSystem.fx(player, CombatFxKind.AURA_BURST, player, player, player.position(), 1.2f);
        return true;
    }

    private static boolean rush(ServerPlayer player, PlayerData data, Technique technique) {
        LivingEntity target = lookTarget(player, data, 14.0);
        if (target == null || !CombatSystem.canHarm(player, target)) {
            return false;
        }
        ServerLevel level = player.level();
        Vec3 front = target.position().add(player.position().subtract(target.position()).multiply(1, 0, 1).normalize().scale(1.4));
        player.teleportTo(front.x, target.getY(), front.z);
        CombatSystem.anim(player, AnimClip.RUSH, AnimClip.RUSH.defaultDuration, 0);
        double total = 0;
        for (int i = 0; i < 6; i++) {
            total += damage(player, target, technique.baseDamage(), StatType.MARTIAL_ARTS) * 0.35;
            CombatSystem.fx(player, i % 2 == 0 ? CombatFxKind.HIT_LIGHT : CombatFxKind.HIT_HEAVY, player, target,
                    target.position().add((player.getRandom().nextDouble() - 0.5) * 0.6, 1.0 + player.getRandom().nextDouble() * 0.6, (player.getRandom().nextDouble() - 0.5) * 0.6), 1.0f);
        }
        hurt(level, player, target, total);
        stun(target, 20, false);
        CombatSystem.playSound(level, player, ModSounds.PUNCH_HEAVY.get(), 1.2f, 1.1f);
        return true;
    }

    private static boolean hakai(ServerPlayer player, PlayerData data, Technique technique) {
        LivingEntity target = lookTarget(player, data, 16.0);
        if (target == null || !CombatSystem.canHarm(player, target)) {
            return false;
        }
        ServerLevel level = player.level();
        CombatSystem.anim(player, AnimClip.BIG_BANG, AnimClip.BIG_BANG.defaultDuration, 0);
        CombatSystem.fx(player, CombatFxKind.AURA_BURST, player, target, target.position().add(0, 1, 0), 2.0f);
        CombatSystem.playSound(level, player, ModSounds.AURA_BURST.get(), 1.4f, 0.5f);
        if (!boss(target) && target.getHealth() <= target.getMaxHealth() * 0.2f) {
            hurt(level, player, target, target.getHealth() + 1000.0f);
        } else {
            hurt(level, player, target, damage(player, target, technique.baseDamage(), StatType.KI_OUTPUT));
            FormEffects.drainKi(target, 60.0);
        }
        return true;
    }

    public static void fireBarrage(ServerPlayer player, PlayerData data, Technique technique, long now) {
        if (onCooldown(data, technique, now)) {
            player.sendOverlayMessage(Component.translatable("message.dbtenkaichi.technique_cooldown", Component.translatable(technique.translationKey())));
            return;
        }
        if (!PlayerManager.spendKi(player, data, technique.kiCost())) {
            player.sendSystemMessage(Component.translatable("message.dbtenkaichi.no_ki"));
            return;
        }
        startCooldown(data, technique, now);
        ServerLevel level = player.level();
        int count;
        try {
            count = Math.max(1, Integer.parseInt(technique.effect()));
        } catch (NumberFormatException e) {
            count = 8;
        }
        LivingEntity target = lookTarget(player, data, 48.0);
        int homing = target == null ? -1 : target.getId();
        Vec3 center = target != null ? target.getBoundingBox().getCenter() : lookPoint(player, 20.0);
        String shape = technique.shape();
        for (int i = 0; i < count; i++) {
            KiBlastEntity blast = KiBlastEntity.create(level, player, technique, technique.glowColor(), homing, 1.0);
            double a = Math.PI * 2 * i / count;
            switch (shape) {
                case "hellzone" -> {
                    Vec3 at = center.add(Math.cos(a) * 6.0, (i % 3 - 1) * 1.6, Math.sin(a) * 6.0);
                    blast.setPos(at);
                    blast.setDeltaMovement(new Vec3(-Math.sin(a), 0, Math.cos(a)).scale(0.2));
                    blast.delayed(20 + i * 2);
                }
                case "rain" -> {
                    Vec3 at = center.add(Math.cos(a) * 3.0 * player.getRandom().nextDouble(), 14.0 + player.getRandom().nextDouble() * 4.0, Math.sin(a) * 3.0 * player.getRandom().nextDouble());
                    blast.setPos(at);
                    blast.setDeltaMovement(new Vec3(0, -technique.speed(), 0));
                    blast.delayed(i * 3);
                }
                default -> {
                    Vec3 look = player.getViewVector(1.0f);
                    Vec3 spread = look.add((player.getRandom().nextDouble() - 0.5) * 0.7, (player.getRandom().nextDouble() - 0.5) * 0.5, (player.getRandom().nextDouble() - 0.5) * 0.7);
                    blast.setDeltaMovement(spread.normalize().scale(technique.speed()));
                    blast.delayed(i);
                }
            }
            level.addFreshEntity(blast);
        }
        CombatSystem.anim(player, "scatter".equals(shape) ? AnimClip.KI_BLAST_RIGHT : AnimClip.RAISE_THROW, "scatter".equals(shape) ? 20 : AnimClip.RAISE_THROW.defaultDuration, 0);
        CombatSystem.playSound(level, player, ModSounds.KI_BLAST.get(), 1.0f, 0.8f);
        data.runtime().lastCombat = now;
        com.dbtenkaichi.mission.MissionManager.onTechnique(player, data, technique.id());
    }

    public static void fireSphere(ServerPlayer player, PlayerData data, Technique technique, double ratio, long now) {
        ServerLevel level = player.level();
        LivingEntity target = lookTarget(player, data, 64.0);
        KiBlastEntity ball = KiBlastEntity.sphere(level, player, technique, ratio, target == null ? -1 : target.getId());
        Vec3 aim = target != null ? target.getBoundingBox().getCenter().subtract(ball.position()) : player.getViewVector(1.0f);
        ball.setDeltaMovement(aim.normalize().scale(technique.speed()));
        level.addFreshEntity(ball);
        startCooldown(data, technique, now);
        CombatSystem.anim(player, AnimClip.RAISE_THROW, AnimClip.RAISE_THROW.defaultDuration, 0);
        CombatSystem.playSound(level, player, ModSounds.BEAM_FIRE.get(), 1.8f, 0.6f);
    }
}
