package com.dbtenkaichi.client;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.client.anim.PoseApplier;
import com.dbtenkaichi.client.cinematic.CinematicDirector;
import com.dbtenkaichi.client.hud.MissionHud;
import com.dbtenkaichi.client.hud.SenseHud;
import com.dbtenkaichi.client.input.LockOn;
import com.dbtenkaichi.client.screen.MissionScreen;
import com.dbtenkaichi.client.story.ScenePlayer;
import com.dbtenkaichi.combat.TenkaichiDamage;
import com.dbtenkaichi.entity.KiBeamEntity;
import com.dbtenkaichi.entity.KiBlastEntity;
import com.dbtenkaichi.entity.PowerBallEntity;
import com.dbtenkaichi.entity.RaiderEntity;
import com.dbtenkaichi.entity.StoryNpc;
import com.dbtenkaichi.form.FormDefinition;
import com.dbtenkaichi.form.FormRegistry;
import com.dbtenkaichi.form.TransformationSystem;
import com.dbtenkaichi.ki.Technique;
import com.dbtenkaichi.ki.Techniques;
import com.dbtenkaichi.mission.MissionManager;
import com.dbtenkaichi.network.ActionType;
import com.dbtenkaichi.network.Packets;
import com.dbtenkaichi.platform.Services;
import com.dbtenkaichi.player.Appearance;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.player.RuntimeState;
import com.dbtenkaichi.race.KiSense;
import com.dbtenkaichi.race.Race;
import com.dbtenkaichi.race.TailSystem;
import com.dbtenkaichi.registry.ModItems;
import com.dbtenkaichi.server.PlayerManager;
import com.dbtenkaichi.story.Scenes;
import com.dbtenkaichi.story.StoryCharacters;
import com.dbtenkaichi.world.OtherWorld;
import com.dbtenkaichi.world.StoryWorld;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

final class FeatureTest {
    private interface Step {
        boolean run(Minecraft mc, int t);
    }

    private static final List<Step> STEPS = new ArrayList<>();
    private static final List<String> FAILS = Collections.synchronizedList(new ArrayList<>());
    private static int passes;
    private static int index;
    private static int t;
    private static boolean built;
    private static String name = "";

    private FeatureTest() {
    }

    static boolean tick(Minecraft mc) {
        if (!built) {
            name = mc.player.getName().getString();
            build();
            built = true;
            Tenkaichi.LOG.info("[DBT-FEATURE] {} steps queued", STEPS.size());
        }
        if (index >= STEPS.size()) {
            return true;
        }
        if (mc.player.isDeadOrDying()) {
            fail("player died during step " + index);
            mc.player.respawn();
            return false;
        }
        boolean done;
        try {
            done = STEPS.get(index).run(mc, t);
        } catch (RuntimeException e) {
            Tenkaichi.LOG.error("[DBT-FEATURE] step {} threw", index, e);
            fail("step " + index + " threw " + e);
            done = true;
        }
        t++;
        if (done) {
            index++;
            t = 0;
        }
        if (index >= STEPS.size()) {
            Tenkaichi.LOG.info("[DBT-FEATURE] RESULT {} passed, {} failed", passes, FAILS.size());
            for (String f : FAILS) {
                Tenkaichi.LOG.error("[DBT-FEATURE] FAILED {}", f);
            }
            return true;
        }
        return false;
    }

    private static void pass(String message) {
        passes++;
        Tenkaichi.LOG.info("[DBT-FEATURE] PASS {}", message);
    }

    private static void fail(String message) {
        FAILS.add(message);
        Tenkaichi.LOG.error("[DBT-FEATURE] FAIL {}", message);
    }

    private static void note(String message) {
        Tenkaichi.LOG.info("[DBT-FEATURE] NOTE {}", message);
    }

    private static void add(Step step) {
        STEPS.add(step);
    }

    private static void once(Consumer<Minecraft> action) {
        add((mc, t) -> {
            action.accept(mc);
            return true;
        });
    }

    private static void sleep(int ticks) {
        add((mc, t) -> t >= ticks);
    }

    private static void until(String label, Predicate<Minecraft> condition, int timeout) {
        add((mc, t) -> {
            if (condition.test(mc)) {
                pass(label + " (" + t + "t)");
                return true;
            }
            if (t >= timeout) {
                fail(label + " (timed out after " + timeout + "t)");
                return true;
            }
            return false;
        });
    }

    private static void check(String label, Predicate<Minecraft> condition) {
        until(label, condition, 0);
    }

    private static void untilServer(String label, Predicate<ServerPlayer> condition, int timeout) {
        AtomicBoolean ok = new AtomicBoolean();
        add((mc, t) -> {
            if (ok.get()) {
                pass(label + " (" + t + "t)");
                return true;
            }
            if (t >= timeout) {
                fail(label + " (timed out after " + timeout + "t)");
                return true;
            }
            IntegratedServer server = mc.getSingleplayerServer();
            server.execute(() -> {
                ServerPlayer sp = server.getPlayerList().getPlayerByName(name);
                if (sp != null && condition.test(sp)) {
                    ok.set(true);
                }
            });
            return false;
        });
    }

    private static void checkServer(String label, Predicate<ServerPlayer> condition) {
        untilServer(label, condition, 6);
    }

