package com.dbtenkaichi.story;

import com.dbtenkaichi.anim.AnimClip;
import com.dbtenkaichi.combat.CombatFxKind;
import com.dbtenkaichi.combat.CombatSystem;
import com.dbtenkaichi.entity.KiBeamEntity;
import com.dbtenkaichi.entity.KiBlastEntity;
import com.dbtenkaichi.entity.PowerBallEntity;
import com.dbtenkaichi.entity.StoryNpc;
import com.dbtenkaichi.ki.SkillStyle;
import com.dbtenkaichi.ki.Technique;
import com.dbtenkaichi.ki.Techniques;
import com.dbtenkaichi.mission.MissionManager;
import com.dbtenkaichi.movement.FlightSystem;
import com.dbtenkaichi.network.Packets;
import com.dbtenkaichi.platform.Services;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.player.RuntimeState;
import com.dbtenkaichi.registry.ModSounds;
import com.dbtenkaichi.server.PlayerManager;
import com.dbtenkaichi.world.DestructionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SceneDirector {
    private static final class Running {
        final UUID player;
        final StoryScene scene;
        final ServerLevel level;
        final BlockPos anchor;
        final Map<String, Integer> actors = new HashMap<>();
        final Map<String, String> carried = new HashMap<>();
        final List<StoryScene.Action> pending = new ArrayList<>();
        final Map<Integer, Long> chargeUntil = new HashMap<>();
        final Runnable onEnd;
        Vec3 spot;
        float yaw;
        @Nullable Vec3 returnPos;
        @Nullable ResourceKey<Level> returnDim;
        int orb = -1;
        int t;

        Running(UUID player, StoryScene scene, ServerLevel level, BlockPos anchor, Runnable onEnd) {
            this.player = player;
            this.scene = scene;
            this.level = level;
            this.anchor = anchor;
            this.onEnd = onEnd;
        }
    }

    private static final Map<UUID, Running> RUNNING = new HashMap<>();

    private SceneDirector() {
    }

    public static boolean running(ServerPlayer player) {
        return RUNNING.containsKey(player.getUUID());
    }

    private static Vec3 at(Running r, double x, double y, double z) {
        return new Vec3(r.anchor.getX() + 0.5 + x, r.anchor.getY() + y, r.anchor.getZ() + 0.5 + z);
    }

    private static @Nullable Vec3 dry(ServerLevel level, double x, double z, double near) {
        int bx = (int) Math.floor(x);
        int bz = (int) Math.floor(z);
        level.getChunk(bx >> 4, bz >> 4);
        int gy = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bx, bz);
        BlockPos below = new BlockPos(bx, gy - 1, bz);
        if (!level.getFluidState(below).isEmpty() || !level.getFluidState(below.above()).isEmpty() || Math.abs(gy - near) > 6) {
            return null;
        }
        return new Vec3(x, gy, z);
    }

    private static Vec3 ground(Running r, double x, double y, double z) {
        Vec3 p = at(r, x, y, z);
        if (y > 0.5) {
            return p;
        }
        Vec3 found = dry(r.level, p.x, p.z, p.y);
        if (found != null) {
            return found;
        }
        Vec3 anchor = at(r, 0, 0, 0);
        for (int i = 1; i <= 24; i++) {
            Vec3 q = p.lerp(anchor, i / 24.0);
            found = dry(r.level, q.x, q.z, p.y);
            if (found != null) {
                return found;
            }
        }
        for (int ring = 1; ring <= 6; ring++) {
            for (int k = 0; k < 12; k++) {
                double a = k * Math.PI / 6.0;
                found = dry(r.level, anchor.x + Math.cos(a) * ring * 1.5, anchor.z + Math.sin(a) * ring * 1.5, p.y);
                if (found != null) {
                    return found;
                }
            }
        }
        return p;
    }

    public static void start(ServerPlayer player, PlayerData data, StoryScene scene, ServerLevel level, BlockPos anchor, Runnable onEnd) {
        end(player, false);
        Running r = new Running(player.getUUID(), scene, level, anchor, onEnd);
        RuntimeState rt = data.runtime();
        if (rt.flying) {
            FlightSystem.stop(player, data);
        }
        rt.inScene = true;
        rt.charging = false;
        rt.skillSlot = -1;
        rt.stateDirty = true;
        if (scene.remote || player.level() != level) {
            r.returnPos = player.position();
            r.returnDim = player.level().dimension();
        }
        StoryScene.PlayerSpec ps = scene.player;
        r.spot = ground(r, ps.x(), ps.y(), ps.z());
        r.yaw = ps.yaw();
        player.teleport(new TeleportTransition(level, r.spot, Vec3.ZERO, r.yaw, 0.0f, TeleportTransition.DO_NOTHING));
        if (ps.hidden()) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, scene.duration + 20, 0, false, false));
        }
        for (StoryScene.ActorSpec spec : scene.actors) {
            StoryNpc npc = spec.keep() ? MissionManager.npc(player, spec.name()) : null;
            Vec3 pos = spec.y() > 0.5 || spec.fly() ? at(r, spec.x(), spec.y(), spec.z()) : ground(r, spec.x(), spec.y(), spec.z());
            if (npc == null || npc.level() != level) {
                StoryCharacters.Character character = StoryCharacters.get(spec.character());
                if (character == null) {
                    continue;
                }
                npc = StoryNpc.spawn(level, character, pos, StoryNpc.ROLE_ACTOR, player, 1.0);
                if (npc == null) {
                    continue;
                }
                MissionManager.register(player, spec.name(), npc);
            } else {
                npc.teleportTo(pos.x, pos.y, pos.z);
            }
            npc.setRole(StoryNpc.ROLE_ACTOR);
            npc.setTag(spec.name());
            npc.stopMoving();
            npc.setYRot(spec.yaw());
            npc.setYHeadRot(spec.yaw());
            npc.setYBodyRot(spec.yaw());
            npc.setHidden(spec.hidden());
            npc.setFighterFlying(spec.fly() || spec.y() > 0.5);
            npc.setHealth(npc.getMaxHealth());
            r.actors.put(spec.name(), npc.getId());
        }
        RUNNING.put(player.getUUID(), r);
        CompoundTag tag = new CompoundTag();
        tag.putString("scene", scene.id);
        tag.putDouble("ax", anchor.getX() + 0.5);
        tag.putDouble("ay", anchor.getY());
        tag.putDouble("az", anchor.getZ() + 0.5);
        tag.putBoolean("seen", data.missions().flag("seen:" + scene.id));
        CompoundTag actors = new CompoundTag();
        r.actors.forEach(actors::putInt);
        actors.putInt("player", player.getId());
        tag.put("actors", actors);
        Services.NETWORK.sendToPlayer(player, new Packets.Data("scene", tag));
    }

    private static @Nullable Entity actor(Running r, ServerPlayer player, String name) {
        if ("player".equals(name)) {
            return player;
        }
        Integer id = r.actors.get(name);
        return id == null ? null : r.level.getEntity(id);
    }

    public static void tick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, Running>> it = RUNNING.entrySet().iterator();
        List<Running> finished = new ArrayList<>();
        while (it.hasNext()) {
            Running r = it.next().getValue();
            ServerPlayer player = server.getPlayerList().getPlayer(r.player);
            if (player == null) {
                cleanup(r, null);
                it.remove();
                continue;
            }
            r.t++;
            for (StoryScene.Action action : r.scene.actions) {
                if (action.at() == r.t) {
                    run(r, player, action);
                }
            }
            Iterator<StoryScene.Action> pending = r.pending.iterator();
            while (pending.hasNext()) {
                StoryScene.Action action = pending.next();
                if (action.at() <= r.t) {
                    run(r, player, action);
                    pending.remove();
                }
            }
            r.carried.forEach((carried, carrier) -> {
                Entity a = actor(r, player, carried);
                Entity b = actor(r, player, carrier);
                if (a != null && b != null) {
                    a.teleportTo(b.getX() + 0.6, b.getY() + 0.6, b.getZ());
                }
            });
            long now = r.level.getGameTime();
            r.chargeUntil.entrySet().removeIf(e -> {
                if (e.getValue() <= now) {
                    Entity entity = r.level.getEntity(e.getKey());
                    RuntimeState rt = entity instanceof LivingEntity living ? CombatSystem.runtime(living) : null;
                    if (rt != null) {
                        rt.skillSlot = -1;
                        rt.stateDirty = true;
                    }
                    return true;
                }
                return false;
            });
            PlayerData data = PlayerManager.data(player);
            data.runtime().invulnerableUntil = Math.max(data.runtime().invulnerableUntil, now + 5);
            if (player.level() == r.level && player.position().distanceToSqr(r.spot) > 0.25) {
                player.teleportTo(r.spot.x, r.spot.y, r.spot.z);
                player.setDeltaMovement(Vec3.ZERO);
            }
            if (r.t >= r.scene.duration) {
                finished.add(r);
                it.remove();
            }
        }
        for (Running r : finished) {
            ServerPlayer player = server.getPlayerList().getPlayer(r.player);
            cleanup(r, player);
            if (player != null) {
                PlayerManager.data(player).missions().setFlag("seen:" + r.scene.id, true);
                r.onEnd.run();
            }
        }
    }

    public static void skip(ServerPlayer player) {
        Running r = RUNNING.get(player.getUUID());
        if (r == null || !PlayerManager.data(player).missions().flag("seen:" + r.scene.id)) {
            return;
        }
        for (StoryScene.Action action : r.scene.actions) {
            if (action.at() > r.t && isPersistent(action.type())) {
                run(r, player, action);
            }
        }
        for (StoryScene.Action action : r.pending) {
            run(r, player, action);
        }
        r.pending.clear();
        r.t = r.scene.duration - 1;
    }

    private static boolean isPersistent(String type) {
        return switch (type) {
            case "form", "unform", "despawn", "tail_cut", "halo_off", "player_move", "teleport", "apply_form" -> true;
            default -> false;
        };
    }

    public static void end(ServerPlayer player, boolean notify) {
        Running r = RUNNING.remove(player.getUUID());
        if (r != null) {
            cleanup(r, player);
            if (notify) {
                r.onEnd.run();
            }
        }
    }

    private static void cleanup(Running r, @Nullable ServerPlayer player) {
        for (StoryScene.ActorSpec spec : r.scene.actors) {
            Integer id = r.actors.get(spec.name());
            Entity entity = id == null ? null : r.level.getEntity(id);
            if (entity instanceof StoryNpc npc && !spec.keep()) {
                npc.discard();
            } else if (entity instanceof StoryNpc npc) {
                npc.setHidden(false);
                npc.stopMoving();
                npc.fighterRuntime().charging = false;
                npc.fighterRuntime().skillSlot = -1;
            }
        }
        if (r.orb >= 0) {
            Entity orb = r.level.getEntity(r.orb);
            if (orb != null) {
                orb.discard();
            }
        }
        if (player == null) {
            return;
        }
        PlayerData data = PlayerManager.data(player);
        RuntimeState rt = data.runtime();
        rt.inScene = false;
        rt.charging = false;
        rt.skillSlot = -1;
        rt.kaioken = 0;
        rt.stateDirty = true;
        player.removeEffect(MobEffects.INVISIBILITY);
        if (r.returnPos != null && r.scene.remote && r.returnDim != null) {
            ServerLevel back = player.level().getServer().getLevel(r.returnDim);
            if (back != null) {
                player.teleport(new TeleportTransition(back, r.returnPos, Vec3.ZERO, player.getYRot(), 0.0f, TeleportTransition.DO_NOTHING));
            }
        }
        Services.NETWORK.sendToPlayer(player, new Packets.Data("scene_end", new CompoundTag()));
    }

    private static SoundEvent sound(String name) {
        return switch (name) {
            case "flight_takeoff" -> ModSounds.FLIGHT_TAKEOFF.get();
            case "punch_heavy" -> ModSounds.PUNCH_HEAVY.get();
            case "beam_fire" -> ModSounds.BEAM_FIRE.get();
            case "land" -> ModSounds.LAND.get();
            case "ki_impact" -> ModSounds.KI_IMPACT.get();
            case "transform_scream" -> ModSounds.TRANSFORM_SCREAM.get();
            case "explosion" -> ModSounds.EXPLOSION.get();
            default -> ModSounds.AURA_BURST.get();
        };
    }

    private static @Nullable SkillStyle chargeStyle(String name) {
        return switch (name) {
            case "SBC_CHARGE" -> SkillStyle.SPECIAL_BEAM_CANNON;
            case "GALICK_CHARGE" -> SkillStyle.GALICK_GUN;
            case "KAMEHAMEHA_CHARGE" -> SkillStyle.KAMEHAMEHA;
            case "SPIRIT_RAISE" -> SkillStyle.RAISED;
            case "TRI_BEAM" -> SkillStyle.TRIANGLE;
            default -> null;
        };
    }

    private static void face(Entity entity, Vec3 target) {
        Vec3 d = target.subtract(entity.getEyePosition());
        float yaw = (float) (Math.toDegrees(Math.atan2(d.z, d.x)) - 90.0);
        float pitch = (float) (-Math.toDegrees(Math.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z))));
        if (entity instanceof ServerPlayer player) {
            player.teleport(new TeleportTransition((ServerLevel) player.level(), player.position(), Vec3.ZERO, yaw, pitch, TeleportTransition.DO_NOTHING));
        } else {
            entity.setYRot(yaw);
            entity.setXRot(pitch);
            entity.setYHeadRot(yaw);
            if (entity instanceof LivingEntity living) {
                living.setYBodyRot(yaw);
            }
        }
    }

    private static void run(Running r, ServerPlayer player, StoryScene.Action a) {
        Entity entity = actor(r, player, a.actor());
        StoryNpc npc = entity instanceof StoryNpc s ? s : null;
        ServerLevel level = r.level;
        long now = level.getGameTime();
        switch (a.type()) {
            case "move" -> {
                if (npc != null) {
                    npc.moveTo(at(r, a.x(), a.y(), a.z()), a.amount(), "fly".equals(a.other()));
                }
            }
            case "teleport" -> {
                if (entity != null) {
                    Vec3 p = a.y() > 0.5 ? at(r, a.x(), a.y(), a.z()) : ground(r, a.x(), a.y(), a.z());
                    entity.teleportTo(p.x, p.y, p.z);
                }
            }
            case "land" -> {
                if (npc != null) {
                    npc.setFighterFlying(false);
                    npc.stopMoving();
                    Vec3 p = ground(r, a.x(), 0, a.z());
                    npc.teleportTo(p.x, p.y, p.z);
                }
            }
            case "look" -> {
                Entity other = actor(r, player, a.other());
                if (npc != null && other != null) {
                    npc.lookAtEntity(other.getId());
                    npc.lookAtPoint(other.getEyePosition());
                }
            }
            case "anim" -> {
                if (!(entity instanceof LivingEntity living)) {
                    return;
                }
                SkillStyle style = chargeStyle(a.value());
                if (style != null) {
                    RuntimeState rt = CombatSystem.runtime(living);
                    if (rt != null) {
                        rt.skillStyle = style.ordinal();
                        rt.skillSlot = 0;
                        rt.skillStart = now;
                        rt.stateDirty = true;
                        r.chargeUntil.put(living.getId(), now + (long) a.amount());
                    }
                    return;
                }
                try {
                    AnimClip clip = AnimClip.valueOf(a.value());
                    CombatSystem.anim(living, clip, (int) a.amount(), 0);
                } catch (IllegalArgumentException ignored) {
                }
            }
            case "fx" -> {
                if (entity != null) {
                    try {
                        CombatFxKind kind = CombatFxKind.valueOf(a.value());
                        CombatSystem.fx(entity, kind, entity, entity, entity.position().add(0, entity.getBbHeight() * 0.5, 0), (float) a.amount());
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
            case "beam" -> {
                Entity target = actor(r, player, a.other());
                Technique technique = Techniques.get(a.value());
                if (entity instanceof LivingEntity owner && target != null && technique != null) {
                    face(owner, target.getBoundingBox().getCenter());
                    KiBeamEntity beam = KiBeamEntity.create(level, owner, technique, a.amount());
                    beam.setDirection(target.getBoundingBox().getCenter().subtract(beam.position()));
                    level.addFreshEntity(beam);
                    RuntimeState rt = CombatSystem.runtime(owner);
                    if (rt != null) {
                        rt.activeBeam = beam.getId();
                        rt.skillStyle = SkillStyle.of(technique.id()).ordinal();
                        rt.stateDirty = true;
                    }
                    CombatSystem.anim(owner, AnimClip.BEAM_FIRE, technique.duration() + 20, 0);
                }
            }
            case "blast" -> {
                Entity target = actor(r, player, a.other());
                Technique technique = Techniques.get(a.value());
                if (entity instanceof LivingEntity owner && technique != null) {
                    if (target != null) {
                        face(owner, target.getBoundingBox().getCenter());
                    }
                    KiBlastEntity blast;
                    if (technique.type() == Technique.Type.SPHERE) {
                        blast = KiBlastEntity.sphere(level, owner, technique, a.amount(), target == null ? -1 : target.getId());
                        if (r.orb >= 0) {
                            Entity orb = level.getEntity(r.orb);
                            if (orb != null) {
                                blast.setPos(orb.position());
                                orb.discard();
                            }
                            r.orb = -1;
                        }
                        Vec3 aim = target == null ? owner.getViewVector(1.0f) : target.getBoundingBox().getCenter().subtract(blast.position());
                        blast.setDeltaMovement(aim.normalize().scale(technique.speed()));
                    } else {
                        blast = KiBlastEntity.create(level, owner, technique, technique.glowColor(), target == null ? -1 : target.getId(), a.amount());
                    }
                    level.addFreshEntity(blast);
                }
            }
            case "form" -> {
                if (npc != null) {
                    npc.playCinematic(a.value(), (int) a.amount());
                    r.pending.add(new StoryScene.Action(r.t + (int) a.amount(), "apply_form", a.actor(), "", a.value(), 0, 0, 0, 0));
                }
            }
            case "apply_form" -> {
                if (npc != null) {
                    npc.setStoryForm(a.value());
                }
            }
            case "unform" -> {
                if (npc != null) {
                    npc.setStoryForm(null);
                    CombatSystem.fx(npc, CombatFxKind.AURA_BURST, npc, npc, npc.position(), 1.4f);
                }
            }
            case "show" -> {
                if (npc != null) {
                    npc.setHidden(false);
                }
            }
            case "hide" -> {
                if (npc != null) {
                    npc.setHidden(true);
                }
            }
            case "aura" -> {
                RuntimeState rt = entity instanceof LivingEntity living ? CombatSystem.runtime(living) : null;
                if (rt != null) {
                    rt.charging = a.amount() > 0;
                    rt.stateDirty = true;
                }
            }
            case "player_aura" -> {
                PlayerData data = PlayerManager.data(player);
                data.runtime().charging = a.amount() > 0;
                data.runtime().stateDirty = true;
            }
            case "fly" -> {
                if (npc != null) {
                    npc.setFighterFlying(a.amount() > 0);
                }
            }
            case "sound" -> {
                if (entity != null) {
                    CombatSystem.playSound(level, entity, sound(a.value()), 1.4f, (float) Math.max(0.3, a.amount()));
                }
            }
            case "despawn" -> {
                if (entity != null && entity != player) {
                    entity.discard();
                    r.actors.remove(a.actor());
                    MissionManager.unregister(player, a.actor());
                }
            }
            case "explode" -> {
                if (entity != null) {
                    Vec3 p = entity.position().add(0, entity.getBbHeight() * 0.5, 0);
                    DestructionManager.sendNear(level, p, 160, new Packets.CombatFx(CombatFxKind.EXPLOSION.ordinal(), -1, -1, p.x, p.y, p.z, (float) a.amount()));
                    level.playSound(null, p.x, p.y, p.z, ModSounds.EXPLOSION.get(), net.minecraft.sounds.SoundSource.PLAYERS, 2.0f, 0.8f);
                }
            }
            case "player_move" -> {
                r.spot = ground(r, a.x(), a.y(), a.z());
                player.teleportTo(r.spot.x, r.spot.y, r.spot.z);
            }
            case "carry" -> r.carried.put(a.actor(), a.other());
            case "kaioken_fx" -> {
                PlayerData data = PlayerManager.data(player);
                data.runtime().kaioken = (int) a.amount();
                data.runtime().stateDirty = true;
            }
            case "clash_win" -> {
                Entity loser = actor(r, player, a.other());
                if (loser != null) {
                    for (KiBeamEntity beam : level.getEntitiesOfClass(KiBeamEntity.class, loser.getBoundingBox().inflate(80.0), b -> b.ownerId() == loser.getId())) {
                        beam.onClashLost();
                    }
                }
            }
            case "power_ball" -> {
                if (entity instanceof LivingEntity owner) {
                    level.addFreshEntity(PowerBallEntity.create(level, owner));
                }
            }
            case "tail_cut" -> {
                if (npc != null) {
                    npc.cutTail();
                    npc.setStoryForm(null);
                    CombatSystem.fx(npc, CombatFxKind.AURA_BURST, npc, npc, npc.position(), 1.6f);
                }
            }
            case "orb" -> {
                if (entity instanceof LivingEntity owner) {
                    Entity old = r.orb >= 0 ? level.getEntity(r.orb) : null;
                    if (old != null) {
                        old.discard();
                    }
                    KiBlastEntity orb = KiBlastEntity.sphere(level, owner, Techniques.SPIRIT_BOMB, Math.min(1.0, a.amount() / 4.0), -1);
                    orb.setPos(owner.position().add(0, a.y(), 0));
                    orb.setDeltaMovement(Vec3.ZERO);
                    orb.setTraining(true);
                    orb.delayed(100000);
                    level.addFreshEntity(orb);
                    r.orb = orb.getId();
                }
            }
            case "orb_handoff" -> {
                Entity other = actor(r, player, a.other());
                Entity orb = r.orb >= 0 ? level.getEntity(r.orb) : null;
                if (other != null && orb != null) {
                    orb.teleportTo(other.getX(), other.getY() + other.getBbHeight() + 2.5, other.getZ());
                }
            }
            case "halo_off" -> {
                PlayerData data = PlayerManager.data(player);
                data.missions().setFlag("halo", false);
                data.runtime().appearanceDirty = true;
                data.runtime().dataDirty = true;
            }
            default -> {
            }
        }
    }
}
