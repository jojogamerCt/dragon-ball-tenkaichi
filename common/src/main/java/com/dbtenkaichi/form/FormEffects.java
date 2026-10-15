package com.dbtenkaichi.form;

import com.dbtenkaichi.anim.AnimClip;
import com.dbtenkaichi.combat.CombatFxKind;
import com.dbtenkaichi.combat.CombatSystem;
import com.dbtenkaichi.combat.Fighter;
import com.dbtenkaichi.combat.TenkaichiFighter;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.player.RuntimeState;
import com.dbtenkaichi.registry.ModSounds;
import com.dbtenkaichi.server.PlayerManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class FormEffects {
    private FormEffects() {
    }

    private static boolean tracked(LivingEntity entity) {
        return entity instanceof Player || entity instanceof TenkaichiFighter;
    }

    public static float incoming(LivingEntity target, ServerLevel level, DamageSource source, float amount) {
        if (amount <= 0 || !tracked(target) || source.is(DamageTypes.FELL_OUT_OF_WORLD) || source.is(DamageTypes.GENERIC_KILL)) {
            return amount;
        }
        if (!(source.getEntity() instanceof LivingEntity attacker) || attacker == target) {
            return amount;
        }
        Fighter defender = Fighter.of(target);
        if (defender.form() == null) {
            return amount;
        }
        long now = level.getGameTime();
        RuntimeState rt = CombatSystem.runtime(target);
        double dodge = defender.passive(FormPassive.AUTO_DODGE);
        if (dodge > 0 && rt != null && rt.stunnedUntil <= now && rt.transformingUntil <= now && target.getRandom().nextDouble() < dodge) {
            Vec3 away = target.position().subtract(attacker.position()).multiply(1, 0, 1);
            Vec3 side = away.lengthSqr() < 1.0e-4 ? new Vec3(1, 0, 0) : new Vec3(-away.z, 0, away.x).normalize();
            if (target.getRandom().nextBoolean()) {
                side = side.scale(-1);
            }
            CombatSystem.knockback(target, side.scale(0.9).add(0, 0.15, 0));
            rt.invulnerableUntil = Math.max(rt.invulnerableUntil, now + 5);
            CombatSystem.anim(target, side.x >= 0 ? AnimClip.DODGE_RIGHT : AnimClip.DODGE_LEFT, 7, 0);
            CombatSystem.fx(target, CombatFxKind.DODGE, attacker, target, target.position().add(0, 1, 0), 1.2f);
            CombatSystem.playSound(level, target, ModSounds.DODGE.get(), 0.8f, 1.3f);
            return 0.0f;
        }
        double intimidate = defender.passive(FormPassive.INTIMIDATE);
        if (intimidate > 0 && Fighter.of(attacker).battlePower() < defender.battlePower()) {
            amount *= (float) (1.0 - Math.min(0.7, intimidate));
        }
        return amount;
    }

    public static void after(LivingEntity target, ServerLevel level, DamageSource source, float amount) {
        if (amount <= 0 || !(source.getEntity() instanceof LivingEntity attacker) || attacker == target) {
            return;
        }
        long now = level.getGameTime();
        if (tracked(target)) {
            Fighter defender = Fighter.of(target);
            RuntimeState rt = CombatSystem.runtime(target);
            double lust = defender.passive(FormPassive.BATTLE_LUST);
            if (lust > 0 && rt != null) {
                rt.battleLust = Math.min(lust, rt.battleLust + 0.04);
                rt.battleLustUntil = now + 200;
            }
        }
        if (tracked(attacker)) {
            double destruction = Fighter.of(attacker).passive(FormPassive.DESTRUCTION);
            if (destruction > 0) {
                drainKi(target, amount * destruction * 2.0);
            }
        }
    }

    public static void drainKi(LivingEntity target, double amount) {
        if (target instanceof net.minecraft.server.level.ServerPlayer player) {
            PlayerData data = PlayerManager.data(player);
            if (data.isCreated()) {
                data.setKi(Math.max(0.0, data.ki() - amount));
                data.runtime().vitalsDirty = true;
            }
        } else if (target instanceof TenkaichiFighter npc) {
            npc.spendFighterKi(Math.min(amount, npc.fighterKiRatio() * 1000.0));
        }
    }

    public static boolean blocksTechniques(PlayerData data) {
        FormDefinition form = data.isTransformed() ? FormRegistry.get(data.form(), false) : null;
        if (form == null) {
            return false;
        }
        double rampage = form.passive(FormPassive.RAMPAGE);
        return rampage > 0 && data.mastery(form.id) < rampage;
    }

    public static boolean blocksRevert(PlayerData data) {
        return blocksTechniques(data);
    }
}
