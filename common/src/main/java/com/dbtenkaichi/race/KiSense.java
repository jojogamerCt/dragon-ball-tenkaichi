package com.dbtenkaichi.race;

import com.dbtenkaichi.combat.Fighter;
import com.dbtenkaichi.combat.TenkaichiFighter;
import com.dbtenkaichi.entity.MasterEntity;
import com.dbtenkaichi.entity.StoryNpc;
import com.dbtenkaichi.form.FormDefinition;
import com.dbtenkaichi.form.FormPassive;
import com.dbtenkaichi.item.ScouterItem;
import com.dbtenkaichi.network.Packets;
import com.dbtenkaichi.platform.Services;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.player.RuntimeState;
import com.dbtenkaichi.server.PlayerManager;
import com.dbtenkaichi.stats.StatType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class KiSense {
    public static final int KIND_ENEMY = 0;
    public static final int KIND_PLAYER = 1;
    public static final int KIND_ALLY = 2;
    public static final int KIND_CREATURE = 3;
    public static final long SCOUTER_LIMIT = 5_000_000_000L;
    private static final int MAX_ENTRIES = 24;

    private KiSense() {
    }

    public static double range(PlayerData data, boolean scouter) {
        double base = 48.0 + data.stat(StatType.KI_CONTROL) * 0.6 + data.stat(StatType.SPIRIT) * 0.4;
        if (scouter) {
            base += 96.0;
        }
        return Math.min(320.0, base);
    }

    public static boolean godKi(LivingEntity entity) {
        Fighter fighter = Fighter.of(entity);
        if (fighter.race().has(RacePassive.DIVINE_KI)) {
            return true;
        }
        FormDefinition form = fighter.form();
        return form != null && form.has(FormPassive.GOD_KI);
    }

    public static boolean canSense(LivingEntity observer, LivingEntity target, boolean scouter) {
        if (target instanceof Player player) {
            PlayerData data = PlayerManager.data(player);
            if (!data.isCreated() || player.isSpectator()) {
                return false;
            }
            if (RaceMechanics.hiddenFromSenses(player, data.race(), observer)) {
                return false;
            }
        } else if (target instanceof TenkaichiFighter npc && npc.fighterRace().has(RacePassive.UNSENSABLE) && target.distanceToSqr(observer) > 144.0) {
            return false;
        }
        if (godKi(target) && !godKi(observer)) {
            return false;
        }
        return !scouter || !godKi(target);
    }

    public static void toggle(ServerPlayer player, PlayerData data) {
        RuntimeState rt = data.runtime();
        rt.sensing = !rt.sensing;
        rt.stateDirty = true;
        rt.nextSenseUpdate = 0;
        if (!rt.sensing) {
            Services.NETWORK.sendToPlayer(player, new Packets.Data("sense", new CompoundTag()));
        }
        player.sendOverlayMessage(Component.translatable(rt.sensing ? "message.dbtenkaichi.sense_on" : "message.dbtenkaichi.sense_off"));
    }

    public static void tick(ServerPlayer player, PlayerData data, long now) {
        RuntimeState rt = data.runtime();
        boolean scouter = ScouterItem.worn(player);
        if ((!rt.sensing && !scouter) || now < rt.nextSenseUpdate) {
            return;
        }
        rt.nextSenseUpdate = now + 10;
        Services.NETWORK.sendToPlayer(player, new Packets.Data("sense", scan(player, data, scouter, now)));
    }

    private record Entry(LivingEntity entity, double distance) {
    }

    public static CompoundTag scan(ServerPlayer player, PlayerData data, boolean scouter, long now) {
        ServerLevel level = player.level();
        double range = range(data, scouter);
        AABB box = player.getBoundingBox().inflate(range);
        List<Entry> found = new ArrayList<>();
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive())) {
            boolean fighter = entity instanceof Player || entity instanceof TenkaichiFighter;
            double d = entity.distanceTo(player);
            if (d > range || (!fighter && !(entity instanceof Enemy && d < Math.min(range, 48.0)))) {
                continue;
            }
            if (!canSense(player, entity, scouter)) {
                continue;
            }
            found.add(new Entry(entity, d));
        }
        found.sort(Comparator.comparingDouble(Entry::distance));
        long selfBp = Fighter.of(player).battlePower();
        ListTag list = new ListTag();
        long highest = 0;
        for (int i = 0; i < Math.min(MAX_ENTRIES, found.size()); i++) {
            LivingEntity entity = found.get(i).entity();
            Fighter fighter = Fighter.of(entity);
            long bp = entity instanceof Player ? fighter.displayedBattlePower() : fighter.battlePower();
            highest = Math.max(highest, bp);
            CompoundTag entry = new CompoundTag();
            entry.putInt("id", entity.getId());
            entry.putDouble("x", entity.getX());
            entry.putDouble("y", entity.getY() + entity.getBbHeight() * 0.6);
            entry.putDouble("z", entity.getZ());
            entry.putLong("bp", bp);
            entry.putFloat("ratio", (float) Math.min(99.0, bp / (double) Math.max(1L, selfBp)));
            entry.putInt("kind", kind(player, entity));
            entry.putString("name", entity.getDisplayName().getString());
            list.add(entry);
        }
        CompoundTag tag = new CompoundTag();
        tag.put("entries", list);
        tag.putBoolean("scouter", scouter);
        tag.putBoolean("sensing", data.runtime().sensing);
        tag.putDouble("range", range);
        if (scouter && highest > 9000L && !data.missions().flag("scouter_9000")) {
            data.missions().setFlag("scouter_9000", true);
            data.runtime().dataDirty = true;
            player.sendSystemMessage(Component.translatable("message.dbtenkaichi.scouter_9000"));
        }
        if (scouter && highest > SCOUTER_LIMIT) {
            ScouterItem.overload(player);
        }
        return tag;
    }

    private static int kind(ServerPlayer observer, LivingEntity entity) {
        if (entity instanceof Player) {
            return KIND_PLAYER;
        }
        if (entity instanceof StoryNpc npc) {
            return npc.hostile() ? KIND_ENEMY : KIND_ALLY;
        }
        if (entity instanceof MasterEntity) {
            return KIND_ALLY;
        }
        if (entity instanceof Enemy) {
            return entity instanceof TenkaichiFighter ? KIND_ENEMY : KIND_CREATURE;
        }
        return KIND_ALLY;
    }
}
