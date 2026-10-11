package com.dbtenkaichi.race;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.combat.CombatFxKind;
import com.dbtenkaichi.combat.CombatSystem;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.player.RuntimeState;
import com.dbtenkaichi.registry.ModSounds;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class RaceMechanics {
    public static final double STRETCH_RANGE = 1.2;
    private static final Identifier ELASTIC_KNOCKBACK = Tenkaichi.id("elastic_knockback");
    private static final Identifier ELASTIC_FALL = Tenkaichi.id("elastic_fall");
    private static final Identifier SPACE_OXYGEN = Tenkaichi.id("space_oxygen");

    private RaceMechanics() {
    }

    public static double attackFactor(LivingEntity entity, Race race) {
        double health = entity.getMaxHealth() <= 0 ? 1.0 : entity.getHealth() / entity.getMaxHealth();
        double factor = 1.0;
        if (race.has(RacePassive.HIDDEN_POTENTIAL) && health < 0.4) {
            factor *= 1.25;
        }
        if (race.has(RacePassive.DEMONIC_RAGE) && health < 0.35) {
            factor *= 1.3;
        }
        return factor;
    }

    public static double kiFactor(LivingEntity entity, Race race) {
        double factor = attackFactor(entity, race);
        if (race.has(RacePassive.DIVINE_KI)) {
            factor *= 1.15;
        }
        return factor;
    }

    public static double meleeRangeBonus(Race race) {
        return race.has(RacePassive.STRETCH_LIMBS) ? STRETCH_RANGE : 0.0;
    }

    public static double zenkaiFactor(Race race) {
        return race.has(RacePassive.HIDDEN_POTENTIAL) ? 0.6 : 1.0;
    }

    public static double fatigueFactor(Race race) {
        return race.has(RacePassive.ENERGY_RESERVE) ? 0.0 : 1.0;
    }

    public static double practiceFactor(Race race) {
        return race.has(RacePassive.TECHNIQUE_MASTERY) ? 1.25 : 1.0;
    }

    public static double combatGainFactor(Race race, double opponentWorth) {
        return race.has(RacePassive.BATTLE_HUNGER) && opponentWorth >= 0.8 ? 1.5 : 1.0;
    }

    public static float skillMasteryFactor(Race race) {
        return race.has(RacePassive.TECHNIQUE_MASTERY) ? 1.5f : 1.0f;
    }

    public static boolean hiddenFromSenses(LivingEntity entity, Race race, LivingEntity observer) {
        return race.has(RacePassive.UNSENSABLE) && entity.distanceToSqr(observer) > 12.0 * 12.0;
    }

    public static void applyAttributes(ServerPlayer player, PlayerData data) {
        Race race = data.race();
        boolean elastic = data.isCreated() && race.has(RacePassive.ELASTIC_BODY);
        set(player, Attributes.KNOCKBACK_RESISTANCE, ELASTIC_KNOCKBACK, elastic ? 0.4 : 0.0, AttributeModifier.Operation.ADD_VALUE);
        set(player, Attributes.FALL_DAMAGE_MULTIPLIER, ELASTIC_FALL, elastic ? -0.6 : 0.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        boolean space = data.isCreated() && race.has(RacePassive.SPACE_SURVIVAL);
        set(player, Attributes.OXYGEN_BONUS, SPACE_OXYGEN, space ? 1000.0 : 0.0, AttributeModifier.Operation.ADD_VALUE);
    }

    private static void set(ServerPlayer player, Holder<Attribute> attribute, Identifier id, double amount, AttributeModifier.Operation op) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        if (Math.abs(amount) < 1.0e-6) {
            instance.removeModifier(id);
        } else {
            instance.addOrUpdateTransientModifier(new AttributeModifier(id, amount, op));
        }
    }

    public static double tick(ServerPlayer player, PlayerData data, double ki, long now) {
        Race race = data.race();
        RuntimeState rt = data.runtime();
        if (race.has(RacePassive.SPACE_SURVIVAL)) {
            player.setAirSupply(player.getMaxAirSupply());
            player.setTicksFrozen(0);
        }
        if (!race.has(RacePassive.REGENERATION) || now % 20 != 0 || player.getHealth() >= player.getMaxHealth()) {
            return ki;
        }
        boolean inCombat = now - rt.lastCombat < 60;
        double percent;
        double kiPerHealth;
        if (race.has(RacePassive.ELASTIC_BODY)) {
            percent = inCombat ? 0.015 : 0.03;
            kiPerHealth = 1.0;
        } else if (race == Race.NAMEKIAN) {
            if (inCombat) {
                return ki;
            }
            percent = 0.02;
            kiPerHealth = 1.5;
        } else {
            if (inCombat) {
                return ki;
            }
            percent = 0.015;
            kiPerHealth = 1.2;
        }
        float missing = player.getMaxHealth() - player.getHealth();
        float heal = (float) Math.min(missing, player.getMaxHealth() * percent);
        double cost = heal * kiPerHealth;
        if (ki < cost || heal <= 0) {
            return ki;
        }
        player.heal(heal);
        if (now % 60 == 0) {
            CombatSystem.fx(player, CombatFxKind.LEVEL_UP, player, player, player.position(), 0.5f);
        }
        return ki - cost;
    }

    public static void onKill(ServerPlayer player, PlayerData data, LivingEntity victim) {
        if (!data.race().has(RacePassive.ABSORPTION) || victim == player) {
            return;
        }
        player.heal(player.getMaxHealth() * 0.2f);
        data.setKi(Math.min(data.runtime().cachedMaxKi, data.ki() + data.runtime().cachedMaxKi * 0.15));
        data.runtime().vitalsDirty = true;
        CombatSystem.fx(player, CombatFxKind.AURA_BURST, player, player, player.position(), 0.6f);
        CombatSystem.playSound(player.level(), player, ModSounds.AURA_BURST.get(), 0.7f, 1.4f);
    }

    public static void onMeleeHit(ServerPlayer attacker, PlayerData data, double damage) {
        if (data.race().has(RacePassive.DEMONIC_RAGE)) {
            data.setKi(Math.min(data.runtime().cachedMaxKi, data.ki() + damage * 0.3));
            data.runtime().vitalsDirty = true;
        }
    }
}
