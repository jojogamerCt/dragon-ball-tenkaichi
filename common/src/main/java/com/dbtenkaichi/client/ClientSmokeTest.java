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

    private static net.minecraft.world.phys.Vec3 trialOrigin;
    private static final String[] BEAMS = {"galick_gun", "masenko", "special_beam_cannon", "final_flash"};
    private static final String[] BLASTS = {"big_bang_attack", "death_beam"};

    private static void showcase(Minecraft mc, String name, int t) {
        if (t < 0 || t >= 780) {
            return;
        }
        if (t == 0) {
            mc.gui.setScreen(null);
            if (trialOrigin != null) {
                command(mc, String.format(java.util.Locale.ROOT, "tp %s %.2f %.2f %.2f", name, trialOrigin.x, trialOrigin.y, trialOrigin.z));
            }
            for (String id : BEAMS) {
                command(mc, "tenkaichi learn " + name + " dbtenkaichi:" + id);
            }
            for (String id : BLASTS) {
                command(mc, "tenkaichi learn " + name + " dbtenkaichi:" + id);
            }
            return;
        }
        if (t == 2) {
            action(ActionType.FLIGHT_TOGGLE, 0);
            return;
        }
        int beam = (t - 8) / 140;
        int local = (t - 8) % 140;
        if (t >= 8 && beam < BEAMS.length) {
            String id = "dbtenkaichi:" + BEAMS[beam];
            String label = "18" + (char) ('a' + beam) + "_" + BEAMS[beam];
            switch (local) {
                case 0 -> {
                    command(mc, "tenkaichi restore " + name);
                    Services.NETWORK.sendToServer(new Packets.AssignSlot(0, id));
                }
                case 4 -> action(ActionType.SKILL_PRESS, 0);
                case 18 -> mc.gui.setScreen(new PosePreview(25.0f));
                case 20 -> {
                    LOG.add(label + " style " + com.dbtenkaichi.ki.SkillStyle.fromFlags(ClientState.flags));
                    shot(mc, label + "_charge");
                }
                case 21 -> {
                    mc.gui.setScreen(null);
                    mc.options.setCameraType(CameraType.FIRST_PERSON);
                }
                case 25 -> shot(mc, label + "_charge_fp");
                case 26 -> mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                case 40 -> action(ActionType.SKILL_RELEASE, 0);
                case 46 -> mc.gui.setScreen(new PosePreview(25.0f));
                case 48 -> shot(mc, label + "_fire");
                case 49 -> {
                    mc.gui.setScreen(null);
                    mc.options.setCameraType(CameraType.FIRST_PERSON);
                }
                case 52 -> shot(mc, label + "_fire_fp");
                case 53 -> mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                default -> {
                }
            }
            return;
        }
        int blastStart = 8 + BEAMS.length * 140;
        int blast = (t - blastStart) / 100;
        int b = (t - blastStart) % 100;
        if (t >= blastStart && blast < BLASTS.length) {
            String label = "18" + (char) ('a' + BEAMS.length + blast) + "_" + BLASTS[blast];
            switch (b) {
                case 0 -> {
                    command(mc, "tenkaichi restore " + name);
                    Services.NETWORK.sendToServer(new Packets.AssignSlot(0, "dbtenkaichi:" + BLASTS[blast]));
                }
                case 5 -> mc.gui.setScreen(new PosePreview(25.0f));
                case 6 -> action(ActionType.SKILL_PRESS, 0);
                case 10 -> shot(mc, label + "_fire");
                case 11 -> {
                    action(ActionType.SKILL_RELEASE, 0);
                    mc.gui.setScreen(null);
                }
                default -> {
                }
            }
        }
    }

    private static void tour(Minecraft mc, String name, String site, int dx, int dy, int dz, int lx, int ly, int lz) {
        net.minecraft.core.BlockPos pos = com.dbtenkaichi.world.Landmarks.sites().get(site);
        if (pos == null) {
            LOG.add("missing landmark " + site);
            return;
        }
        command(mc, String.format(java.util.Locale.ROOT, "tp %s %d %d %d facing %d %d %d", name, pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz, pos.getX() + lx, pos.getY() + ly, pos.getZ() + lz));
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

    private static final class PosePreview extends net.minecraft.client.gui.screens.Screen {
        private final float yaw;

        PosePreview(float yaw) {
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
                        a.eyebrowStyle, a.hairStyle, a.hairColor, a.expression, a.voice, a.auraColor, a.outfit, a.outfitPrimary, a.outfitSecondary, a.featureColor, true, false));
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
                command(mc, "give " + name + " dbtenkaichi:senzu_bean 8");
                command(mc, "give " + name + " dbtenkaichi:master_spawn_egg");
                command(mc, "give " + name + " dbtenkaichi:saiyan_raider_spawn_egg");
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
            case 227 -> shot(mc, "05a_ssj_face_closeup");
            case 249 -> shot(mc, "05b_ssj_lightning_wide");
            case 271 -> shot(mc, "05c_ssj_full_body_rocks");
            case 297 -> shot(mc, "05d_ssj_hair_flicker");
            case 314 -> shot(mc, "05e_ssj_scream");
            case 336 -> shot(mc, "05f_ssj_aura_back");
            case 361 -> shot(mc, "05g_ssj_reveal");
            case 385 -> {
                LOG.add("form after transformation: " + data.form());
                Tenkaichi.LOG.info("[DBT-CLIENT] form: {}", data.form());
                shot(mc, "06_super_saiyan");
            }
            case 430 -> {
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                Vec3 look = mc.player.getLookAngle().multiply(1, 0, 1).normalize();
                Vec3 at = mc.player.position().add(look.scale(30));
                command(mc, String.format(java.util.Locale.ROOT, "summon dbtenkaichi:saiyan_raider %.1f %.1f %.1f {NoAI:1b,NoGravity:1b}", at.x, mc.player.getY(), at.z));
                Vec3 side = mc.player.position().add(look.cross(new Vec3(0, 1, 0)).scale(5));
                command(mc, String.format(java.util.Locale.ROOT, "summon dbtenkaichi:master %.1f %.1f %.1f {NoGravity:1b}", side.x, mc.player.getY(), side.z));
                for (int kind = 1; kind <= 3; kind++) {
                    Vec3 spot = side.add(look.scale(2.2 * kind));
                    command(mc, String.format(java.util.Locale.ROOT, "summon dbtenkaichi:master %.1f %.1f %.1f {NoGravity:1b,master_kind:%d}", spot.x, mc.player.getY(), spot.z, kind));
                }
            }
            case 460 -> shot(mc, "07_npcs");
            case 470 -> {
                LockOn.toggle();
                LOG.add("locked target: " + LockOn.target());
            }
            case 480 -> {
                int slot = kamehamehaSlot(data);
                LOG.add("kamehameha slot: " + slot);
                if (slot >= 0) {
                    action(ActionType.SKILL_PRESS, slot);
                }
            }
            case 496 -> mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            case 502 -> shot(mc, "08a_kamehameha_pose_front");
            case 504 -> {
                mc.player.getInventory().setSelectedSlot(8);
                mc.options.setCameraType(CameraType.FIRST_PERSON);
            }
            case 509 -> shot(mc, "08b_kamehameha_pose_first_person");
            case 511 -> mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            case 512 -> mc.gui.setScreen(new PosePreview(0.0f));
            case 514 -> shot(mc, "08c_kamehameha_pose_view_front");
            case 515 -> mc.gui.setScreen(new PosePreview(-90.0f));
            case 517 -> shot(mc, "08d_kamehameha_pose_view_left");
            case 518 -> mc.gui.setScreen(new PosePreview(180.0f));
            case 520 -> shot(mc, "08e_kamehameha_pose_view_back");
            case 521 -> mc.gui.setScreen(new PosePreview(90.0f));
            case 523 -> shot(mc, "08f_kamehameha_pose_view_right");
            case 524 -> mc.gui.setScreen(null);
            case 528 -> shot(mc, "08_kamehameha_charge");
            case 530 -> {
                int slot = kamehamehaSlot(data);
                if (slot >= 0) {
                    action(ActionType.SKILL_RELEASE, slot);
                }
            }
            case 531 -> {
                LockOn.release();
                mc.player.setYRot(mc.player.getYRot() + 70.0f);
                mc.player.setXRot(10.0f);
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            }
            case 535 -> shot(mc, "09_kamehameha_fire");
            case 536 -> {
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
            case 546 -> shot(mc, "10b_beam_clash_side");
            case 552 -> {
                LOG.add("beam clash active on client: " + (ClientState.clashId >= 0) + " balance " + ClientState.clashBalance);
                Tenkaichi.LOG.info("[DBT-CLIENT] beam clash active: {} balance {}", ClientState.clashId >= 0, ClientState.clashBalance);
                shot(mc, "10_beam_clash");
            }
            case 558, 566 -> action(ActionType.CLASH_PULSE, 0);
            case 568 -> {
                int slot = kamehamehaSlot(data);
                if (slot >= 0) {
                    action(ActionType.SKILL_RELEASE, slot);
                }
                command(mc, "kill @e[type=dbtenkaichi:saiyan_raider]");
            }
            case 574 -> {
                Vec3 look = mc.player.getLookAngle().multiply(1, 0, 1).normalize();
                Vec3 near = mc.player.position().add(look.scale(3));
                command(mc, String.format(java.util.Locale.ROOT, "summon dbtenkaichi:saiyan_raider %.1f %.1f %.1f {NoAI:1b,NoGravity:1b}", near.x, mc.player.getY(), near.z));
            }
            case 576 -> {
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
            case 580, 586, 592, 598, 606 -> action(ActionType.LIGHT_ATTACK, 0);
            case 600 -> {
                mc.player.getInventory().setSelectedSlot(8);
                mc.options.setCameraType(CameraType.FIRST_PERSON);
            }
            case 608 -> shot(mc, "11b_first_person_punch");
            case 612 -> mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            case 588 -> shot(mc, "11_melee_combo");
            case 620 -> {
                mc.gui.setScreen(new CharacterScreen());
            }
            case 630 -> shot(mc, "12_character_menu");
            case 635 -> {
                mc.gui.setScreen(null);
                action(ActionType.TRANSFORM_DOWN, 0);
                action(ActionType.COMBAT_STANCE, 0);
                command(mc, "kill @e[type=dbtenkaichi:saiyan_raider]");
            }
            case 640 -> {
                command(mc, "tenkaichi setstat " + name + " all 160");
                command(mc, "tenkaichi restore " + name);
            }
            case 646 -> action(ActionType.TRANSFORM_UP, TransformationSystem.CINEMATIC_SKIP);
            case 662 -> {
                LOG.add("form after second transformation: " + data.form());
                shot(mc, "12b_super_saiyan_2");
                command(mc, "tenkaichi restore " + name);
            }
            case 666 -> action(ActionType.TRANSFORM_UP, TransformationSystem.CINEMATIC_SKIP);
            case 684 -> {
                LOG.add("form after third transformation: " + data.form());
                LOG.add("skills after unlocks: " + new java.util.TreeSet<>(data.skills()));
                shot(mc, "12c_super_saiyan_3");
            }
            case 690, 694, 698 -> action(ActionType.TRANSFORM_DOWN, 0);
            case 700 -> {
                command(mc, "tenkaichi learn " + name + " dbtenkaichi:kaioken");
                command(mc, "tenkaichi restore " + name);
            }
            case 704 -> action(ActionType.KAIOKEN, 0);
            case 716 -> {
                LOG.add("kaioken level after first press: " + com.dbtenkaichi.form.KaiokenSystem.fromFlags(ClientState.forEntity(mc.player).flags) + " bp " + ClientState.battlePower);
                shot(mc, "14a_kaioken");
            }
            case 720 -> action(ActionType.KAIOKEN, 0);
            case 726 -> {
                LOG.add("kaioken level after second press: " + com.dbtenkaichi.form.KaiokenSystem.fromFlags(ClientState.forEntity(mc.player).flags) + " bp " + ClientState.battlePower);
                command(mc, "tenkaichi restore " + name);
            }
            case 730 -> action(ActionType.TRANSFORM_UP, TransformationSystem.CINEMATIC_SKIP);
            case 746 -> action(ActionType.KAIOKEN, 0);
            case 758 -> {
                LOG.add("form with kaioken: " + data.form() + " level " + com.dbtenkaichi.form.KaiokenSystem.fromFlags(ClientState.forEntity(mc.player).flags) + " bp " + ClientState.battlePower);
                shot(mc, "14b_super_saiyan_kaioken");
            }
            case 762, 766 -> action(ActionType.TRANSFORM_DOWN, 0);
            case 772 -> {
                command(mc, "tenkaichi restore " + name);
                trialOrigin = mc.player.position();
                LOG.add(String.format(java.util.Locale.ROOT, "trial origin %.2f %.2f %.2f in %s", trialOrigin.x, trialOrigin.y, trialOrigin.z, mc.player.level().dimension().identifier()));
                command(mc, "tenkaichi trial start " + name + " sacred_water");
            }
            case 790 -> {
                LOG.add("trial dimension: " + mc.player.level().dimension().identifier());
                shot(mc, "15a_trial_countdown");
            }
            case 885 -> {
                java.util.List<com.dbtenkaichi.entity.MasterEntity> rivals = mc.level.getEntitiesOfClass(com.dbtenkaichi.entity.MasterEntity.class, mc.player.getBoundingBox().inflate(24));
                LOG.add("trial opponents near: " + rivals.size() + (rivals.isEmpty() ? "" : " kind " + rivals.get(0).kind() + " distance " + String.format(java.util.Locale.ROOT, "%.1f", rivals.get(0).distanceTo(mc.player))));
                mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            }
            case 887 -> {
                shot(mc, "15b_trial_fight");
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            }
            case 890 -> command(mc, "tenkaichi trial end " + name + " win");
            case 905 -> {
                net.minecraft.world.phys.Vec3 now = mc.player.position();
                LOG.add(String.format(java.util.Locale.ROOT, "after trial %.2f %.2f %.2f in %s, origin distance %.3f, sacred water %s", now.x, now.y, now.z, mc.player.level().dimension().identifier(),
                        trialOrigin == null ? -1.0 : now.distanceTo(trialOrigin), data.hasSkill("dbtenkaichi:sacred_water")));
                shot(mc, "15c_trial_return");
                command(mc, "title " + name + " clear");
                mc.options.setCameraType(CameraType.FIRST_PERSON);
                command(mc, "tenkaichi restore " + name);
                action(ActionType.FLIGHT_TOGGLE, 0);
            }
            case 910 -> tour(mc, name, com.dbtenkaichi.world.Landmarks.KAME_HOUSE, 5, 4, 12, 1, 5, -4);
            case 940 -> shot(mc, "16a_kame_house");
            case 945 -> tour(mc, name, com.dbtenkaichi.world.Landmarks.KORIN_TOWER, 14, 2, 8, 0, 4, -6);
            case 975 -> shot(mc, "16b_korin_tower_top");
            case 980 -> tour(mc, name, com.dbtenkaichi.world.Landmarks.KAMI_LOOKOUT, 0, -6, 42, 0, -4, -8);
            case 1010 -> shot(mc, "16c_kami_lookout");
            case 1015 -> tour(mc, name, com.dbtenkaichi.world.Landmarks.KAMI_LOOKOUT, 26, 6, 20, 0, 4, -8);
            case 1040 -> shot(mc, "16d_kami_palace");
            case 1044 -> {
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                LOG.add("landmarks: " + com.dbtenkaichi.world.Landmarks.sites());
                net.minecraft.nbt.CompoundTag menu = new net.minecraft.nbt.CompoundTag();
                menu.putString("mode", "menu");
                menu.putInt("master", -1);
                menu.putInt("kind", com.dbtenkaichi.entity.MasterKind.KARIN.ordinal());
                menu.putLong("tp", data.trainingPoints());
                net.minecraft.nbt.ListTag lessons = new net.minecraft.nbt.ListTag();
                for (com.dbtenkaichi.training.Lessons.Lesson lesson : com.dbtenkaichi.training.Lessons.forMaster(com.dbtenkaichi.entity.MasterKind.KARIN)) {
                    net.minecraft.nbt.CompoundTag entry = new net.minecraft.nbt.CompoundTag();
                    entry.putString("id", lesson.id());
                    entry.putInt("state", data.hasSkill(lesson.skill()) ? com.dbtenkaichi.training.Lessons.DONE : com.dbtenkaichi.training.Lessons.AVAILABLE);
                    entry.putInt("price", lesson.price());
                    entry.putBoolean("trial", lesson.trial());
                    entry.putLong("bp", lesson.battlePower());
                    lessons.add(entry);
                }
                menu.put("lessons", lessons);
                mc.gui.setScreen(new com.dbtenkaichi.client.screen.TrainingScreen(menu));
            }
            case 1048 -> shot(mc, "17_master_lessons");
            case 1992 -> {
                mc.gui.setScreen(null);
                finished = true;
                Tenkaichi.LOG.info("[DBT-CLIENT] RESULT {}", LOG);
                mc.stop();
            }
            default -> {
                showcase(mc, name, tick - 1052);
                int gallery = tick - 1832;
                if (gallery >= 0 && gallery < Race.values().length * 14) {
                    Race race = Race.values()[gallery / 14];
                    if (gallery % 14 == 0) {
                        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
                        tag.putString("races", race.id());
                        mc.gui.setScreen(new CharacterCreatorScreen(tag));
                    } else if (gallery % 14 == 12) {
                        shot(mc, "13_race_" + race.id());
                    }
                }
            }
        }
    }
}
