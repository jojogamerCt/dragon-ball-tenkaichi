package com.dbtenkaichi.server;

import com.dbtenkaichi.combat.CombatFxKind;
import com.dbtenkaichi.combat.CombatSystem;
import com.dbtenkaichi.combat.Fighter;
import com.dbtenkaichi.config.BalanceConfig;
import com.dbtenkaichi.config.ConfigManager;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.player.RuntimeState;
import com.dbtenkaichi.progression.ProgressionMath;
import com.dbtenkaichi.race.RacePassive;
import com.dbtenkaichi.registry.ModSounds;
import com.dbtenkaichi.stats.StatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public final class ZenkaiSystem {
    private static final StatType[] BOOSTED = {StatType.STRENGTH, StatType.DEFENSE, StatType.CONSTITUTION, StatType.KI_OUTPUT, StatType.KI_CAPACITY};

    private ZenkaiSystem() {
    }

    public static void onDamaged(ServerPlayer player, PlayerData data, LivingEntity attacker, long now) {
        if (!data.race().has(RacePassive.ZENKAI)) {
            return;
        }
        if (player.getHealth() / Math.max(1.0f, player.getMaxHealth()) > 0.15f) {
            return;
        }
        long enemyBp = Fighter.of(attacker).battlePower();
        long ownBp = Fighter.of(player).battlePower();
        if (enemyBp <= ownBp) {
            return;
        }
        RuntimeState rt = data.runtime();
        if (rt.zenkaiPendingBp <= 0) {
            rt.zenkaiPendingSince = now;
        }
        rt.zenkaiPendingBp = Math.max(rt.zenkaiPendingBp, enemyBp);
    }

    public static void tick(ServerPlayer player, PlayerData data, long now) {
        RuntimeState rt = data.runtime();
        if (rt.zenkaiPendingBp <= 0) {
            return;
        }
        boolean recovered = player.getHealth() / Math.max(1.0f, player.getMaxHealth()) > 0.6f;
        boolean survived = now - rt.zenkaiPendingSince > 600 && now - rt.lastCombat > 200;
        if (!recovered && !survived) {
            return;
        }
        double pending = rt.zenkaiPendingBp;
        rt.zenkaiPendingBp = 0;
        if (now < data.zenkaiReadyAt()) {
            return;
        }
        BalanceConfig cfg = ConfigManager.balance();
        long ownBp = Fighter.of(player).battlePower();
        int cap = ProgressionMath.statCap(data.stat(StatType.POTENTIAL), cfg);
        int total = 0;
        for (StatType type : BOOSTED) {
            int gain = ProgressionMath.zenkaiLevels(data.stat(type), ownBp, (long) pending, cfg);
            int allowed = Math.max(0, Math.min(gain, cap - data.stat(type)));
            data.stats().add(type, allowed);
            total += allowed;
        }
        data.setZenkaiReadyAt(now + cfg.zenkaiCooldownTicks);
        if (total > 0) {
            data.incrementZenkai();
            PlayerManager.refreshCaches(player, data);
            PlayerManager.applyAttributes(player, data);
            rt.dataDirty = true;
            player.sendSystemMessage(Component.translatable("message.dbtenkaichi.zenkai", total));
            CombatSystem.fx(player, CombatFxKind.ZENKAI, player, player, player.position(), 1.0f);
            CombatSystem.playSound(player.level(), player, ModSounds.AURA_BURST.get(), 1.0f, 1.3f);
        }
    }
}
