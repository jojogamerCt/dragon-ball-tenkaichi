package com.dbtenkaichi.mission;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.entity.StoryNpc;
import com.dbtenkaichi.form.FormRegistry;
import com.dbtenkaichi.network.Packets;
import com.dbtenkaichi.platform.Services;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.registry.ModSounds;
import com.dbtenkaichi.server.PlayerManager;
import com.dbtenkaichi.stats.BattlePower;
import com.dbtenkaichi.config.ConfigManager;
import com.dbtenkaichi.story.SceneDirector;
import com.dbtenkaichi.story.Scenes;
import com.dbtenkaichi.story.StoryCharacters;
import com.dbtenkaichi.story.StoryScene;
import com.dbtenkaichi.world.OtherWorld;
import com.dbtenkaichi.world.StoryWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class MissionManager {
    private static final Map<UUID, Map<String, Integer>> NPCS = new HashMap<>();
    private static final Map<UUID, Set<String>> SPAWNED = new HashMap<>();
    private static final Map<UUID, Set<String>> STARTED_SCENES = new HashMap<>();

    private MissionManager() {
    }

    public static void register(ServerPlayer player, String tag, StoryNpc npc) {
        NPCS.computeIfAbsent(player.getUUID(), k -> new HashMap<>()).put(tag, npc.getId());
    }

    public static void unregister(ServerPlayer player, String tag) {
        Map<String, Integer> map = NPCS.get(player.getUUID());
        if (map != null) {
            map.remove(tag);
        }
    }

    public static @Nullable StoryNpc npc(ServerPlayer player, String tag) {
        Map<String, Integer> map = NPCS.get(player.getUUID());
        if (map == null) {
            return null;
        }
        Integer id = map.get(tag);
        if (id == null) {
            return null;
        }
        for (ServerLevel level : player.level().getServer().getAllLevels()) {
            Entity entity = level.getEntity(id);
            if (entity instanceof StoryNpc npc && npc.isAlive()) {
                return npc;
            }
        }
        map.remove(tag);
        return null;
    }

    public static void despawnAll(ServerPlayer player) {
        Map<String, Integer> map = NPCS.remove(player.getUUID());
        if (map != null) {
            for (String tag : map.keySet()) {
                for (ServerLevel level : player.level().getServer().getAllLevels()) {
                    Entity entity = level.getEntity(map.get(tag));
                    if (entity instanceof StoryNpc npc) {
                        npc.discard();
                    }
                }
            }
        }
        SPAWNED.remove(player.getUUID());
    }

    private static void despawn(ServerPlayer player, String tag) {
        StoryNpc npc = npc(player, tag);
        if (npc != null) {
            npc.discard();
        }
        unregister(player, tag);
    }

    public static void startStory(ServerPlayer player, PlayerData data) {
        MissionState state = data.missions();
        for (Mission mission : Missions.story()) {
            if (state.isActive(mission.id)) {
                return;
            }
        }
        for (Mission mission : Missions.story()) {
            if (!state.isCompleted(mission.id)) {
                start(player, data, mission);
                return;
            }
        }
    }

    public static boolean available(PlayerData data, Mission mission) {
        MissionState state = data.missions();
        if (state.isActive(mission.id) || mission.story && state.isCompleted(mission.id)) {
            return false;
        }
        if (!mission.prerequisite.isEmpty() && !state.isCompleted(mission.prerequisite)) {
            return false;
        }
        if (mission.story) {
            for (Mission other : Missions.story()) {
                if (other.id.equals(mission.id)) {
                    break;
                }
                if (!state.isCompleted(other.id)) {
                    return false;
                }
            }
        }
        return BattlePower.base(data.stats(), ConfigManager.balance()) >= mission.minBattlePower;
    }

    public static void start(ServerPlayer player, PlayerData data, Mission mission) {
        MissionState state = data.missions();
        if (state.isActive(mission.id)) {
            return;
        }
        MissionState.Active active = state.start(mission.id, player.level().getGameTime());
        state.track(mission.id);
        enterStage(player, data, mission, active);
        title(player, Component.translatable(mission.story ? "message.dbtenkaichi.story_mission" : "message.dbtenkaichi.side_mission"),
                Component.translatable(mission.titleKey()));
        sync(player, data);
    }

    private static void enterStage(ServerPlayer player, PlayerData data, Mission mission, MissionState.Active active) {
        Set<String> spawned = SPAWNED.computeIfAbsent(player.getUUID(), k -> new HashSet<>());
        spawned.remove(mission.id);
        Set<String> started = STARTED_SCENES.computeIfAbsent(player.getUUID(), k -> new HashSet<>());
        started.remove(mission.id);
        active.progress = 0;
        active.stageStart = player.level().getGameTime();
        if (active.stage < mission.stages.size()) {
            for (String action : mission.stages.get(active.stage).onStart) {
                action(player, data, action);
            }
        }
        data.runtime().dataDirty = true;
    }

    private static void completeStage(ServerPlayer player, PlayerData data, Mission mission, MissionState.Active active) {
        Mission.Stage stage = mission.stages.get(active.stage);
        for (String action : stage.onComplete) {
            action(player, data, action);
        }
        active.stage++;
        if (active.stage >= mission.stages.size()) {
            complete(player, data, mission);
            return;
        }
        player.playSound(ModSounds.UI_CONFIRM.get(), 0.8f, 1.2f);
        enterStage(player, data, mission, active);
        sync(player, data);
    }

    private static void complete(ServerPlayer player, PlayerData data, Mission mission) {
        MissionState state = data.missions();
        state.complete(mission.id);
        if (mission.rewardTp > 0) {
            data.addTrainingPoints(mission.rewardTp);
        }
        for (String skill : mission.rewardSkills) {
            data.learnSkill(skill);
        }
        data.runtime().dataDirty = true;
        data.runtime().vitalsDirty = true;
        title(player, Component.translatable("message.dbtenkaichi.mission_complete"), Component.translatable(mission.titleKey()));
        player.sendSystemMessage(Component.translatable("message.dbtenkaichi.mission_reward", Component.translatable(mission.titleKey()), mission.rewardTp));
        player.playSound(ModSounds.TRAINING_SUCCESS.get(), 1.0f, 1.0f);
        Mission next = Missions.get(mission.next);
        if (next != null && !state.isCompleted(next.id)) {
            start(player, data, next);
        } else {
            sync(player, data);
        }
    }

    public static void forceComplete(ServerPlayer player, PlayerData data, String id) {
        Mission mission = Missions.get(id);
        MissionState.Active active = data.missions().get(id);
        if (mission == null || active == null) {
            return;
        }
        SceneDirector.end(player, false);
        completeStage(player, data, mission, active);
    }

    private static void action(ServerPlayer player, PlayerData data, String action) {
        int colon = action.indexOf(':');
        String verb = colon < 0 ? action : action.substring(0, colon);
        String arg = colon < 0 ? "" : action.substring(colon + 1);
        switch (verb) {
            case "despawn_all" -> despawnAll(player);
            case "despawn" -> despawn(player, arg);
            case "flag" -> {
                data.missions().setFlag(arg, true);
                data.runtime().appearanceDirty = true;
            }
            case "unflag" -> {
                data.missions().setFlag(arg, false);
                data.runtime().appearanceDirty = true;
            }
            case "learn" -> {
                if (!data.hasSkill(arg)) {
                    data.learnSkill(arg);
                    player.sendSystemMessage(Component.translatable("message.dbtenkaichi.learned", Component.translatable(skillKey(arg))));
                }
            }
            case "teleport" -> {
                StoryWorld.Site site = StoryWorld.site(player.level().getServer(), arg);
                if (site != null) {
                    BlockPos p = site.pos();
                    player.teleport(new TeleportTransition(site.level(), new Vec3(p.getX() + 0.5, p.getY() + 0.2, p.getZ() + 0.5), Vec3.ZERO, player.getYRot(), 0.0f, TeleportTransition.DO_NOTHING));
                    player.resetFallDistance();
                }
            }
            default -> Tenkaichi.LOG.warn("Unknown mission action {}", action);
        }
        data.runtime().dataDirty = true;
    }

    private static String skillKey(String id) {
        if ("dbtenkaichi:kaioken".equals(id)) {
            return "technique.dbtenkaichi.kaioken";
        }
        var technique = com.dbtenkaichi.ki.Techniques.get(id);
        return technique == null ? id : technique.translationKey();
    }

    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % 5 != 0) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PlayerData data = PlayerManager.data(player);
            if (!data.isCreated() || player.isSpectator()) {
                continue;
            }
            try {
                tickPlayer(player, data);
            } catch (RuntimeException e) {
                Tenkaichi.LOG.error("Mission tick failed for {}", player.getPlainTextName(), e);
            }
            if (server.getTickCount() % 20 == 0 && !data.missions().active().isEmpty()) {
                sync(player, data);
            }
        }
    }

    private static void tickPlayer(ServerPlayer player, PlayerData data) {
        MissionState state = data.missions();
        for (MissionState.Active active : state.active().values().toArray(new MissionState.Active[0])) {
            Mission mission = Missions.get(active.id);
            if (mission == null) {
                state.abandon(active.id);
                continue;
            }
            if (active.stage >= mission.stages.size()) {
                complete(player, data, mission);
                continue;
            }
            Mission.Stage stage = mission.stages.get(active.stage);
            switch (stage.kind) {
                case GOTO -> {
                    StoryWorld.Site site = StoryWorld.site(player.level().getServer(), stage.site);
                    if (site != null && OtherWorld.is(site.level()) && player.level() != site.level() && mission.story && !SceneDirector.running(player)) {
                        action(player, data, "teleport:check_in");
                        player.sendSystemMessage(Component.translatable("message.dbtenkaichi.returned_other_world"));
                    } else if (site != null && player.level() == site.level() && player.position().distanceTo(Vec3.atCenterOf(site.pos())) <= stage.radius) {
                        completeStage(player, data, mission, active);
                    }
                }
                case SCENE -> startScene(player, data, mission, active, stage);
                case FIGHT -> fight(player, data, mission, active, stage);
                case CATCH, HIT -> chase(player, data, mission, active, stage);
                case FLAG -> {
                    if (state.flag(stage.target)) {
                        completeStage(player, data, mission, active);
                    }
                }
                case BATTLE_POWER -> {
                    if (BattlePower.base(data.stats(), ConfigManager.balance()) >= stage.amount) {
                        completeStage(player, data, mission, active);
                    }
                }
                case DELIVER -> deliver(player, data, mission, active, stage);
                default -> {
                }
            }
        }
    }

    private static void startScene(ServerPlayer player, PlayerData data, Mission mission, MissionState.Active active, Mission.Stage stage) {
        Set<String> started = STARTED_SCENES.computeIfAbsent(player.getUUID(), k -> new HashSet<>());
        if (started.contains(mission.id) || SceneDirector.running(player) || com.dbtenkaichi.training.TrialManager.active(player)) {
            return;
        }
        StoryScene scene = Scenes.get(stage.target);
        if (scene == null) {
            completeStage(player, data, mission, active);
            return;
        }
        StoryWorld.Site site = StoryWorld.site(player.level().getServer(), scene.site);
        if (site == null) {
            return;
        }
        if (!scene.remote && player.level() != site.level() && !"check_in".equals(scene.site) && !"king_kai_planet".equals(scene.site)) {
            return;
        }
        if (!scene.remote && player.level() == site.level() && player.position().distanceTo(Vec3.atCenterOf(site.pos())) > 64) {
            return;
        }
        started.add(mission.id);
        int stageIndex = active.stage;
        SceneDirector.start(player, data, scene, site.level(), site.pos(), () -> {
            MissionState.Active now = data.missions().get(mission.id);
            if (now != null && now.stage == stageIndex) {
                completeStage(player, data, mission, now);
            }
        });
    }

    private static Vec3 sitePos(StoryWorld.Site site, double x, double y, double z) {
        return new Vec3(site.pos().getX() + 0.5 + x, site.pos().getY() + y + 0.1, site.pos().getZ() + 0.5 + z);
    }

    private static void fight(ServerPlayer player, PlayerData data, Mission mission, MissionState.Active active, Mission.Stage stage) {
        StoryWorld.Site site = StoryWorld.site(player.level().getServer(), stage.site);
        if (site == null || player.level() != site.level()) {
            return;
        }
        Set<String> spawned = SPAWNED.computeIfAbsent(player.getUUID(), k -> new HashSet<>());
        if (!spawned.contains(mission.id)) {
            if (player.position().distanceTo(Vec3.atCenterOf(site.pos())) > 56 || SceneDirector.running(player)) {
                return;
            }
            spawned.add(mission.id);
            for (Mission.Fighter spec : stage.fighters) {
                StoryNpc npc = npc(player, spec.tag());
                StoryCharacters.Character character = StoryCharacters.get(spec.character());
                if (character == null) {
                    continue;
                }
                if (npc == null) {
                    npc = StoryNpc.spawn(site.level(), character, sitePos(site, spec.x(), spec.y(), spec.z()), spec.role(), player, spec.power());
                    if (npc == null) {
                        continue;
                    }
                    register(player, spec.tag(), npc);
                } else {
                    npc.setup(character, spec.role(), player, spec.power());
                }
                npc.setTag(spec.tag());
                npc.setRole(spec.role());
                npc.setHidden(false);
                npc.setStoryForm(spec.form());
                npc.setHealth(npc.getMaxHealth());
                if (spec.role() == StoryNpc.ROLE_ENEMY) {
                    npc.setBossThreshold(spec.threshold());
                    npc.setTarget(player);
                }
            }
            title(player, Component.translatable("message.dbtenkaichi.story_fight"), Component.translatable(mission.stages.get(active.stage).objectiveKey(mission.id, active.stage)));
            return;
        }
        boolean allDown = true;
        boolean any = false;
        for (Mission.Fighter spec : stage.fighters) {
            if (spec.role() != StoryNpc.ROLE_ENEMY) {
                continue;
            }
            any = true;
            StoryNpc npc = npc(player, spec.tag());
            if (npc == null) {
                spawned.remove(mission.id);
                return;
            }
            if (!npc.defeated()) {
                allDown = false;
            }
        }
        if (any && allDown) {
            for (Mission.Fighter spec : stage.fighters) {
                StoryNpc npc = npc(player, spec.tag());
                if (npc != null) {
                    npc.setRole(StoryNpc.ROLE_ACTOR);
                    npc.setBossThreshold(-1.0f);
                    npc.fighterRuntime().invulnerableUntil = 0;
                }
            }
            completeStage(player, data, mission, active);
        }
    }

    private static void chase(ServerPlayer player, PlayerData data, Mission mission, MissionState.Active active, Mission.Stage stage) {
        StoryWorld.Site site = StoryWorld.site(player.level().getServer(), stage.site);
        if (site == null || player.level() != site.level() || SceneDirector.running(player)) {
            return;
        }
        Set<String> spawned = SPAWNED.computeIfAbsent(player.getUUID(), k -> new HashSet<>());
        StoryNpc npc = npc(player, stage.target);
        if (!spawned.contains(mission.id) || npc == null) {
            if (player.position().distanceTo(Vec3.atCenterOf(site.pos())) > 48) {
                return;
            }
            spawned.add(mission.id);
            Mission.Fighter spec = stage.fighters.get(0);
            StoryCharacters.Character character = StoryCharacters.get(spec.character());
            if (character == null) {
                return;
            }
            npc = StoryNpc.spawn(site.level(), character, sitePos(site, spec.x(), spec.y(), spec.z()), stage.kind == Mission.Kind.HIT ? StoryNpc.ROLE_ENEMY : StoryNpc.ROLE_ACTOR, player, spec.power());
            if (npc == null) {
                return;
            }
            register(player, stage.target, npc);
            npc.setTag(stage.target);
            npc.setFlee(Vec3.atCenterOf(site.pos()), 14.0, stage.kind == Mission.Kind.HIT);
            if (stage.kind == Mission.Kind.HIT) {
                npc.setBossThreshold(0.98f);
            }
            return;
        }
        if (stage.kind == Mission.Kind.CATCH && npc.distanceTo(player) < 2.2) {
            completeStage(player, data, mission, active);
        } else if (stage.kind == Mission.Kind.HIT && npc.defeated()) {
            completeStage(player, data, mission, active);
        }
    }

    private static void deliver(ServerPlayer player, PlayerData data, Mission mission, MissionState.Active active, Mission.Stage stage) {
        StoryWorld.Site site = StoryWorld.site(player.level().getServer(), stage.site);
        Identifier id = Identifier.tryParse(stage.target);
        Item item = id == null ? null : BuiltInRegistries.ITEM.getValue(id);
        if (item == null) {
            return;
        }
        int have = 0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(item)) {
                have += stack.getCount();
            }
        }
        active.progress = Math.min(stage.count, have);
        if (site == null || player.level() != site.level() || player.position().distanceTo(Vec3.atCenterOf(site.pos())) > stage.radius || have < stage.count) {
            return;
        }
        int left = stage.count;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (left > 0 && stack.is(item)) {
                int take = Math.min(left, stack.getCount());
                stack.shrink(take);
                left -= take;
            }
        }
        completeStage(player, data, mission, active);
    }

    public static void onStoryEvent(ServerLevel level, StoryNpc npc, String event) {
        if (npc.owner() == null) {
            return;
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(npc.owner());
        if (player == null) {
            return;
        }
        PlayerData data = PlayerManager.data(player);
        if ("defeated".equals(event)) {
            player.playSound(ModSounds.AURA_BURST.get(), 0.8f, 1.3f);
        }
        data.runtime().dataDirty = true;
    }

    public static void onTechnique(ServerPlayer player, PlayerData data, String technique) {
        for (MissionState.Active active : data.missions().active().values().toArray(new MissionState.Active[0])) {
            Mission mission = Missions.get(active.id);
            if (mission == null || active.stage >= mission.stages.size()) {
                continue;
            }
            Mission.Stage stage = mission.stages.get(active.stage);
            if (stage.kind == Mission.Kind.TECHNIQUE && stage.target.equals(technique)) {
                completeStage(player, data, mission, active);
            }
        }
    }

    public static void onKill(ServerPlayer player, PlayerData data, LivingEntity victim) {
        String type = BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).toString();
        for (MissionState.Active active : data.missions().active().values().toArray(new MissionState.Active[0])) {
            Mission mission = Missions.get(active.id);
            if (mission == null || active.stage >= mission.stages.size()) {
                continue;
            }
            Mission.Stage stage = mission.stages.get(active.stage);
            if (stage.kind == Mission.Kind.DEFEAT && stage.target.equals(type)) {
                active.progress++;
                data.runtime().dataDirty = true;
                if (active.progress >= stage.count) {
                    completeStage(player, data, mission, active);
                } else {
                    sync(player, data);
                }
            }
        }
    }

    public static void onPlayerDeath(ServerPlayer player) {
        SceneDirector.end(player, false);
        Map<String, Integer> map = NPCS.get(player.getUUID());
        if (map != null) {
            for (Iterator<Map.Entry<String, Integer>> it = map.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Integer> entry = it.next();
                for (ServerLevel level : player.level().getServer().getAllLevels()) {
                    if (level.getEntity(entry.getValue()) instanceof StoryNpc npc && npc.role() != StoryNpc.ROLE_ACTOR) {
                        npc.discard();
                        it.remove();
                        break;
                    }
                }
            }
        }
        SPAWNED.remove(player.getUUID());
        STARTED_SCENES.remove(player.getUUID());
    }

    public static void onLeave(ServerPlayer player) {
        SceneDirector.end(player, false);
        despawnAll(player);
        STARTED_SCENES.remove(player.getUUID());
    }

    public static void request(ServerPlayer player, PlayerData data, String kind, String value) {
        MissionState state = data.missions();
        Mission mission = Missions.get(value);
        switch (kind) {
            case "mission_track" -> {
                state.track(value);
                sync(player, data);
            }
            case "mission_accept" -> {
                if (mission != null && !mission.story && available(data, mission)) {
                    start(player, data, mission);
                } else if (mission != null && mission.story) {
                    startStory(player, data);
                }
            }
            case "mission_abandon" -> {
                if (mission != null && !mission.story) {
                    state.abandon(value);
                    sync(player, data);
                }
            }
            case "target_form" -> {
                if (value.isEmpty() || FormRegistry.get(value, false) != null && data.hasUnlockedForm(value)) {
                    data.setTargetForm(value);
                    data.runtime().dataDirty = true;
                }
            }
            case "missions_sync" -> sync(player, data);
            default -> {
            }
        }
    }

    public static void sync(ServerPlayer player, PlayerData data) {
        MissionState state = data.missions();
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("waypoint", state.waypoint());
        String tracked = state.tracked();
        Mission mission = Missions.get(tracked);
        MissionState.Active active = state.get(tracked);
        if (mission != null && active != null && active.stage < mission.stages.size()) {
            Mission.Stage stage = mission.stages.get(active.stage);
            tag.putString("tracked", mission.id);
            tag.putBoolean("story", mission.story);
            tag.putString("title", mission.titleKey());
            tag.putString("objective", stage.objectiveKey(mission.id, active.stage));
            tag.putInt("stage", active.stage);
            tag.putInt("stages", mission.stages.size());
            tag.putInt("progress", active.progress);
            tag.putInt("count", stage.count);
            waypoint(player, mission, active, stage, tag);
        }
        ListTag list = new ListTag();
        for (MissionState.Active a : state.active().values()) {
            Mission m = Missions.get(a.id);
            if (m == null) {
                continue;
            }
            CompoundTag entry = new CompoundTag();
            entry.putString("id", m.id);
            entry.putBoolean("story", m.story);
            entry.putInt("stage", a.stage);
            entry.putInt("stages", m.stages.size());
            list.add(entry);
        }
        tag.put("active", list);
        ListTag available = new ListTag();
        for (Mission m : Missions.all().values()) {
            if (!state.isActive(m.id) && !(m.story && state.isCompleted(m.id))) {
                CompoundTag entry = new CompoundTag();
                entry.putString("id", m.id);
                entry.putBoolean("story", m.story);
                entry.putBoolean("ready", available(data, m));
                entry.putBoolean("done", state.isCompleted(m.id));
                entry.putLong("reward", m.rewardTp);
                entry.putLong("bp", m.minBattlePower);
                available.add(entry);
            }
        }
        tag.put("available", available);
        tag.putInt("completed", state.completed().size());
        tag.putBoolean("halo", state.flag("halo"));
        Services.NETWORK.sendToPlayer(player, new Packets.Data("missions", tag));
    }

    private static void waypoint(ServerPlayer player, Mission mission, MissionState.Active active, Mission.Stage stage, CompoundTag tag) {
        MinecraftServer server = player.level().getServer();
        String siteKey = stage.site;
        Vec3 point = null;
        String dimension = null;
        if (stage.kind == Mission.Kind.FIGHT || stage.kind == Mission.Kind.CATCH || stage.kind == Mission.Kind.HIT) {
            for (Mission.Fighter spec : stage.fighters) {
                StoryNpc npc = npc(player, spec.tag());
                if (npc != null && (spec.role() == StoryNpc.ROLE_ENEMY || stage.kind != Mission.Kind.FIGHT)) {
                    point = npc.position().add(0, npc.getBbHeight() + 0.6, 0);
                    dimension = npc.level().dimension().identifier().toString();
                    tag.putInt("entity", npc.getId());
                    break;
                }
            }
        }
        if (stage.kind == Mission.Kind.SCENE) {
            StoryScene scene = Scenes.get(stage.target);
            siteKey = scene == null ? "" : scene.site;
        }
        if (point == null && siteKey != null && !siteKey.isEmpty()) {
            StoryWorld.Site site = StoryWorld.site(server, siteKey);
            if (site != null) {
                point = Vec3.atCenterOf(site.pos()).add(0, 1.5, 0);
                dimension = site.level().dimension().identifier().toString();
            }
        }
        if (point != null) {
            tag.putDouble("wx", point.x);
            tag.putDouble("wy", point.y);
            tag.putDouble("wz", point.z);
            tag.putString("wdim", dimension);
        }
    }

    private static void title(ServerPlayer player, Component title, Component subtitle) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(8, 50, 12));
        player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        player.connection.send(new ClientboundSetTitleTextPacket(title));
    }

    public static void toggleWaypoint(ServerPlayer player, PlayerData data) {
        data.missions().setWaypoint(!data.missions().waypoint());
        data.runtime().dataDirty = true;
        sync(player, data);
    }

    public static boolean isStoryNpcOwner(Player player, StoryNpc npc) {
        return player.getUUID().equals(npc.owner());
    }

    public static void onOtherWorld(ServerPlayer player, PlayerData data, long now) {
        OtherWorld.tick(player, data, now);
    }
}
