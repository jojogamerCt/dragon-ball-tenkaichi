package com.dbtenkaichi.ki;

import com.dbtenkaichi.anim.AnimClip;
import com.dbtenkaichi.combat.CombatFxKind;
import com.dbtenkaichi.combat.CombatSystem;
import com.dbtenkaichi.entity.KiBeamEntity;
import com.dbtenkaichi.entity.KiBlastEntity;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.player.RuntimeState;
import com.dbtenkaichi.registry.ModSounds;
import com.dbtenkaichi.server.PlayerManager;
import com.dbtenkaichi.training.TrainingSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class KiSystem {
    public static final String DEFAULT_SKILL = Techniques.KI_BLAST.id();

    private KiSystem() {
    }

    public static void chargeStart(ServerPlayer player, PlayerData data, long now) {
        RuntimeState rt = data.runtime();
        if (rt.busy(now) || rt.guarding || rt.skillSlot >= 0 || rt.activeBeam >= 0 || rt.exhaustedUntil > now) {
            return;
        }
        if (rt.clashId >= 0) {
            BeamClashManager.pulse(player, now);
            return;
        }
        if (!rt.charging) {
            rt.charging = true;
            rt.chargeStart = now;
            rt.stateDirty = true;
            PlayerManager.applyAttributes(player, data);
            CombatSystem.playSound(player.level(), player, ModSounds.KI_CHARGE_START.get(), 0.9f, 1.0f);
            if (data.ki() >= rt.cachedMaxKi * 0.98) {
                CombatSystem.fx(player, CombatFxKind.AURA_BURST, player, player, player.position(), 0.6f);
            }
        }
    }

    public static void chargeEnd(ServerPlayer player, PlayerData data) {
        RuntimeState rt = data.runtime();
        if (rt.charging) {
            rt.charging = false;
            rt.stateDirty = true;
            PlayerManager.applyAttributes(player, data);
        }
    }

    public static void skillPress(ServerPlayer player, PlayerData data, int slot, long now) {
        RuntimeState rt = data.runtime();
        if (rt.activeBeam >= 0) {
            BeamClashManager.setHolding(player, true);
            return;
        }
        if (rt.busy(now) || rt.guarding || slot < 0 || slot >= PlayerData.SKILL_SLOTS) {
            return;
        }
        String id = data.skillInSlot(slot);
        if (id.isEmpty() || !data.hasSkill(id)) {
            player.sendSystemMessage(Component.translatable("message.dbtenkaichi.empty_slot"));
            return;
        }
        Technique technique = Techniques.get(id);
        if (technique == null) {
            return;
        }
        rt.charging = false;
        if (technique.type() == Technique.Type.BLAST) {
            fireBlast(player, data, technique, now);
            return;
        }
        if (rt.skillSlot < 0) {
            rt.skillSlot = slot;
            rt.skillStart = now;
            rt.stateDirty = true;
            CombatSystem.playSound(player.level(), player, ModSounds.BEAM_CHARGE.get(), 1.0f, 1.0f);
        }
    }

    public static void skillRelease(ServerPlayer player, PlayerData data, int slot, long now) {
        RuntimeState rt = data.runtime();
        if (rt.activeBeam >= 0) {
            BeamClashManager.setHolding(player, false);
            return;
        }
        if (rt.skillSlot < 0 || rt.skillSlot != slot) {
            return;
        }
        String id = data.skillInSlot(slot);
        rt.skillSlot = -1;
        rt.stateDirty = true;
        Technique technique = Techniques.get(id);
        if (technique == null || technique.type() != Technique.Type.BEAM || rt.busy(now)) {
            return;
        }
        int charged = (int) (now - rt.skillStart);
        if (charged < technique.minCharge()) {
            return;
        }
        double ratio = Math.min(1.0, (double) (charged - technique.minCharge()) / Math.max(1, technique.maxCharge() - technique.minCharge()));
        double cost = technique.costAt(ratio);
        if (!PlayerManager.spendKi(player, data, cost)) {
            double affordable = data.ki() / Math.max(1.0, cost);
            if (affordable < 0.35 || !PlayerManager.spendKi(player, data, technique.costAt(0.0))) {
                player.sendSystemMessage(Component.translatable("message.dbtenkaichi.no_ki"));
                return;
            }
            ratio = 0.0;
        }
        ServerLevel level = player.level();
        KiBeamEntity beam = KiBeamEntity.create(level, player, technique, ratio);
        level.addFreshEntity(beam);
        rt.activeBeam = beam.getId();
        rt.lastCombat = now;
        CombatSystem.anim(player, AnimClip.BEAM_FIRE, technique.duration() + 10, 0);
        CombatSystem.playSound(level, player, ModSounds.BEAM_FIRE.get(), 1.6f, 1.0f);
        float mastery = data.skillMastery(id);
        data.setSkillMastery(id, mastery + 0.01f);
        TrainingSystem.passiveGain(player, data, "technique_" + id, technique.trainedStat(), 2.0 + ratio * 2.0);
    }

    public static void fireBlast(ServerPlayer player, PlayerData data, Technique technique, long now) {
        RuntimeState rt = data.runtime();
        if (now < rt.nextKiBlast || rt.busy(now)) {
            return;
        }
        if (!PlayerManager.spendKi(player, data, technique.kiCost())) {
            player.sendSystemMessage(Component.translatable("message.dbtenkaichi.no_ki"));
            return;
        }
        rt.nextKiBlast = now + technique.cooldownTicks();
        rt.lastCombat = now;
        ServerLevel level = player.level();
        int color = data.appearance().auraColor;
        KiBlastEntity blast = KiBlastEntity.create(level, player, technique, color, rt.lockedTarget, 1.0);
        level.addFreshEntity(blast);
        rt.blastHand = !rt.blastHand;
        CombatSystem.anim(player, rt.blastHand ? AnimClip.KI_BLAST_LEFT : AnimClip.KI_BLAST_RIGHT, 6, 0);
        CombatSystem.playSound(level, player, ModSounds.KI_BLAST.get(), 0.8f, 0.9f + player.getRandom().nextFloat() * 0.3f);
        TrainingSystem.passiveGain(player, data, "ki_blast", technique.trainedStat(), 0.15);
    }
}
