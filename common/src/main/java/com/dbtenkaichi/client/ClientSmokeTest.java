package com.dbtenkaichi.client;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.client.input.LockOn;
import com.dbtenkaichi.client.screen.CharacterCreatorScreen;
import com.dbtenkaichi.client.screen.CharacterScreen;
import com.dbtenkaichi.form.TransformationSystem;
import com.dbtenkaichi.network.ActionType;
import com.dbtenkaichi.network.Packets;
import com.dbtenkaichi.platform.Services;
import com.dbtenkaichi.player.Appearance;
import com.dbtenkaichi.player.HairStyle;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.race.Race;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class ClientSmokeTest {
    private static final boolean ENABLED = Boolean.getBoolean("dbtenkaichi.clientSmoke");
    private static final List<String> LOG = new ArrayList<>();
    private static int tick = -1;
    private static boolean finished;

    private ClientSmokeTest() {
    }

    public static boolean enabled() {
        return ENABLED;
    }

    public static void tick(Minecraft mc) {
        if (!ENABLED || finished || mc.player == null || mc.level == null) {
            return;
        }
        tick++;
        try {
            step(mc);
        } catch (RuntimeException e) {
            Tenkaichi.LOG.error("[DBT-CLIENT] step {} failed", tick, e);
            LOG.add("FAIL step " + tick + ": " + e);
        }
    }

    private static void action(ActionType type, int arg) {
        Services.NETWORK.sendToServer(new Packets.Action(type.ordinal(), arg));
    }

    private static void command(Minecraft mc, String cmd) {
        IntegratedServer server = mc.getSingleplayerServer();
        if (server != null) {
            server.execute(() -> server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), cmd));
        }
    }

    private static void shot(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, "dbt_" + name + ".png", mc.gameRenderer.mainRenderTarget(), 1, message -> Tenkaichi.LOG.info("[DBT-CLIENT] screenshot {}: {}", name, message.getString()));
        LOG.add("shot " + name);
    }

    private static int kamehamehaSlot(PlayerData data) {
        for (int i = 0; i < PlayerData.SKILL_SLOTS; i++) {
            if ("dbtenkaichi:kamehameha".equals(data.skillInSlot(i))) {
                return i;
            }
        }
        return -1;
    }

    private static void step(Minecraft mc) {
        String name = mc.player.getName().getString();
        PlayerData data = Services.PLATFORM.playerData(mc.player);
        switch (tick) {
            case 2 -> {
                command(mc, "kill @e[type=dbtenkaichi:saiyan_raider]");
                command(mc, "kill @e[type=minecraft:husk]");
                command(mc, "difficulty peaceful");
            }
            case 30 -> command(mc, "difficulty normal");
            case 40 -> {
                boolean creator = mc.gui.screen() instanceof CharacterCreatorScreen;
                LOG.add("creator opened on first join: " + creator);
                Tenkaichi.LOG.info("[DBT-CLIENT] creator opened on first join: {}", creator);
                shot(mc, "01_creator");
            }
            case 60 -> {
                Appearance a = Appearance.defaultsFor(Race.SAIYAN);
                a.hairStyle = HairStyle.WILD_SPIKES.ordinal();
                a.auraColor = 0xE8F4FF;
                a.outfit = 0;
                Services.NETWORK.sendToServer(new Packets.CreateCharacter(Race.SAIYAN.ordinal(), a.presentation, 1.0f, a.build, a.skinColor, a.eyeStyle, a.eyeColor,
                        a.eyebrowStyle, a.hairStyle, a.hairColor, a.expression, a.voice, a.auraColor, a.outfit, a.outfitPrimary, a.outfitSecondary, true, false));
                mc.gui.setScreen(null);
                command(mc, "time set day");
                command(mc, "weather clear");
                command(mc, "gamerule advance_time false");
                command(mc, "kill @e[type=dbtenkaichi:saiyan_raider]");
                command(mc, "kill @e[type=minecraft:husk]");
            }
            case 80 -> {
                for (String stat : new String[]{"strength", "defense", "constitution", "ki_control", "ki_capacity", "ki_output", "speed", "agility", "stamina", "martial_arts", "spirit", "potential"}) {
                    command(mc, "tenkaichi setstat " + name + " " + stat + " 30");
                }
                command(mc, "tenkaichi learn " + name + " dbtenkaichi:kamehameha");
                command(mc, "tenkaichi unlockform " + name + " dbtenkaichi:super_saiyan");
                command(mc, "tenkaichi restore " + name);
                mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            }
            case 110 -> {
                LOG.add("character created client-side: " + data.isCreated() + " race=" + data.race().id());
                Tenkaichi.LOG.info("[DBT-CLIENT] character created: {} race {} skills {}", data.isCreated(), data.race().id(), data.skills());
                shot(mc, "02_character_hud");
            }
            case 120 -> action(ActionType.CHARGE_START, 0);
            case 150 -> shot(mc, "03_ki_charge_aura");
            case 160 -> action(ActionType.CHARGE_END, 0);
            case 170 -> action(ActionType.FLIGHT_TOGGLE, 0);
            case 175, 180, 185, 190 -> mc.player.setDeltaMovement(new Vec3(0, 0.5, 0));
            case 200 -> {
                LOG.add("flying after toggle: " + data.runtime().flying);
                Tenkaichi.LOG.info("[DBT-CLIENT] flying: {}", data.runtime().flying);
                shot(mc, "04_flight");
            }
            case 220 -> action(ActionType.TRANSFORM_UP, TransformationSystem.CINEMATIC_AUTO);
            case 250 -> shot(mc, "05_transformation_cinematic");
            case 320 -> {
                LOG.add("form after transformation: " + data.form());
                Tenkaichi.LOG.info("[DBT-CLIENT] form: {}", data.form());
                shot(mc, "06_super_saiyan");
            }
            case 330 -> {
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                Vec3 look = mc.player.getLookAngle().multiply(1, 0, 1).normalize();
                Vec3 at = mc.player.position().add(look.scale(30));
                command(mc, String.format(java.util.Locale.ROOT, "summon dbtenkaichi:saiyan_raider %.1f %.1f %.1f {NoAI:1b,NoGravity:1b}", at.x, mc.player.getY(), at.z));
                Vec3 side = mc.player.position().add(look.cross(new Vec3(0, 1, 0)).scale(5));
                command(mc, String.format(java.util.Locale.ROOT, "summon dbtenkaichi:master %.1f %.1f %.1f {NoGravity:1b}", side.x, mc.player.getY(), side.z));
            }
            case 360 -> shot(mc, "07_npcs");
            case 370 -> {
                LockOn.toggle();
                LOG.add("locked target: " + LockOn.target());
            }
            case 380 -> {
                int slot = kamehamehaSlot(data);
                LOG.add("kamehameha slot: " + slot);
                if (slot >= 0) {
                    action(ActionType.SKILL_PRESS, slot);
                }
            }
            case 420 -> shot(mc, "08_kamehameha_charge");
            case 430 -> {
                int slot = kamehamehaSlot(data);
                if (slot >= 0) {
                    action(ActionType.SKILL_RELEASE, slot);
                }
            }
            case 431 -> {
                LockOn.release();
                mc.player.setYRot(mc.player.getYRot() + 70.0f);
                mc.player.setXRot(10.0f);
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            }
            case 435 -> shot(mc, "09_kamehameha_fire");
            case 436 -> {
                IntegratedServer server = mc.getSingleplayerServer();
                if (server != null) {
                    server.execute(() -> {
                        ServerPlayer sp = server.getPlayerList().getPlayerByName(name);
                        if (sp == null) {
                            return;
                        }
                        var raiders = sp.level().getEntitiesOfClass(com.dbtenkaichi.entity.RaiderEntity.class, sp.getBoundingBox().inflate(40));
                        if (!raiders.isEmpty()) {
                            var raider = raiders.getFirst();
                            raider.lookAtPoint(sp.getEyePosition());
                            var beam = com.dbtenkaichi.entity.KiBeamEntity.create(sp.level(), raider, com.dbtenkaichi.ki.Techniques.RAIDER_CANNON, 0.8);
                            sp.level().addFreshEntity(beam);
                            LOG.add("raider counter beam fired");
                        }
                    });
                }
            }
            case 446 -> shot(mc, "10b_beam_clash_side");
            case 452 -> {
                LOG.add("beam clash active on client: " + (ClientState.clashId >= 0) + " balance " + ClientState.clashBalance);
                Tenkaichi.LOG.info("[DBT-CLIENT] beam clash active: {} balance {}", ClientState.clashId >= 0, ClientState.clashBalance);
                shot(mc, "10_beam_clash");
            }
            case 458, 466 -> action(ActionType.CLASH_PULSE, 0);
            case 468 -> {
                int slot = kamehamehaSlot(data);
                if (slot >= 0) {
                    action(ActionType.SKILL_RELEASE, slot);
                }
                command(mc, "kill @e[type=dbtenkaichi:saiyan_raider]");
            }
            case 474 -> {
                Vec3 look = mc.player.getLookAngle().multiply(1, 0, 1).normalize();
                Vec3 near = mc.player.position().add(look.scale(3));
                command(mc, String.format(java.util.Locale.ROOT, "summon dbtenkaichi:saiyan_raider %.1f %.1f %.1f {NoAI:1b,NoGravity:1b}", near.x, mc.player.getY(), near.z));
            }
            case 476 -> {
                action(ActionType.COMBAT_STANCE, 1);
                IntegratedServer server = mc.getSingleplayerServer();
                if (server != null) {
                    server.execute(() -> {
                        ServerPlayer sp = server.getPlayerList().getPlayerByName(name);
                        if (sp != null) {
                            var raiders = sp.level().getEntitiesOfClass(com.dbtenkaichi.entity.RaiderEntity.class, sp.getBoundingBox().inflate(40));
                            LOG.add("raiders near after beam: " + raiders.size());
                            if (!raiders.isEmpty()) {
                                var r = raiders.getFirst();
                                Vec3 dir = r.position().subtract(sp.position()).normalize();
                                Vec3 to = r.position().subtract(dir.scale(2.2));
                                sp.teleportTo(to.x, r.getY(), to.z);
                            }
                        }
                    });
                }
            }
            case 480, 486, 492, 498 -> action(ActionType.LIGHT_ATTACK, 0);
            case 488 -> shot(mc, "11_melee_combo");
            case 520 -> {
                mc.gui.setScreen(new CharacterScreen());
            }
            case 530 -> shot(mc, "12_character_menu");
            case 540 -> {
                mc.gui.setScreen(null);
                finished = true;
                Tenkaichi.LOG.info("[DBT-CLIENT] RESULT {}", LOG);
                mc.stop();
            }
            default -> {
            }
        }
    }
}
