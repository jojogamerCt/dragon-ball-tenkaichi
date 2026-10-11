package com.dbtenkaichi.combat;

import com.dbtenkaichi.config.BalanceConfig;
import com.dbtenkaichi.config.ConfigManager;
import com.dbtenkaichi.form.FormDefinition;
import com.dbtenkaichi.form.FormRegistry;
import com.dbtenkaichi.platform.Services;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.player.RuntimeState;
import com.dbtenkaichi.race.Race;
import com.dbtenkaichi.stats.BattlePower;
import com.dbtenkaichi.stats.StatBlock;
import com.dbtenkaichi.stats.StatType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public record Fighter(LivingEntity entity, StatBlock stats, Race race, @Nullable FormDefinition form, float mastery, double kiRatio, double fatigue,
                      @Nullable RuntimeState runtime, @Nullable PlayerData data, float suppression) {

    public static Fighter of(LivingEntity entity) {
        boolean client = entity.level().isClientSide();
        if (entity instanceof Player player) {
            PlayerData data = Services.PLATFORM.playerData(player);
            FormDefinition form = data.isTransformed() ? FormRegistry.get(data.form(), client) : null;
            double maxKi = Math.max(1.0, data.runtime().cachedMaxKi);
            double ki = data.ki() < 0 ? maxKi : data.ki();
            return new Fighter(entity, data.stats(), data.race(), form, data.mastery(data.form()), ki / maxKi, data.fatigue(), data.runtime(), data, data.suppression());
        }
        if (entity instanceof TenkaichiFighter npc) {
            FormDefinition form = npc.fighterForm() == null ? null : FormRegistry.get(npc.fighterForm(), client);
            return new Fighter(entity, npc.fighterStats(), npc.fighterRace(), form, npc.fighterMastery(), npc.fighterKiRatio(), 0.0, npc.fighterRuntime(), null, 1.0f);
        }
        return new Fighter(entity, vanillaStats(entity), Race.HUMAN, null, 0.0f, 1.0, 0.0, null, null, 1.0f);
    }

    public static StatBlock vanillaStats(LivingEntity entity) {
        StatBlock block = new StatBlock();
        double health = entity.getMaxHealth();
        double armor = entity.getArmorValue();
        double attack = entity.getAttribute(Attributes.ATTACK_DAMAGE) != null ? entity.getAttributeValue(Attributes.ATTACK_DAMAGE) : 1.0;
        int con = (int) Math.max(1, Math.round(health / 4.0));
        int def = (int) Math.max(1, Math.round(armor * 2.0));
        int str = (int) Math.max(1, Math.round(attack * 1.5));
        block.set(StatType.CONSTITUTION, con);
        block.set(StatType.DEFENSE, def);
        block.set(StatType.STRENGTH, str);
        block.set(StatType.KI_OUTPUT, Math.max(1, str / 2));
        block.set(StatType.SPEED, 2);
        block.set(StatType.AGILITY, 2);
        block.set(StatType.MARTIAL_ARTS, Math.max(1, str / 2));
        return block;
    }

    public double formBp() {
        return form == null ? 1.0 : form.bpMultiplier;
    }

    public double attackMultiplier(BalanceConfig cfg) {
        double race = com.dbtenkaichi.race.RaceMechanics.attackFactor(entity, this.race);
        return (form == null ? 1.0 : form.attack(cfg) * form.controlFactor(mastery)) * race;
    }

    public double defenseMultiplier(BalanceConfig cfg) {
        return form == null ? 1.0 : form.defense(cfg);
    }

    public double kiMultiplier(BalanceConfig cfg) {
        double race = com.dbtenkaichi.race.RaceMechanics.kiFactor(entity, this.race);
        return (form == null ? 1.0 : form.ki(cfg) * form.controlFactor(mastery)) * race;
    }

    public double speedMultiplier() {
        return form == null ? 1.0 : form.speedMultiplier * form.controlFactor(mastery);
    }

    public long battlePower() {
        BalanceConfig cfg = ConfigManager.balance();
        double health = entity.getMaxHealth() <= 0 ? 1.0 : entity.getHealth() / entity.getMaxHealth();
        return BattlePower.effective(stats, cfg, new BattlePower.Condition(formBp(), kiRatio, health, fatigue, 1.0));
    }

    public long displayedBattlePower() {
        BalanceConfig cfg = ConfigManager.balance();
        double health = entity.getMaxHealth() <= 0 ? 1.0 : entity.getHealth() / entity.getMaxHealth();
        return BattlePower.effective(stats, cfg, new BattlePower.Condition(formBp(), kiRatio, health, fatigue, suppression));
    }

    public int stat(StatType type) {
        return stats.get(type);
    }
}