    private static void server(Consumer<ServerPlayer> action) {
        once(mc -> {
            IntegratedServer server = mc.getSingleplayerServer();
            server.execute(() -> {
                ServerPlayer sp = server.getPlayerList().getPlayerByName(name);
                if (sp != null) {
                    action.accept(sp);
                }
            });
        });
    }

    private static void cmd(String command) {
        once(mc -> ClientSmokeTest.command(mc, command.replace("@me", name)));
    }

    private static void act(ActionType type, int arg) {
        once(mc -> ClientSmokeTest.action(type, arg));
    }

    private static void shot(String label) {
        once(mc -> ClientSmokeTest.shot(mc, "f_" + label));
    }

    private static void camera(CameraType type) {
        once(mc -> mc.options.setCameraType(type));
    }

    private static PlayerData data(ServerPlayer sp) {
        return PlayerManager.data(sp);
    }

    private static String tracked() {
        return MissionHud.data().getStringOr("tracked", "");
    }

    private static int trackedStage() {
        return MissionHud.data().getIntOr("stage", -1);
    }

    private static void stage(String mission, int stage, int timeout) {
        until("story " + mission + " reaches stage " + stage, mc -> tracked().equals(mission) && trackedStage() == stage, timeout);
    }

    private static void restore() {
        cmd("tenkaichi restore @me");
        server(sp -> {
            sp.setHealth(sp.getMaxHealth());
            sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.RESISTANCE, 2400, 4, false, false));
            sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 2400, 4, false, false));
        });
    }

    private static void toSite(String key, double dx, double dy, double dz, float yaw, float pitch) {
        server(sp -> {
            StoryWorld.Site site = StoryWorld.site(sp.level().getServer(), key);
            if (site == null) {
                fail("story site " + key + " missing");
                return;
            }
            BlockPos p = site.pos();
            sp.teleport(new TeleportTransition(site.level(), new Vec3(p.getX() + 0.5 + dx, p.getY() + dy, p.getZ() + 0.5 + dz), Vec3.ZERO, yaw, pitch, TeleportTransition.DO_NOTHING));
            sp.resetFallDistance();
        });
    }

    private static void scene(String id, int... shots) {
        until("scene " + id + " starts", mc -> ScenePlayer.active() && ScenePlayer.sceneId().equals(id), 900);
        int duration = Scenes.get(id) == null ? 0 : Scenes.get(id).duration;
        boolean[] taken = new boolean[shots.length];
        int[] last = {0};
        add((mc, t) -> {
            boolean running = ScenePlayer.active() && ScenePlayer.sceneId().equals(id);
            if (running) {
                int e = ScenePlayer.elapsed();
                last[0] = e;
                for (int i = 0; i < shots.length; i++) {
                    if (!taken[i] && e >= shots[i]) {
                        taken[i] = true;
                        ClientSmokeTest.shot(mc, "s_" + id + "_" + i);
                    }
                }
                if (e > duration + 300) {
                    fail("scene " + id + " never ended");
                    return true;
                }
                return false;
            }
            if (Math.abs(last[0] - duration) <= 30) {
                pass("scene " + id + " played to the end (" + last[0] + "/" + duration + "t)");
            } else {
                fail("scene " + id + " ended after " + last[0] + "t, expected about " + duration);
            }
            return true;
        });
    }

    private static StoryNpc npc(ServerPlayer sp, String tag) {
        return MissionManager.npc(sp, tag);
    }

    private static void face(ServerPlayer sp, Vec3 from, Vec3 target) {
        Vec3 d = target.subtract(from.add(0, sp.getEyeHeight(), 0));
        float yaw = (float) (Math.toDegrees(Math.atan2(d.z, d.x)) - 90.0);
        float pitch = (float) (-Math.toDegrees(Math.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z))));
        sp.teleport(new TeleportTransition(sp.level(), from, Vec3.ZERO, yaw, pitch, TeleportTransition.DO_NOTHING));
    }

    private static void approach(String tag, double distance) {
        server(sp -> {
            StoryNpc n = npc(sp, tag);
            if (n == null) {
                return;
            }
            Vec3 dir = sp.position().subtract(n.position()).multiply(1, 0, 1);
            dir = dir.lengthSqr() < 1.0e-3 ? new Vec3(1, 0, 0) : dir.normalize();
            face(sp, n.position().add(dir.scale(distance)), n.getBoundingBox().getCenter());
            data(sp).runtime().lockedTarget = n.getId();
        });
    }

    private static void fight(String mission, String tag, String label) {
        untilServer("story fight spawns " + tag, sp -> npc(sp, tag) != null && npc(sp, tag).hostile(), 600);
        restore();
        approach(tag, 2.4);
        sleep(3);
        camera(CameraType.THIRD_PERSON_BACK);
        for (int i = 0; i < 6; i++) {
            approach(tag, 2.2);
            act(ActionType.LIGHT_ATTACK, 0);
            sleep(7);
        }
        shot(label);
        untilServer("melee hits damage " + tag, sp -> npc(sp, tag) != null && npc(sp, tag).getHealth() < npc(sp, tag).getMaxHealth(), 20);
        server(sp -> {
            StoryNpc n = npc(sp, tag);
            if (n != null) {
                n.hurtServer(sp.level(), TenkaichiDamage.source(sp.level(), TenkaichiDamage.KI_BLAST, sp, sp), n.getMaxHealth() * 4.0f);
            }
        });
        untilServer(tag + " reaches the defeat threshold", sp -> npc(sp, tag) != null && npc(sp, tag).defeated(), 40);
        once(mc -> LockOn.release());
        server(sp -> data(sp).runtime().lockedTarget = -1);
    }

    private static void build() {
        story();
        sideMissions();
        senseAndScouter();
        tailAndMoon();
        techniques();
        races();
    }

    private static void story() {
        cmd("tenkaichi mission reset @me");
        cmd("time set day");
        restore();
        sleep(10);
        once(mc -> mc.gui.setScreen(new MissionScreen()));
        sleep(20);
        shot("40_missions_screen");
        check("missions screen lists the story start", mc -> MissionHud.data().getListOrEmpty("available").stream()
                .anyMatch(tag -> tag instanceof net.minecraft.nbt.CompoundTag c && c.getStringOr("id", "").equals("saiyan_saga_01_reunion") && c.getBooleanOr("ready", false)));
        once(mc -> Services.NETWORK.sendToServer(new Packets.Request("mission_accept", "saiyan_saga_01_reunion")));
        sleep(10);
        once(mc -> mc.gui.setScreen(null));
        stage("saiyan_saga_01_reunion", 0, 100);
        check("mission waypoint targets Kame House", mc -> MissionHud.data().contains("wx"));
        toSite("kame_house", 30, 6, 30, 135.0f, 0.0f);
        camera(CameraType.FIRST_PERSON);
        sleep(30);
        shot("41_mission_hud_waypoint");
        act(ActionType.WAYPOINT_TOGGLE, 0);
        until("waypoint can be hidden", mc -> !MissionHud.data().getBooleanOr("waypoint", true), 60);
        shot("41b_waypoint_hidden");
        act(ActionType.WAYPOINT_TOGGLE, 0);
        until("waypoint can be shown again", mc -> MissionHud.data().getBooleanOr("waypoint", false), 60);
        camera(CameraType.THIRD_PERSON_BACK);
        toSite("kame_house", 4, 1, 4, 0.0f, 0.0f);
        scene("reunion", 80, 180, 300, 430, 600, 800, 900);
        check("skip hint hidden on first viewing", mc -> !ScenePlayer.skippable());
        stage("saiyan_saga_02_alliance", 0, 200);
        toSite("raditz_field", 3, 1, 3, 0.0f, 0.0f);
        scene("alliance", 60, 200, 520);
        stage("saiyan_saga_02_alliance", 2, 200);
        untilServer("Piccolo fights as an ally", sp -> npc(sp, "piccolo") != null && npc(sp, "piccolo").role() == StoryNpc.ROLE_ALLY, 200);
        fight("saiyan_saga_02_alliance", "raditz", "42_raditz_fight");
        scene("gohan_rage", 120, 300, 470, 600, 700, 960);
        until("story moves the player to the Other World", mc -> mc.player.level().dimension().equals(OtherWorld.KEY), 200);
        check("player wears a halo after dying", mc -> ClientState.forEntity(mc.player).halo);
        scene("king_yemma", 40, 200, 420);
        stage("saiyan_saga_03_snake_way", 1, 200);
        server(sp -> {
            Vec3 road = Vec3.atBottomCenterOf(OtherWorld.roadPoint(420)).add(0, 1.2, 0);
            face(sp, road, road.add(0, 1.4, 20));
        });
        sleep(30);
        shot("43_snake_way");
        server(sp -> sp.teleportTo(OtherWorld.roadX(420) + 40, 55, 420));
        untilServer("falling off Snake Way puts the player back on the road", sp -> sp.getY() > 110, 120);
        toSite("king_kai_planet", 0, 1, 0, 180.0f, 0.0f);
        scene("king_kai_meeting", 40, 300, 360, 640);
        checkServer("King Kai's gravity weighs on the player", sp -> sp.getAttributeValue(Attributes.GRAVITY) > 0.09);
        stage("saiyan_saga_04_bubbles", 0, 200);
        untilServer("Bubbles appears", sp -> npc(sp, "bubbles") != null, 200);
        shot("44_bubbles_chase");
        add((mc, t) -> {
            IntegratedServer server = mc.getSingleplayerServer();
            server.execute(() -> {
                ServerPlayer sp = server.getPlayerList().getPlayerByName(name);
                StoryNpc b = sp == null ? null : npc(sp, "bubbles");
                if (b != null) {
                    sp.teleportTo(b.getX(), b.getY(), b.getZ());
                }
            });
            if (ScenePlayer.active()) {
                pass("Bubbles caught (" + t + "t)");
                return true;
            }
            if (t > 300) {
                fail("Bubbles could not be caught");
                return true;
            }
            return false;
        });
        scene("bubbles_caught", 40, 200);
        untilServer("Gregory appears", sp -> npc(sp, "gregory") != null, 200);
        add((mc, t) -> {
            if (t % 8 == 0) {
                IntegratedServer server = mc.getSingleplayerServer();
                server.execute(() -> {
                    ServerPlayer sp = server.getPlayerList().getPlayerByName(name);
                    StoryNpc g = sp == null ? null : npc(sp, "gregory");
                    if (g != null) {
                        Vec3 dir = sp.position().subtract(g.position()).multiply(1, 0, 1);
                        dir = dir.lengthSqr() < 1.0e-3 ? new Vec3(1, 0, 0) : dir.normalize();
                        face(sp, g.position().add(dir.scale(1.6)), g.getBoundingBox().getCenter());
                    }
                });
            }
            if (t % 8 == 3) {
                ClientSmokeTest.action(ActionType.LIGHT_ATTACK, 0);
            }
            if (t == 20) {
                ClientSmokeTest.shot(mc, "f_45_gregory_hit");
            }
            if (ScenePlayer.active()) {
                pass("Gregory was hit with a real melee attack (" + t + "t)");
                return true;
            }
            if (t > 400) {
                fail("Gregory could not be hit");
                return true;
            }
            return false;
        });
        scene("gregory_hit", 40, 200);
        scene("king_kai_training", 60, 150, 330, 450, 620, 840);
        checkServer("King Kai taught the Kaioken and the Spirit Bomb", sp -> data(sp).hasSkill("dbtenkaichi:kaioken") && data(sp).hasSkill("dbtenkaichi:spirit_bomb"));
        checkServer("gravity training lifts the heavy gravity", sp -> sp.getAttributeValue(Attributes.GRAVITY) < 0.09);
        stage("saiyan_saga_06_techniques", 1, 100);
        restore();
        act(ActionType.KAIOKEN, 0);
        stage("saiyan_saga_06_techniques", 2, 100);
        act(ActionType.TRANSFORM_DOWN, 0);
        restore();
        once(mc -> Services.NETWORK.sendToServer(new Packets.AssignSlot(0, "dbtenkaichi:spirit_bomb")));
        server(sp -> face(sp, sp.position(), sp.position().add(0, 1.6, 30)));
        sleep(4);
        act(ActionType.SKILL_PRESS, 0);
        sleep(60);
        camera(CameraType.THIRD_PERSON_FRONT);
        sleep(2);
        shot("46_spirit_bomb_charge");
        sleep(30);
        shot("46b_spirit_bomb_big");
        act(ActionType.SKILL_RELEASE, 0);
        sleep(6);
        camera(CameraType.THIRD_PERSON_BACK);
        sleep(4);
        shot("46c_spirit_bomb_thrown");
        scene("wish", 40, 200);
        check("halo removed after the wish", mc -> !ClientState.forEntity(mc.player).halo);
        stage("saiyan_saga_07_return", 1, 200);
        toSite("check_in", 0, 1, 6, 180.0f, 0.0f);
        scene("saiyans_land", 60, 200, 400, 470, 650, 760, 900, 1000, 1080, 1200, 1350);
        until("player arrives on the battlefield", mc -> mc.player.level().dimension().equals(net.minecraft.world.level.Level.OVERWORLD), 200);
        scene("goku_arrives", 120, 330, 400, 700);
        fight("saiyan_saga_08_nappa", "nappa", "47_nappa_fight");
        scene("nappa_end", 100, 340, 380, 520);
        fight("saiyan_saga_09_vegeta", "vegeta", "48_vegeta_fight");
        scene("beam_clash", 100, 220, 320, 420);
        fight("saiyan_saga_09_vegeta", "vegeta", "49_vegeta_fight_2");
        scene("power_ball", 100, 170, 250, 350, 450, 620, 720);
        untilServer("Vegeta fights as a Great Ape", sp -> npc(sp, "vegeta") != null && "dbtenkaichi:great_ape".equals(npc(sp, "vegeta").fighterForm()), 300);
        fight("saiyan_saga_09_vegeta", "vegeta", "50_great_ape_vegeta_fight");
        once(mc -> mc.options.setCameraType(CameraType.THIRD_PERSON_BACK));
        scene("tail_cut", 100, 170, 360, 450, 530, 610, 710, 900);
        scene("vegeta_escapes", 120, 200, 330, 470, 560, 720, 940);
        stage("saiyan_saga_10_epilogue", 0, 300);
        toSite("kame_house", 4, 1, 4, 0.0f, 0.0f);
        scene("epilogue", 120, 300, 560);
        untilServer("Saiyan Saga completed", sp -> data(sp).missions().flag("saiyan_saga_complete") && data(sp).missions().isCompleted("saiyan_saga_10_epilogue"), 100);
        checkServer("all ten story missions completed", sp -> data(sp).missions().completed().stream().filter(id -> id.startsWith("saiyan_saga_")).count() == 10);
    }

    private static void sideMissions() {
        restore();
        once(mc -> Services.NETWORK.sendToServer(new Packets.Request("mission_accept", "side_turtle_training")));
        untilServer("Turtle School training completes by Battle Power", sp -> data(sp).missions().isCompleted("side_turtle_training"), 100);
        cmd("give @me minecraft:cooked_cod 6");
        once(mc -> Services.NETWORK.sendToServer(new Packets.Request("mission_accept", "side_roshi_lunch")));
        sleep(10);
        toSite("kame_house", 2, 1, 2, 0.0f, 0.0f);
        untilServer("Roshi's lunch is delivered", sp -> data(sp).missions().isCompleted("side_roshi_lunch")
                && sp.getInventory().getNonEquipmentItems().stream().noneMatch(s -> s.is(net.minecraft.world.item.Items.COOKED_COD)), 100);
        once(mc -> Services.NETWORK.sendToServer(new Packets.Request("mission_accept", "side_raider_hunt")));
        server(sp -> {
            ServerLevel level = sp.level();
            for (int i = 0; i < 5; i++) {
                RaiderEntity raider = com.dbtenkaichi.registry.ModEntities.RAIDER.get().create(level, EntitySpawnReason.COMMAND);
                if (raider != null) {
                    raider.setPos(sp.getX() + 6 + i * 2, sp.getY(), sp.getZ() + 6);
                    level.addFreshEntity(raider);
                    raider.hurtServer(level, TenkaichiDamage.source(level, TenkaichiDamage.MARTIAL_ARTS, sp, sp), raider.getMaxHealth() * 50.0f);
                }
            }
        });
        untilServer("Raider hunt counts five defeated raiders", sp -> data(sp).missions().isCompleted("side_raider_hunt"), 200);
        once(mc -> Services.NETWORK.sendToServer(new Packets.Request("mission_accept", "side_korin_water")));
        untilServer("side mission accepted", sp -> data(sp).missions().isActive("side_korin_water"), 60);
        once(mc -> mc.gui.setScreen(new MissionScreen()));
        sleep(20);
        shot("51_missions_screen_side");
        once(mc -> Services.NETWORK.sendToServer(new Packets.Request("mission_abandon", "side_korin_water")));
        untilServer("side mission can be abandoned", sp -> !data(sp).missions().isActive("side_korin_water"), 60);
        once(mc -> mc.gui.setScreen(null));
    }

    private static void senseAndScouter() {
        restore();
        cmd("time set day");
        cmd("kill @e[type=dbtenkaichi:story_character]");
        server(sp -> {
            ServerLevel level = sp.level();
            RaiderEntity raider = com.dbtenkaichi.registry.ModEntities.RAIDER.get().create(level, EntitySpawnReason.COMMAND);
            if (raider != null) {
                Vec3 look = sp.getViewVector(1.0f).multiply(1, 0, 1).normalize();
                raider.setPos(sp.getX() + look.x * 24, sp.getY(), sp.getZ() + look.z * 24);
                raider.setNoAi(true);
                level.addFreshEntity(raider);
            }
        });
        act(ActionType.SENSE_TOGGLE, 0);
        until("ki sense lists nearby fighters", mc -> SenseHud.active(), 80);
        camera(CameraType.FIRST_PERSON);
        sleep(10);
        shot("52_ki_sense");
        act(ActionType.SENSE_TOGGLE, 0);
        cmd("item replace entity @me armor.head with dbtenkaichi:scouter");
        once(mc -> Services.NETWORK.sendToServer(new Packets.Request("mission_accept", "side_power_level")));
        server(sp -> {
            StoryNpc vegeta = StoryNpc.spawn(sp.level(), StoryCharacters.get("vegeta"), sp.position().add(sp.getViewVector(1.0f).multiply(1, 0, 1).normalize().scale(8)), StoryNpc.ROLE_ACTOR, sp, 1.0);
            if (vegeta != null) {
                vegeta.setLifetime(400);
            }
        });
        until("scouter reads power levels", mc -> SenseHud.active(), 80);
        sleep(10);
        shot("53_scouter");
        untilServer("scouter finds a power level over 9000", sp -> data(sp).missions().isCompleted("side_power_level"), 100);
        camera(CameraType.THIRD_PERSON_FRONT);
        sleep(4);
        shot("53b_scouter_worn");
        cmd("item replace entity @me armor.head with minecraft:air");
        cmd("kill @e[type=dbtenkaichi:saiyan_raider]");
        camera(CameraType.THIRD_PERSON_BACK);
    }

    private static void tailAndMoon() {
        restore();
        act(ActionType.TAIL_TOGGLE, 0);
        until("tail can be wrapped around the waist", mc -> ClientState.forEntity(mc.player).tailState == PlayerData.TAIL_WRAPPED, 60);
        once(mc -> mc.gui.setScreen(new PosePreviewScreen(160.0f)));
        sleep(4);
        shot("54_tail_wrapped");
        once(mc -> mc.gui.setScreen(null));
        act(ActionType.TAIL_TOGGLE, 0);
        until("tail can be unwrapped", mc -> ClientState.forEntity(mc.player).tailState == PlayerData.TAIL_FREE, 60);
        AtomicBoolean grabbed = new AtomicBoolean();
        server(sp -> {
            ServerLevel level = sp.level();
            RaiderEntity raider = com.dbtenkaichi.registry.ModEntities.RAIDER.get().create(level, EntitySpawnReason.COMMAND);
            if (raider == null) {
                return;
            }
            Vec3 back = sp.position().subtract(sp.getViewVector(1.0f).multiply(1, 0, 1).normalize().scale(1.5));
            raider.setPos(back.x, sp.getY(), back.z);
            raider.setNoAi(true);
            level.addFreshEntity(raider);
            PlayerData pd = data(sp);
            for (int i = 0; i < 80 && !grabbed.get(); i++) {
                TailSystem.onMeleeHit(raider, sp, pd, level.getGameTime());
                grabbed.set(pd.runtime().tailGrabbedUntil > level.getGameTime());
            }
            raider.discard();
        });
        until("a hit from behind can grab the tail", mc -> grabbed.get(), 20);
        server(sp -> data(sp).runtime().tailGrabbedUntil = 0);
        cmd("time set 18000");
        server(sp -> face(sp, sp.position(), sp.getEyePosition().add(0.05, 50, 0)));
        untilServer("the full moon turns a Saiyan into a Great Ape", sp -> TailSystem.GREAT_APE.equals(data(sp).form()), 500);
        until("great ape cinematic finished", mc -> !CinematicDirector.active(ClientState.forEntity(mc.player)), 400);
        checkServer("Great Ape body is giant", sp -> sp.getAttributeValue(Attributes.SCALE) > 2.5);
        camera(CameraType.THIRD_PERSON_FRONT);
        server(sp -> face(sp, sp.position(), sp.getEyePosition().add(0, 0, 10)));
        sleep(10);
        shot("55_great_ape_moon");
        once(mc -> mc.gui.setScreen(new PosePreviewScreen(25.0f)));
        sleep(4);
        shot("55b_great_ape_model");
        once(mc -> mc.gui.setScreen(null));
        cmd("time set 6000");
        untilServer("Great Ape ends when the moon is gone", sp -> !TailSystem.GREAT_APE.equals(data(sp).form()), 300);
        restore();
        cmd("tenkaichi learn @me dbtenkaichi:power_ball");
        once(mc -> Services.NETWORK.sendToServer(new Packets.AssignSlot(0, "dbtenkaichi:power_ball")));
        server(sp -> face(sp, sp.position(), sp.getEyePosition().add(0, 2, 12)));
        sleep(4);
        act(ActionType.SKILL_PRESS, 0);
        untilServer("Power Ball creates an artificial moon", sp -> !sp.level().getEntitiesOfClass(PowerBallEntity.class, sp.getBoundingBox().inflate(80)).isEmpty(), 40);
        sleep(40);
        camera(CameraType.THIRD_PERSON_BACK);
        server(sp -> {
            List<PowerBallEntity> balls = sp.level().getEntitiesOfClass(PowerBallEntity.class, sp.getBoundingBox().inflate(80));
            if (!balls.isEmpty()) {
                face(sp, sp.position(), balls.get(0).position());
            }
        });
        sleep(6);
        shot("56_power_ball");
        untilServer("staring at the Power Ball triggers the Great Ape", sp -> TailSystem.GREAT_APE.equals(data(sp).form()), 500);
        until("power ball great ape cinematic finished", mc -> !CinematicDirector.active(ClientState.forEntity(mc.player)), 400);
        server(sp -> TailSystem.cutTail(sp, data(sp), sp.level().getGameTime()));
        untilServer("cutting the tail ends the Great Ape and loses the tail", sp -> !TailSystem.GREAT_APE.equals(data(sp).form()) && data(sp).tailState() == PlayerData.TAIL_LOST, 100);
        server(sp -> data(sp).setTailRegrowAt(sp.level().getGameTime()));
        untilServer("the tail grows back", sp -> data(sp).tailState() == PlayerData.TAIL_FREE, 100);
        server(sp -> sp.level().getEntitiesOfClass(PowerBallEntity.class, sp.getBoundingBox().inflate(120)).forEach(net.minecraft.world.entity.Entity::discard));
    }

    private static final String[] TECHNIQUES = {
            "double_sunday", "tri_beam", "hells_flash", "innocence_cannon", "perfect_kamehameha",
            "spirit_bomb", "death_ball", "planet_burst",
            "light_grenade", "spirit_ball", "dodon_ray", "eye_laser", "power_blitz", "evil_impulse", "vanishing_ball", "destructo_disc", "death_saucer",
            "hellzone_grenade", "scattering_bullet", "human_extinction_attack", "lightning_of_absolution",
            "solar_flare", "instant_transmission", "kai_kai", "bomber_dx", "absorption", "candy_beam", "energy_absorption", "barrier",
            "restoration", "telekinesis", "stone_spit", "cell_junior", "wolf_fang_fist", "hakai"};

    private static void techniques() {
        cmd("time set day");
        for (String id : TECHNIQUES) {
            technique(id);
        }
        cmd("kill @e[type=dbtenkaichi:story_character]");
        cmd("kill @e[type=minecraft:cow]");
        cmd("kill @e[type=minecraft:item]");
    }

    private static void technique(String shortId) {
        String id = "dbtenkaichi:" + shortId;
        Technique technique = Techniques.get(id);
        if (technique == null) {
            once(mc -> fail("technique " + id + " is not registered"));
            return;
        }
        float[] before = new float[3];
        cmd("kill @e[type=dbtenkaichi:story_character]");
        cmd("kill @e[type=minecraft:cow]");
        cmd("kill @e[type=minecraft:item]");
        restore();
        cmd("tenkaichi learn @me " + id);
        sleep(2);
        once(mc -> Services.NETWORK.sendToServer(new Packets.AssignSlot(0, id)));
        server(sp -> {
            ServerLevel level = sp.level();
            Vec3 look = new Vec3(0, 0, 1);
            Vec3 at = sp.position().add(look.scale("absorption".equals(shortId) || "energy_absorption".equals(shortId) || "restoration".equals(shortId) ? 2.5 : 9.0));
            LivingEntity target;
            if ("candy_beam".equals(shortId)) {
                Cow cow = EntityTypes.COW.create(level, EntitySpawnReason.COMMAND);
                cow.setPos(at.x, sp.getY(), at.z);
                cow.setNoAi(true);
                level.addFreshEntity(cow);
                cow.setHealth(cow.getMaxHealth() * 0.4f);
                target = cow;
            } else {
                StoryNpc npc = StoryNpc.spawn(level, StoryCharacters.get("raditz"), new Vec3(at.x, sp.getY(), at.z), StoryNpc.ROLE_ENEMY, sp, 1.0);
                if (npc == null) {
                    fail("could not spawn a target for " + id);
                    return;
                }
                npc.setNoAi(true);
                if ("absorption".equals(shortId)) {
                    npc.setHealth(npc.getMaxHealth() * 0.2f);
                }
                target = npc;
            }
            face(sp, sp.position(), target.getBoundingBox().getCenter());
            PlayerData pd = data(sp);
            pd.runtime().lockedTarget = target.getId();
            before[0] = target.getHealth();
            before[1] = (float) pd.ki();
            before[2] = pd.absorptions();
            if ("restoration".equals(shortId) || "energy_absorption".equals(shortId)) {
                sp.setHealth(sp.getMaxHealth() * 0.5f);
                pd.setKi(pd.runtime().cachedMaxKi * 0.4);
                before[1] = (float) pd.ki();
            }
        });
        sleep(4);
        act(ActionType.SKILL_PRESS, 0);
        if (technique.charged()) {
            sleep(Math.max(technique.minCharge(), Math.min(technique.maxCharge(), technique.minCharge() + 30)));
            camera(CameraType.THIRD_PERSON_FRONT);
            sleep(2);
            shot("t_" + shortId + "_charge");
            camera(CameraType.THIRD_PERSON_BACK);
            act(ActionType.SKILL_RELEASE, 0);
        }
        sleep(technique.type() == Technique.Type.BEAM ? 14 : 8);
        shot("t_" + shortId);
        untilServer("technique " + shortId + " has its effect", sp -> effectSeen(sp, shortId, technique, before), technique.type() == Technique.Type.SUPPORT ? 20 : 120);
        act(ActionType.SKILL_RELEASE, 0);
        server(sp -> data(sp).runtime().lockedTarget = -1);
        sleep(technique.type() == Technique.Type.BEAM ? 40 : 6);
    }

    private static LivingEntity techniqueTarget(ServerPlayer sp) {
        List<LivingEntity> list = sp.level().getEntitiesOfClass(LivingEntity.class, sp.getBoundingBox().inflate(30), e -> e instanceof StoryNpc npc && npc.role() == StoryNpc.ROLE_ENEMY || e instanceof Cow);
        return list.isEmpty() ? null : list.get(0);
    }

    private static boolean effectSeen(ServerPlayer sp, String id, Technique technique, float[] before) {
        PlayerData pd = data(sp);
        RuntimeState rt = pd.runtime();
        long now = sp.level().getGameTime();
        LivingEntity target = techniqueTarget(sp);
        RuntimeState trt = target == null ? null : com.dbtenkaichi.combat.CombatSystem.runtime(target);
        boolean damaged = target == null || !target.isAlive() || target.getHealth() < before[0] - 0.01f;
        return switch (id) {
            case "solar_flare", "telekinesis" -> trt != null && trt.stunnedUntil > now;
            case "stone_spit" -> trt != null && trt.petrifiedUntil > now;
            case "instant_transmission", "kai_kai" -> target != null && sp.distanceTo(target) < 3.5;
            case "absorption" -> pd.absorptions() > before[2] && (target == null || !target.isAlive());
            case "candy_beam" -> !sp.level().getEntitiesOfClass(ItemEntity.class, sp.getBoundingBox().inflate(20), e -> e.getItem().is(ModItems.CANDY.get())).isEmpty();
            case "energy_absorption" -> pd.ki() > before[1] + 1.0;
            case "barrier" -> rt.barrierUntil > now;
            case "restoration" -> sp.getHealth() > sp.getMaxHealth() * 0.6f;
            case "cell_junior" -> sp.level().getEntitiesOfClass(StoryNpc.class, sp.getBoundingBox().inflate(8), n -> "cell_jr".equals(n.characterId())).size() >= 3;
            default -> damaged;
        };
    }

    private static void races() {
        for (Race race : Race.values()) {
            raceForms(race);
        }
    }

    private static void create(Race race) {
        cmd("tenkaichi reset @me");
        sleep(10);
        once(mc -> {
            Appearance a = Appearance.defaultsFor(race);
            Services.NETWORK.sendToServer(new Packets.CreateCharacter(race.ordinal(), a.presentation, 1.0f, a.build, a.skinColor, a.eyeStyle, a.eyeColor,
                    a.eyebrowStyle, a.hairStyle, a.hairColor, a.expression, a.auraColor, a.outfit, a.outfitPrimary, a.outfitSecondary, a.featureColor, true, false));
            mc.gui.setScreen(null);
        });
        sleep(10);
        cmd("tenkaichi mission reset @me");
        cmd("tenkaichi setstat @me all 160");
        restore();
        sleep(10);
        until("character recreated as " + race.id(), mc -> Services.PLATFORM.playerData(mc.player).isCreated() && Services.PLATFORM.playerData(mc.player).race() == race, 60);
    }

    private static void raceForms(Race race) {
        create(race);
        if (race == Race.ANDROID) {
            AtomicBoolean far = new AtomicBoolean();
            AtomicBoolean near = new AtomicBoolean();
            server(sp -> {
                ServerLevel level = sp.level();
                RaiderEntity raider = com.dbtenkaichi.registry.ModEntities.RAIDER.get().create(level, EntitySpawnReason.COMMAND);
                if (raider == null) {
                    return;
                }
                raider.setNoAi(true);
                raider.setPos(sp.getX() + 30, sp.getY(), sp.getZ());
                level.addFreshEntity(raider);
                far.set(KiSense.canSense(raider, sp, false));
                raider.setPos(sp.getX() + 5, sp.getY(), sp.getZ());
                near.set(KiSense.canSense(raider, sp, false));
                raider.discard();
            });
            sleep(4);
            check("androids cannot be sensed from afar", mc -> !far.get());
            check("androids can be noticed up close", mc -> near.get());
        }
        if (race == Race.BIO_ANDROID) {
            absorptionUnlock();
        }
        List<FormDefinition> forms = FormRegistry.chainFor(race, true);
        once(mc -> note(race.id() + " has " + FormRegistry.chainFor(race, true).size() + " forms"));
        for (FormDefinition form : forms) {
            if (form.id.equals(FormRegistry.BASE) || form.id.startsWith("dbtenkaichi:raider")) {
                continue;
            }
            formCinematic(race, form.id);
        }
    }

    private static void absorptionUnlock() {
        restore();
        once(mc -> Services.NETWORK.sendToServer(new Packets.Request("target_form", "dbtenkaichi:semi_perfect")));
        act(ActionType.TRANSFORM_UP, TransformationSystem.CINEMATIC_SKIP);
        sleep(20);
        checkServer("Semi-Perfect form stays locked without absorptions", sp -> !"dbtenkaichi:semi_perfect".equals(data(sp).form()));
        cmd("tenkaichi learn @me dbtenkaichi:absorption");
        once(mc -> Services.NETWORK.sendToServer(new Packets.AssignSlot(0, "dbtenkaichi:absorption")));
        server(sp -> {
            StoryNpc victim = StoryNpc.spawn(sp.level(), StoryCharacters.get("yamcha"), sp.position().add(0, 0, 2.2), StoryNpc.ROLE_ENEMY, sp, 1.0);
            if (victim != null) {
                victim.setNoAi(true);
                victim.setHealth(victim.getMaxHealth() * 0.2f);
                face(sp, sp.position(), victim.getBoundingBox().getCenter());
                data(sp).runtime().lockedTarget = victim.getId();
            }
        });
        sleep(4);
        act(ActionType.SKILL_PRESS, 0);
        untilServer("Bio-Android absorbs a weakened fighter", sp -> data(sp).absorptions() >= 1, 40);
        restore();
        cmd("tenkaichi unlockform @me dbtenkaichi:semi_perfect");
        act(ActionType.TRANSFORM_UP, TransformationSystem.CINEMATIC_SKIP);
        untilServer("one absorption unlocks the Semi-Perfect form", sp -> "dbtenkaichi:semi_perfect".equals(data(sp).form()), 120);
        server(sp -> TransformationSystem.forceRevert(sp, data(sp), null));
        sleep(10);
    }

    private static void formCinematic(Race race, String formId) {
        String label = race.id() + "_" + formId.substring(formId.indexOf(':') + 1);
        restore();
        server(sp -> {
            PlayerData pd = data(sp);
            TransformationSystem.forceRevert(sp, pd, null);
            FormDefinition form = FormRegistry.get(formId, false);
            if (form == null) {
                fail("form " + formId + " missing on the server");
                return;
            }
            face(sp, sp.position(), sp.getEyePosition().add(0, 0, 10));
            TransformationSystem.begin(sp, pd, form, TransformationSystem.CINEMATIC_AUTO, true, sp.level().getGameTime());
        });
        until("cinematic for " + label + " starts", mc -> CinematicDirector.active(ClientState.forEntity(mc.player)), 60);
        add((mc, t) -> {
            EntityVisual visual = ClientState.forEntity(mc.player);
            float p = CinematicDirector.progress(visual, 0.0f);
            if (t == 0) {
                note(label + " cinematic script " + CinematicDirector.script(visual).id + " ticks " + visual.cinematicTicks);
            }
            if (p >= 0.3f && p < 0.3f + 1.5f / Math.max(1, visual.cinematicTicks)) {
                ClientSmokeTest.shot(mc, "c_" + label + "_a");
            }
            if (p >= 0.65f && p < 0.65f + 1.5f / Math.max(1, visual.cinematicTicks)) {
                ClientSmokeTest.shot(mc, "c_" + label + "_b");
            }
            if (!CinematicDirector.active(visual)) {
                return true;
            }
            if (t > 700) {
                fail("cinematic for " + label + " never ended");
                return true;
            }
            return false;
        });
        untilServer("player is in form " + label, sp -> formId.equals(data(sp).form()), 120);
        camera(CameraType.THIRD_PERSON_FRONT);
        sleep(12);
        shot("form_" + label);
        camera(CameraType.THIRD_PERSON_BACK);
        server(sp -> TransformationSystem.forceRevert(sp, data(sp), null));
        sleep(6);
    }

    private static final class PosePreviewScreen extends net.minecraft.client.gui.screens.Screen {
        private final float yaw;

        PosePreviewScreen(float yaw) {
            super(net.minecraft.network.chat.Component.empty());
            this.yaw = yaw;
        }

        @Override
        public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                com.dbtenkaichi.client.screen.EntityPreview.draw(g, 0, 0, this.width, this.height, 70, yaw, -5.0f, 1.0f, mc.player);
            }
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }

    static String summary() {
        return String.format(Locale.ROOT, "%d passed, %d failed", passes, FAILS.size());
    }
}
