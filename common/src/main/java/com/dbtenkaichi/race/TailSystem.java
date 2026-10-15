package com.dbtenkaichi.race;

import com.dbtenkaichi.combat.CombatFxKind;
import com.dbtenkaichi.combat.CombatSystem;
import com.dbtenkaichi.entity.PowerBallEntity;
import com.dbtenkaichi.form.FormDefinition;
import com.dbtenkaichi.form.FormPassive;
import com.dbtenkaichi.form.FormRegistry;
import com.dbtenkaichi.form.TransformationSystem;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.player.RuntimeState;
import com.dbtenkaichi.registry.ModSounds;
import com.dbtenkaichi.server.PlayerManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class TailSystem {
    public static final String GREAT_APE = "dbtenkaichi:great_ape";
    public static final long REGROW_TICKS = 72000L;

    private TailSystem() {
    }

    public static boolean fullMoonNight(ServerLevel level) {
        if (level.dimension() != Level.OVERWORLD) {
            return false;
        }
        long time = level.getOverworldClockTime();
        long day = Math.floorMod(time, 24000L);
        long phase = Math.floorMod(time / 24000L, 8L);
        return phase == 0 && day >= 13000L && day <= 23000L;
    }

    public static boolean moonInView(ServerLevel level, LivingEntity entity, boolean requireGaze) {
        BlockPos head = BlockPos.containing(entity.getEyePosition());
        if (fullMoonNight(level) && level.canSeeSkyFromBelowWater(head) && (!requireGaze || entity.getXRot() < -20.0f)) {
            return true;
        }
        Vec3 eye = entity.getEyePosition();
        Vec3 look = entity.getViewVector(1.0f);
        for (PowerBallEntity ball : level.getEntitiesOfClass(PowerBallEntity.class, new AABB(eye, eye).inflate(PowerBallEntity.RANGE))) {
            Vec3 to = ball.position().subtract(eye);
            if (!requireGaze || to.normalize().dot(look) > 0.8) {
                return true;
            }
        }
        return false;
    }

    public static void tick(ServerPlayer player, PlayerData data, long now) {
        if (data.tailState() == PlayerData.TAIL_LOST && data.tailRegrowAt() > 0 && now >= data.tailRegrowAt()) {
            data.setTailState(PlayerData.TAIL_FREE);
            data.setTailRegrowAt(0L);
            data.runtime().appearanceDirty = true;
            data.runtime().dataDirty = true;
            player.sendSystemMessage(Component.translatable("message.dbtenkaichi.tail_regrown"));
        }
        if (now % 20 != 0) {
            return;
        }
        FormDefinition form = data.isTransformed() ? FormRegistry.get(data.form(), false) : null;
        ServerLevel level = player.level();
        if (form != null) {
            if (form.has(FormPassive.TAIL_BOUND) && !data.hasTail()) {
                TransformationSystem.forceRevert(player, data, Component.translatable("message.dbtenkaichi.tail_lost_revert"));
                return;
            }
            if (form.has(FormPassive.MOON_BOUND) && !moonInView(level, player, false)) {
                TransformationSystem.forceRevert(player, data, Component.translatable("message.dbtenkaichi.moon_gone"));
                return;
            }
            return;
        }
        RuntimeState rt = data.runtime();
        if (!data.tailExposed() || rt.busy(now) || rt.inScene || player.isSpectator()) {
            return;
        }
        FormDefinition ape = FormRegistry.get(GREAT_APE, false);
        if (ape == null || !ape.allows(data.race()) || !moonInView(level, player, true)) {
            return;
        }
        data.unlockForm(ape.id);
        player.sendSystemMessage(Component.translatable("message.dbtenkaichi.great_ape_moon"));
        TransformationSystem.begin(player, data, ape, TransformationSystem.CINEMATIC_AUTO, !data.hasSeenCinematic(ape.id), now);
    }

    public static void toggleWrap(ServerPlayer player, PlayerData data) {
        if (!data.hasTail()) {
            player.sendSystemMessage(Component.translatable("message.dbtenkaichi.no_tail"));
            return;
        }
        data.setTailState(data.tailState() == PlayerData.TAIL_FREE ? PlayerData.TAIL_WRAPPED : PlayerData.TAIL_FREE);
        data.runtime().appearanceDirty = true;
        data.runtime().dataDirty = true;
        player.sendSystemMessage(Component.translatable(data.tailState() == PlayerData.TAIL_WRAPPED ? "message.dbtenkaichi.tail_wrapped" : "message.dbtenkaichi.tail_free"));
    }

    public static boolean cutTail(ServerPlayer player, PlayerData data, long now) {
        if (!data.hasTail()) {
            return false;
        }
        data.setTailState(PlayerData.TAIL_LOST);
        data.setTailRegrowAt(now + REGROW_TICKS);
        data.runtime().appearanceDirty = true;
        data.runtime().dataDirty = true;
        CombatSystem.fx(player, CombatFxKind.HIT_HEAVY, player, player, player.position().add(0, 0.8, 0), 0.8f);
        CombatSystem.playSound(player.level(), player, ModSounds.PUNCH_HEAVY.get(), 1.0f, 1.4f);
        player.sendSystemMessage(Component.translatable("message.dbtenkaichi.tail_cut"));
        if (data.isTransformed()) {
            FormDefinition form = FormRegistry.get(data.form(), false);
            if (form != null && form.has(FormPassive.TAIL_BOUND)) {
                TransformationSystem.forceRevert(player, data, Component.translatable("message.dbtenkaichi.tail_lost_revert"));
            }
        }
        return true;
    }

    public static void onMeleeHit(LivingEntity attacker, ServerPlayer target, PlayerData data, long now) {
        if (!data.tailExposed() || data.isTransformed()) {
            return;
        }
        Vec3 facing = target.getViewVector(1.0f).multiply(1, 0, 1);
        Vec3 from = attacker.position().subtract(target.position()).multiply(1, 0, 1);
        if (facing.lengthSqr() < 1.0e-4 || from.lengthSqr() < 1.0e-4 || facing.normalize().dot(from.normalize()) > -0.5) {
            return;
        }
        float trained = Math.min(1.0f, data.mastery(GREAT_APE) + data.stat(com.dbtenkaichi.stats.StatType.SPIRIT) / 400.0f);
        if (target.getRandom().nextFloat() > 0.25f * (1.0f - trained)) {
            return;
        }
        RuntimeState rt = data.runtime();
        rt.tailGrabbedUntil = now + 30;
        rt.stunnedUntil = Math.max(rt.stunnedUntil, now + 30);
        rt.charging = false;
        rt.stateDirty = true;
        PlayerManager.spendStamina(data, Math.min(data.stamina(), 20.0));
        target.sendOverlayMessage(Component.translatable("message.dbtenkaichi.tail_grabbed"));
    }
}
