package com.dbtenkaichi.training;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.entity.MasterEntity;
import com.dbtenkaichi.entity.MasterKind;
import com.dbtenkaichi.movement.FlightSystem;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.player.RuntimeState;
import com.dbtenkaichi.registry.ModEntities;
import com.dbtenkaichi.registry.ModSounds;
import com.dbtenkaichi.server.PlayerManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class TrialManager {
    public static final ResourceKey<Level> REALM = ResourceKey.create(Registries.DIMENSION, Tenkaichi.id("training_realm"));
    public static final int COUNTDOWN = 100;
    public static final int TIME_LIMIT = 20 * 180;

    private static final class Trial {
        final Lessons.Lesson lesson;
        final MasterKind kind;
        final BlockPos center;
        final Vec3 stand;
        final long start;
        int opponent = -1;
        boolean fighting;

        Trial(Lessons.Lesson lesson, MasterKind kind, BlockPos center, Vec3 stand, long start) {
            this.lesson = lesson;
            this.kind = kind;
            this.center = center;
            this.stand = stand;
            this.start = start;
        }
    }

    private static final Map<UUID, Trial> ACTIVE = new HashMap<>();

    private TrialManager() {
    }

    public static boolean isRealm(Level level) {
        return REALM.equals(level.dimension());
    }

    public static boolean active(ServerPlayer player) {
        return ACTIVE.containsKey(player.getUUID());
    }

    public static boolean start(ServerPlayer player, PlayerData data, Lessons.Lesson lesson, MasterKind kind) {
        RuntimeState rt = data.runtime();
        long now = player.level().getGameTime();
        if (!lesson.trial() || !lesson.mentors().contains(kind)) {
            return false;
        }
        if (active(player) || isRealm(player.level()) || rt.trainingActivity != null || rt.busy(now) || now - rt.lastCombat < 100 || data.returnPoint() != null) {
            player.sendSystemMessage(Component.translatable("message.dbtenkaichi.trial_busy"));
            return false;
        }
        int state = Lessons.state(player, data, lesson);
        if (state == Lessons.LOCKED) {
            player.sendSystemMessage(Component.translatable("message.dbtenkaichi.lesson_locked", lesson.battlePower()));
            return false;
        }
        if (state != Lessons.AVAILABLE) {
            return false;
        }
        ServerLevel realm = player.level().getServer().getLevel(REALM);
        if (realm == null) {
            player.sendSystemMessage(Component.translatable("message.dbtenkaichi.trial_unavailable"));
            return false;
        }
        CompoundTag back = new CompoundTag();
        back.putString("dim", player.level().dimension().identifier().toString());
        back.putDouble("x", player.getX());
        back.putDouble("y", player.getY());
        back.putDouble("z", player.getZ());
        back.putFloat("yaw", player.getYRot());
        back.putFloat("pitch", player.getXRot());
        back.putFloat("health", player.getHealth());
        data.setReturnPoint(back);
        rt.dataDirty = true;
        if (rt.flying) {
            FlightSystem.stop(player, data);
        }
        int slot = Math.floorMod(player.getUUID().hashCode(), 1024);
        BlockPos center = new BlockPos((slot % 32) * 512 - 8192, 100, (slot / 32) * 512 - 8192);
        kind.arena().build(realm, center);
        Vec3 stand = Vec3.atBottomCenterOf(center.offset(0, 0, -7));
        player.teleport(new TeleportTransition(realm, stand, Vec3.ZERO, 0.0f, 0.0f, TeleportTransition.DO_NOTHING));
        Trial trial = new Trial(lesson, kind, center, stand, realm.getGameTime());
        MasterEntity opponent = ModEntities.MASTER.get().spawn(realm, center.offset(0, 0, 7), EntitySpawnReason.EVENT);
        if (opponent != null) {
            opponent.setKind(kind);
            opponent.prepareTrial(player, data);
            trial.opponent = opponent.getId();
        }
        ACTIVE.put(player.getUUID(), trial);
        rt.invulnerableUntil = Math.max(rt.invulnerableUntil, realm.getGameTime() + COUNTDOWN);
        rt.stateDirty = true;
        title(player, Component.translatable(kind.arena().translationKey()), Component.translatable("message.dbtenkaichi.trial_vs", Component.translatable(kind.translationKey())), 10, 50, 10);
        return true;
    }

    public static void tick(MinecraftServer server) {
        ServerLevel realm = server.getLevel(REALM);
        Iterator<Map.Entry<UUID, Trial>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Trial> entry = it.next();
            Trial trial = entry.getValue();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null || realm == null) {
                discard(realm, trial);
                it.remove();
                continue;
            }
            PlayerData data = PlayerManager.data(player);
            if (!isRealm(player.level()) || !player.isAlive()) {
                it.remove();
                finish(player, data, trial, false, realm);
                continue;
            }
            long t = realm.getGameTime() - trial.start;
            MasterEntity opponent = realm.getEntity(trial.opponent) instanceof MasterEntity master ? master : null;
            if (t < COUNTDOWN) {
                if (player.position().distanceToSqr(trial.stand) > 0.05) {
                    player.teleportTo(trial.stand.x, trial.stand.y, trial.stand.z);
                }
                if (t % 20 == 0) {
                    title(player, Component.literal(String.valueOf((COUNTDOWN - t) / 20)), Component.translatable(trial.lesson.translationKey()), 0, 22, 4);
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.UI_CLICK.get(), SoundSource.PLAYERS, 1.0f, 0.8f);
                }
                continue;
            }
            if (!trial.fighting) {
                trial.fighting = true;
                title(player, Component.translatable("message.dbtenkaichi.trial_fight"), Component.empty(), 0, 20, 10);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.AURA_BURST.get(), SoundSource.PLAYERS, 1.2f, 1.0f);
                if (opponent != null) {
                    opponent.startTrialFight(player);
                }
            }
            boolean win = opponent == null || !opponent.isAlive() || opponent.getHealth() <= opponent.getMaxHealth() * 0.25f;
            boolean lose = player.getHealth() <= player.getMaxHealth() * 0.2f || player.getY() < trial.center.getY() - 12 || t > COUNTDOWN + TIME_LIMIT;
            if (win || lose) {
                it.remove();
                finish(player, data, trial, win && !lose, realm);
            } else if (t % 20 == 0) {
                long left = (COUNTDOWN + TIME_LIMIT - t) / 20;
                long seconds = left % 60;
                player.sendOverlayMessage(Component.translatable("message.dbtenkaichi.trial_time", left / 60, (seconds < 10 ? "0" : "") + seconds));
            }
        }
        if (server.getTickCount() % 20 == 0) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                PlayerData data = PlayerManager.data(player);
                if (data.returnPoint() != null && !ACTIVE.containsKey(player.getUUID()) && player.isAlive()) {
                    returnHome(player, data);
                }
            }
        }
    }

    public static boolean forceEnd(ServerPlayer player, boolean win) {
        Trial trial = ACTIVE.remove(player.getUUID());
        if (trial == null) {
            return false;
        }
        finish(player, PlayerManager.data(player), trial, win, player.level().getServer().getLevel(REALM));
        return true;
    }

    public static void onLeave(ServerPlayer player) {
        Trial trial = ACTIVE.remove(player.getUUID());
        if (trial != null) {
            discard(player.level().getServer().getLevel(REALM), trial);
        }
    }

    private static void finish(ServerPlayer player, PlayerData data, Trial trial, boolean win, ServerLevel realm) {
        discard(realm, trial);
        if (win) {
            title(player, Component.translatable("message.dbtenkaichi.trial_cleared"), Component.translatable(trial.lesson.translationKey()), 5, 50, 15);
            Lessons.grant(player, data, trial.lesson);
        } else {
            title(player, Component.translatable("message.dbtenkaichi.trial_failed"), Component.translatable(trial.lesson.translationKey()), 5, 50, 15);
        }
        returnHome(player, data);
    }

    private static void discard(ServerLevel realm, Trial trial) {
        if (realm != null && trial.opponent >= 0 && realm.getEntity(trial.opponent) instanceof MasterEntity master) {
            master.discard();
        }
    }

    public static void returnHome(ServerPlayer player, PlayerData data) {
        CompoundTag back = data.returnPoint();
        if (back == null) {
            return;
        }
        data.setReturnPoint(null);
        MinecraftServer server = player.level().getServer();
        Identifier id = Identifier.tryParse(back.getStringOr("dim", "minecraft:overworld"));
        ServerLevel level = id == null ? null : server.getLevel(ResourceKey.create(Registries.DIMENSION, id));
        if (level == null || isRealm(level)) {
            level = server.overworld();
        }
        Vec3 pos = new Vec3(back.getDoubleOr("x", 0.0), back.getDoubleOr("y", 100.0), back.getDoubleOr("z", 0.0));
        player.teleport(new TeleportTransition(level, pos, Vec3.ZERO, back.getFloatOr("yaw", 0.0f), back.getFloatOr("pitch", 0.0f), TeleportTransition.DO_NOTHING));
        player.resetFallDistance();
        player.setHealth(Math.max(player.getHealth(), Math.min(player.getMaxHealth(), back.getFloatOr("health", player.getMaxHealth()))));
        RuntimeState rt = data.runtime();
        rt.resetCombat();
        rt.dataDirty = true;
        PlayerManager.applyAttributes(player, data);
    }

    private static void title(ServerPlayer player, Component title, Component subtitle, int in, int stay, int out) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(in, stay, out));
        player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        player.connection.send(new ClientboundSetTitleTextPacket(title));
    }
}
