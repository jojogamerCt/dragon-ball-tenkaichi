package com.dbtenkaichi.form;

import com.dbtenkaichi.anim.AnimClip;
import com.dbtenkaichi.combat.CombatFxKind;
import com.dbtenkaichi.combat.CombatSystem;
import com.dbtenkaichi.config.BalanceConfig;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.player.RuntimeState;
import com.dbtenkaichi.registry.ModSounds;
import com.dbtenkaichi.server.PlayerManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class KaiokenSystem {
    public static final String ABILITY = "dbtenkaichi:kaioken";
    public static final int FLAG_SHIFT = 14;
    public static final int FLAG_MASK = 7 << FLAG_SHIFT;
    public static final int SHOP_INDEX = 99;
    public static final int PRICE = 600;
    public static final long BATTLE_POWER = 4_000L;

    public record Level(int multiplier, double kiDrain, double healthDrain, double speed, float mastery) {
    }

    private static final Level[] LEVELS = {
            new Level(2, 2.0, 0.3, 1.10, 0.0f),
            new Level(3, 3.5, 0.6, 1.16, 0.10f),
            new Level(4, 5.0, 1.0, 1.22, 0.25f),
            new Level(10, 9.0, 2.0, 1.34, 0.50f),
            new Level(20, 16.0, 3.5, 1.50, 0.80f)
    };

    private KaiokenSystem() {
    }

    public static int maxLevel() {
        return LEVELS.length;
    }

    public static Level level(int level) {
        return LEVELS[Math.max(1, Math.min(LEVELS.length, level)) - 1];
    }

    public static double multiplier(int level) {
        return level <= 0 ? 1.0 : level(level).multiplier();
    }

    public static double speed(int level) {
        return level <= 0 ? 1.0 : level(level).speed();
    }

    public static int fromFlags(int flags) {
        return (flags & FLAG_MASK) >> FLAG_SHIFT;
    }

    public static boolean compatible(PlayerData data) {
        if (!data.isTransformed()) {
            return true;
        }
        FormDefinition form = FormRegistry.get(data.form(), false);
        return form != null && form.kaiokenCompatible;
    }

    public static void cycle(ServerPlayer player, PlayerData data, long now) {
        RuntimeState rt = data.runtime();
        if (!data.hasSkill(ABILITY)) {
            player.sendOverlayMessage(Component.translatable("message.dbtenkaichi.kaioken_unknown"));
            return;
        }
        if (rt.busy(now) || rt.activeBeam >= 0 || rt.clashId >= 0) {
            return;
        }
        if (!compatible(data)) {
            player.sendOverlayMessage(Component.translatable("message.dbtenkaichi.kaioken_incompatible"));
            return;
        }
        float mastery = data.mastery(ABILITY);
        int next = rt.kaioken + 1;
        if (next > LEVELS.length || LEVELS[next - 1].mastery() > mastery) {
            next = 0;
        }
        if (next > 0 && !PlayerManager.spendKi(player, data, 8.0 + 4.0 * next)) {
            player.sendOverlayMessage(Component.translatable("message.dbtenkaichi.no_ki"));
            return;
        }
        set(player, data, next, now);
    }

    public static void set(ServerPlayer player, PlayerData data, int level, long now) {
        RuntimeState rt = data.runtime();
        if (rt.kaioken == level) {
            return;
        }
        rt.kaioken = level;
        rt.stateDirty = true;
        rt.dataDirty = true;
        PlayerManager.refreshCaches(player, data);
        PlayerManager.applyAttributes(player, data);
        if (level > 0) {
            CombatSystem.fx(player, CombatFxKind.AURA_BURST, player, player, player.position(), 1.0f + level * 0.15f);
            CombatSystem.playSound(player.level(), player, ModSounds.AURA_BURST.get(), 1.0f, 0.9f + level * 0.08f);
            CombatSystem.anim(player, AnimClip.POWER_UP, 10, 0);
            player.sendOverlayMessage(Component.translatable("message.dbtenkaichi.kaioken_on", (int) multiplier(level)));
        } else {
            CombatSystem.playSound(player.level(), player, ModSounds.REVERT.get(), 0.9f, 1.1f);
            player.sendOverlayMessage(Component.translatable("message.dbtenkaichi.kaioken_off"));
        }
    }

    public static void off(ServerPlayer player, PlayerData data, long now, String reason) {
        if (data.runtime().kaioken <= 0) {
            return;
        }
        set(player, data, 0, now);
        if (reason != null) {
            player.sendSystemMessage(Component.translatable(reason));
        }
    }

    public static double tick(ServerPlayer player, PlayerData data, BalanceConfig cfg, long now) {
        RuntimeState rt = data.runtime();
        if (rt.kaioken <= 0) {
            return 0.0;
        }
        if (!compatible(data)) {
            off(player, data, now, "message.dbtenkaichi.kaioken_incompatible");
            return 0.0;
        }
        Level level = level(rt.kaioken);
        float mastery = data.mastery(ABILITY);
        if (player.getHealth() <= 3.0f) {
            off(player, data, now, "message.dbtenkaichi.kaioken_strain");
            return 0.0;
        }
        float drained = (float) (level.healthDrain() * (1.0 - 0.5 * mastery) / 20.0);
        player.setHealth(Math.max(2.0f, player.getHealth() - drained));
        if (mastery < 1.0f) {
            float gain = (float) (0.0012 / 20.0 * (now - rt.lastCombat < 100 ? 2.0 : 1.0) * cfg.trainingSpeedMultiplier);
            data.setMastery(ABILITY, mastery + gain);
            if (now % 100 == 0) {
                rt.dataDirty = true;
            }
        }
        return level.kiDrain() * (1.0 - 0.5 * mastery) / 20.0;
    }
}
