package com.dbtenkaichi.server;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.combat.CombatSystem;
import com.dbtenkaichi.combat.Fighter;
import com.dbtenkaichi.combat.TenkaichiDamage;
import com.dbtenkaichi.config.BalanceConfig;
import com.dbtenkaichi.config.ConfigManager;
import com.dbtenkaichi.form.FormRegistry;
import com.dbtenkaichi.form.TransformationSystem;
import com.dbtenkaichi.ki.BeamClashManager;
import com.dbtenkaichi.network.Packets;
import com.dbtenkaichi.platform.Services;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.player.RuntimeState;
import com.dbtenkaichi.progression.ProgressionMath;
import com.dbtenkaichi.stats.StatType;
import com.dbtenkaichi.world.DestructionManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class ServerEvents {
    private ServerEvents() {
    }

    public static void onServerTick(MinecraftServer server) {
        PlayerManager.tickServer(server);
        BeamClashManager.tick(server);
        DestructionManager.tick(server);
        SmokeTest.tick(server);
    }

    public static void onServerStopping(MinecraftServer server) {
        BeamClashManager.stopAll();
        DestructionManager.restoreAll(server);
    }

    public static void onDataReloaded(MinecraftServer server) {
        ConfigManager.reloadCommon();
        Packets.Forms forms = new Packets.Forms(FormRegistry.serializeServer());
        Packets.ServerConfig config = new Packets.ServerConfig(ConfigManager.serializeCommon());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Services.NETWORK.sendToPlayer(player, config);
            Services.NETWORK.sendToPlayer(player, forms);
            PlayerData data = PlayerManager.data(player);
            if (data.isTransformed() && FormRegistry.get(data.form(), false) == null) {
                TransformationSystem.forceRevert(player, data, null);
            }
            PlayerManager.refreshCaches(player, data);
            PlayerManager.applyAttributes(player, data);
        }
    }

    public static float modifyIncomingDamage(LivingEntity target, ServerLevel level, DamageSource source, float amount) {
        if (amount <= 0 || TenkaichiDamage.applyingDirect() || TenkaichiDamage.isTenkaichi(source)) {
            return amount;
        }
        BalanceConfig cfg = ConfigManager.balance();
        float result = amount;
        if (source.getEntity() instanceof Player attacker && source.isDirect() && !(attacker == target)) {
            PlayerData data = PlayerManager.data(attacker);
            if (data.isCreated()) {
                Fighter fighter = Fighter.of(attacker);
                result *= (float) ((1.0 + data.stat(StatType.STRENGTH) * 0.04 * cfg.statMultiplier) * fighter.attackMultiplier(cfg));
            }
        }
        if (target instanceof Player player) {
            PlayerData data = PlayerManager.data(player);
            if (data.isCreated() && !source.is(DamageTypes.FELL_OUT_OF_WORLD) && !source.is(DamageTypes.GENERIC_KILL)) {
                Fighter fighter = Fighter.of(player);
                double defense = data.stat(StatType.DEFENSE) * cfg.defenseScale * cfg.statMultiplier * fighter.defenseMultiplier(cfg);
                result *= (float) (cfg.defenseReference / (cfg.defenseReference + defense));
                RuntimeState rt = data.runtime();
                if (rt.invulnerableUntil > level.getGameTime() && source.getEntity() != null) {
                    return 0.0f;
                }
            }
        }
        return result;
    }

    public static void afterDamage(LivingEntity target, ServerLevel level, DamageSource source, float amount) {
        long now = level.getGameTime();
        RuntimeState rt = CombatSystem.runtime(target);
        if (rt != null && source.getEntity() instanceof LivingEntity) {
            rt.lastCombat = now;
            if (rt.charging) {
                rt.charging = false;
                rt.stateDirty = true;
            }
            BeamClashManager.onAttacked(target, now);
        }
        if (target instanceof ServerPlayer player && source.getEntity() instanceof LivingEntity attacker && attacker != player) {
            PlayerData data = PlayerManager.data(player);
            if (data.isCreated()) {
                ZenkaiSystem.onDamaged(player, data, attacker, now);
                TransformationSystem.tryRageUnlock(player, data, attacker, now);
            }
        }
    }

    public static void onLivingDeath(LivingEntity entity, DamageSource source) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        long enemyBp = Fighter.of(entity).battlePower();
        rewardContributors(level, entity, enemyBp);
    }

    public static void onFighterDefeated(ServerLevel level, LivingEntity entity, long enemyBp) {
    }

    private static void rewardContributors(ServerLevel level, LivingEntity entity, long enemyBp) {
        BalanceConfig cfg = ConfigManager.balance();
        int id = entity.getId();
        float maxHealth = Math.max(1.0f, entity.getMaxHealth());
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            PlayerData data = PlayerManager.data(player);
            Double dealt = data.runtime().damageDealt.remove(id);
            if (dealt == null || !data.isCreated() || player == entity) {
                continue;
            }
            double share = Math.min(1.0, dealt / maxHealth);
            long playerBp = Fighter.of(player).battlePower();
            double reward = ProgressionMath.combatReward(playerBp, enemyBp, share, data.fatigue(), cfg) * data.race().combatTpMultiplier();
            if (entity instanceof Player) {
                reward *= 0.5;
            }
            PlayerManager.grantTp(player, data, reward);
            if (reward >= 1.0) {
                player.sendOverlayMessage(Component.translatable("message.dbtenkaichi.tp_gained", (long) Math.floor(reward)));
            }
        }
    }

    public static void onPlayerDeath(ServerPlayer player) {
        PlayerData data = PlayerManager.data(player);
        data.runtime().damageDealt.clear();
        if (data.runtime().flying) {
            player.setNoGravity(false);
        }
    }

    public static void onClone(ServerPlayer original, ServerPlayer clone, boolean alive) {
        PlayerData from = PlayerManager.data(original);
        PlayerData to = PlayerManager.data(clone);
        if (from != to) {
            to.copyFrom(from);
        }
    }

    public static void logStartup() {
        Tenkaichi.LOG.info("Dragon Ball: Tenkaichi server hooks ready on {}", Services.PLATFORM.loaderName());
    }
}
