package com.dbtenkaichi.client;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.client.input.LockOn;
import com.dbtenkaichi.form.TransformationSystem;
import com.dbtenkaichi.network.ActionType;
import com.dbtenkaichi.network.Packets;
import com.dbtenkaichi.platform.Services;
import com.dbtenkaichi.player.Appearance;
import com.dbtenkaichi.player.HairStyle;
import com.dbtenkaichi.race.Race;
import com.dbtenkaichi.world.Landmarks;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

public final class MarketingCapture {
    private static final boolean ENABLED = Boolean.getBoolean("dbtenkaichi.marketing");
    private static final Map<Integer, Consumer<Minecraft>> STEPS = new TreeMap<>();
    private static int tick = -1;
    private static int end;
    private static boolean finished;
    private static String name = "";

    private MarketingCapture() {
    }

    public static boolean enabled() {
        return ENABLED;
    }

    public static void tick(Minecraft mc) {
        if (!ENABLED || finished || mc.player == null || mc.level == null) {
            return;
        }
        if (tick < 0) {
            name = mc.player.getName().getString();
            build();
        }
        tick++;
        Consumer<Minecraft> step = STEPS.get(tick);
        if (step != null) {
            try {
                step.accept(mc);
            } catch (RuntimeException e) {
                Tenkaichi.LOG.error("[DBT-MARKETING] step {} failed", tick, e);
            }
        }
        if (tick >= end) {
            finished = true;
            Tenkaichi.LOG.info("[DBT-MARKETING] DONE");
            mc.stop();
        }
    }

    private static int at(int t, Consumer<Minecraft> step) {
        STEPS.merge(t, step, (a, b) -> a.andThen(b));
        return t;
    }

    private static void build() {
        int t = 2;
        at(t, mc -> {
            command(mc, "difficulty peaceful");
            command(mc, "time set 6000");
            command(mc, "weather clear");
            command(mc, "gamerule advance_time false");
            command(mc, "gamerule advance_weather false");
            command(mc, "kill @e[type=dbtenkaichi:saiyan_raider]");
        });
        at(t + 10, mc -> {
            Appearance a = Appearance.defaultsFor(Race.SAIYAN);
            a.hairStyle = HairStyle.GOKU.ordinal();
            a.outfit = 0;
            Services.NETWORK.sendToServer(new Packets.CreateCharacter(Race.SAIYAN.ordinal(), a.presentation, 1.0f, a.build, a.skinColor, a.eyeStyle, a.eyeColor,
                    a.eyebrowStyle, a.hairStyle, a.hairColor, a.expression, a.auraColor, a.outfit, a.outfitPrimary, a.outfitSecondary, a.featureColor, true, false));
            mc.gui.setScreen(null);
        });
        at(t + 20, mc -> {
            for (String stat : new String[]{"strength", "defense", "constitution", "ki_control", "ki_capacity", "ki_output", "speed", "agility", "stamina", "martial_arts", "spirit", "potential"}) {
                command(mc, "tenkaichi setstat " + name + " " + stat + " 60");
            }
            for (String skill : new String[]{"kamehameha", "final_flash", "big_bang_attack", "kaioken"}) {
                command(mc, "tenkaichi learn " + name + " dbtenkaichi:" + skill);
            }
            command(mc, "tenkaichi restore " + name);
            hideHud(mc, true);
        });
        t = 40;
        at(t, mc -> stage(mc, Landmarks.KORIN_TOWER, -18, 14, 135.0f));
        at(t + 30, mc -> mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
        t = transformation(80, "dbtenkaichi:super_saiyan", "ssj", new int[]{7, 51, 141}, new String[]{"face", "rocks", "reveal"});
        t = transformation(t, "dbtenkaichi:super_saiyan_2", "ssj2", new int[]{33, 50, 141}, new String[]{"backdrop", "streak", "reveal"});
        t = transformation(t, "dbtenkaichi:super_saiyan_3", "ssj3", new int[]{48, 86, 159}, new String[]{"clouds", "hair", "reveal"});
        at(t, mc -> {
            command(mc, "tenkaichi restore " + name);
            action(ActionType.KAIOKEN, 0);
        });
        at(t + 10, mc -> action(ActionType.KAIOKEN, 0));
        at(t + 14, mc -> action(ActionType.CHARGE_START, 0));
        at(t + 36, mc -> mc.gui.setScreen(new Preview(20.0f)));
        at(t + 40, mc -> {
            shot(mc, "kaioken");
            mc.gui.setScreen(null);
        });
        at(t + 42, mc -> {
            action(ActionType.CHARGE_END, 0);
            action(ActionType.TRANSFORM_DOWN, 0);
        });
        t += 60;
        at(t, mc -> {
            forceForm(mc, "dbtenkaichi:super_saiyan");
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            Vec3 look = mc.player.getLookAngle().multiply(1, 0, 1).normalize();
            Vec3 foe = mc.player.position().add(look.scale(28));
            command(mc, String.format(Locale.ROOT, "summon dbtenkaichi:saiyan_raider %.1f %.1f %.1f {NoAI:1b,NoGravity:1b}", foe.x, mc.player.getY(), foe.z));
            assign(mc, "dbtenkaichi:kamehameha");
        });
        at(t + 30, mc -> LockOn.toggle());
        at(t + 34, mc -> action(ActionType.SKILL_PRESS, 0));
        at(t + 50, mc -> mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
        at(t + 80, mc -> shot(mc, "kamehameha_charge"));
        at(t + 82, mc -> {
            action(ActionType.SKILL_RELEASE, 0);
            LockOn.release();
            mc.player.setYRot(mc.player.getYRot() + 70.0f);
            mc.player.setXRot(8.0f);
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        });
        at(t + 84, mc -> counterBeam(mc));
        at(t + 98, mc -> shot(mc, "beam_clash"));
        at(t + 104, mc -> shot(mc, "beam_clash_late"));
        at(t + 120, mc -> {
            action(ActionType.SKILL_RELEASE, 0);
            command(mc, "kill @e[type=dbtenkaichi:saiyan_raider]");
        });
        t += 140;
        t = technique(t, "dbtenkaichi:final_flash", "final_flash", true);
        t = technique(t, "dbtenkaichi:big_bang_attack", "big_bang", false);
        at(t, mc -> {
            forceForm(mc, null);
            command(mc, "gamemode spectator " + name);
            tour(mc, Landmarks.KAME_HOUSE, 9, 6, 16, 1, 5, -2);
        });
        at(t + 40, mc -> shot(mc, "kame_house"));
        at(t + 42, mc -> tour(mc, Landmarks.KORIN_TOWER, 26, -60, 26, 0, -20, -6));
        at(t + 82, mc -> shot(mc, "korin_tower"));
        at(t + 84, mc -> tour(mc, Landmarks.KAMI_LOOKOUT, 0, 10, 48, 0, 0, -8));
        at(t + 124, mc -> shot(mc, "kami_lookout"));
        at(t + 168, mc -> command(mc, "gamemode survival " + name));
        t += 180;
        t = trial(t, "dbtenkaichi:super_saiyan_2", "trial_cell");
        t = trial(t, "dbtenkaichi:super_saiyan", "trial_namek");
        Race[] races = Race.values();
        at(t, mc -> {
            stage(mc, Landmarks.KORIN_TOWER, -18, 14, 135.0f);
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        });
        t += 40;
        for (int i = 0; i < races.length; i++) {
            Race race = races[i];
            for (int presentation = 0; presentation < 2; presentation++) {
                int p = presentation;
                at(t, mc -> {
                    Appearance a = Appearance.defaultsFor(race);
                    a.presentation = p;
                    a.hairStyle = Appearance.defaultHair(race, p);
                    ClientState.preview = a;
                    ClientState.previewRace = race;
                    mc.gui.setScreen(new Preview(20.0f));
                });
                at(t + 3, mc -> shot(mc, "race_" + race.id() + (p == 1 ? "_f" : "")));
                t += 5;
            }
        }
        at(t, mc -> {
            ClientState.preview = null;
            ClientState.previewRace = null;
            mc.gui.setScreen(new com.dbtenkaichi.client.screen.CharacterScreen(2));
        });
        at(t + 6, mc -> shot(mc, "menu_forms"));
        at(t + 8, mc -> {
            net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
            StringBuilder ids = new StringBuilder();
            for (Race race : races) {
                ids.append(ids.isEmpty() ? "" : ",").append(race.id());
            }
            tag.putString("races", ids.toString());
            mc.gui.setScreen(new com.dbtenkaichi.client.screen.CharacterCreatorScreen(tag));
        });
        at(t + 16, mc -> shot(mc, "creator"));
        at(t + 18, mc -> mc.gui.setScreen(null));
        t = story(t + 24);
        t = otherWorld(t);
        t = newForms(t);
        end = t + 10;
    }

    private static void server(Minecraft mc, Consumer<ServerPlayer> action) {
        IntegratedServer server = mc.getSingleplayerServer();
        if (server == null) {
            return;
        }
        server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayerByName(name);
            if (sp != null) {
                action.accept(sp);
            }
        });
    }

    private static void site(Minecraft mc, String key, double dx, double dy, double dz, float yaw, float pitch) {
        server(mc, sp -> {
            com.dbtenkaichi.world.StoryWorld.Site site = com.dbtenkaichi.world.StoryWorld.site(sp.level().getServer(), key);
            if (site != null) {
                BlockPos p = site.pos();
                sp.teleport(new net.minecraft.world.level.portal.TeleportTransition(site.level(), new Vec3(p.getX() + 0.5 + dx, p.getY() + dy, p.getZ() + 0.5 + dz), Vec3.ZERO, yaw, pitch,
                        net.minecraft.world.level.portal.TeleportTransition.DO_NOTHING));
            }
        });
    }

    private static int scene(int t, String id, int[] offsets) {
        at(t, mc -> server(mc, sp -> {
            com.dbtenkaichi.story.StoryScene scene = com.dbtenkaichi.story.Scenes.get(id);
            com.dbtenkaichi.world.StoryWorld.Site site = scene == null ? null : com.dbtenkaichi.world.StoryWorld.site(sp.level().getServer(), scene.site);
            if (site != null) {
                com.dbtenkaichi.story.SceneDirector.start(sp, com.dbtenkaichi.server.PlayerManager.data(sp), scene, site.level(), site.pos(), () -> {
                });
            }
        }));
        int last = 0;
        for (int offset : offsets) {
            int o = offset;
            at(t + 2 + o, mc -> shot(mc, "scene_" + id + "_" + o));
            last = Math.max(last, o);
        }
        at(t + last + 8, mc -> server(mc, sp -> com.dbtenkaichi.story.SceneDirector.end(sp, false)));
        at(t + last + 10, mc -> command(mc, "kill @e[type=dbtenkaichi:story_character]"));
        return t + last + 30;
    }

    private static int story(int t) {
        at(t, mc -> {
            hideHud(mc, false);
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            command(mc, "tenkaichi mission reset " + name);
            command(mc, "tenkaichi mission start " + name + " saiyan_saga_01_reunion");
        });
        at(t + 4, mc -> site(mc, "kame_house", 30, 7, 34, 140.0f, 8.0f));
        at(t + 60, mc -> shot(mc, "mission_hud"));
        at(t + 62, mc -> {
            command(mc, "tenkaichi mission reset " + name);
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        });
        t += 70;
        t = scene(t, "reunion", new int[]{430, 560, 810});
        t = scene(t, "gohan_rage", new int[]{180, 600, 960});
        t = scene(t, "king_kai_meeting", new int[]{120, 420});
        t = scene(t, "saiyans_land", new int[]{300, 745, 960, 1080});
        t = scene(t, "beam_clash", new int[]{240, 330});
        t = scene(t, "power_ball", new int[]{200, 330, 470, 640});
        t = scene(t, "tail_cut", new int[]{200, 460, 610});
        t = scene(t, "vegeta_escapes", new int[]{330, 720});
        at(t, mc -> {
            command(mc, "tenkaichi restore " + name);
            command(mc, "item replace entity " + name + " armor.head with dbtenkaichi:scouter");
            Vec3 look = mc.player.getLookAngle().multiply(1, 0, 1).normalize();
            for (int i = 0; i < 3; i++) {
                Vec3 at = mc.player.position().add(look.scale(18 + i * 9)).add(look.cross(new Vec3(0, 1, 0)).scale((i - 1) * 7));
                command(mc, String.format(Locale.ROOT, "summon dbtenkaichi:saiyan_raider %.1f %.1f %.1f {NoAI:1b}", at.x, mc.player.getY(), at.z));
            }
            mc.options.setCameraType(CameraType.FIRST_PERSON);
        });
        at(t + 50, mc -> shot(mc, "scouter"));
        at(t + 52, mc -> {
            command(mc, "item replace entity " + name + " armor.head with minecraft:air");
            action(ActionType.SENSE_TOGGLE, 0);
        });
        at(t + 90, mc -> shot(mc, "ki_sense"));
        at(t + 92, mc -> {
            action(ActionType.SENSE_TOGGLE, 0);
            command(mc, "kill @e[type=dbtenkaichi:saiyan_raider]");
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        });
        return t + 100;
    }

    private static int otherWorld(int t) {
        at(t, mc -> {
            hideHud(mc, true);
            command(mc, "gamemode spectator " + name);
            server(mc, sp -> {
                net.minecraft.server.level.ServerLevel other = com.dbtenkaichi.world.OtherWorld.level(sp.level().getServer());
                if (other != null) {
                    double z = 330;
                    sp.teleport(new net.minecraft.world.level.portal.TeleportTransition(other, new Vec3(com.dbtenkaichi.world.OtherWorld.roadX(z) - 30, 150, z - 40), Vec3.ZERO, -40.0f, 30.0f,
                            net.minecraft.world.level.portal.TeleportTransition.DO_NOTHING));
                }
            });
        });
        at(t + 60, mc -> shot(mc, "snake_way"));
        at(t + 62, mc -> server(mc, sp -> {
            net.minecraft.server.level.ServerLevel other = com.dbtenkaichi.world.OtherWorld.level(sp.level().getServer());
            if (other != null) {
                BlockPos top = com.dbtenkaichi.world.OtherWorld.planet();
                sp.teleport(new net.minecraft.world.level.portal.TeleportTransition(other, new Vec3(top.getX() + 24, top.getY() + 10, top.getZ() - 34), Vec3.ZERO, 35.0f, 22.0f,
                        net.minecraft.world.level.portal.TeleportTransition.DO_NOTHING));
            }
        }));
        at(t + 120, mc -> shot(mc, "king_kai_planet"));
        at(t + 122, mc -> site(mc, "check_in", 0, 6, 26, 180.0f, 10.0f));
        at(t + 180, mc -> shot(mc, "check_in"));
        at(t + 182, mc -> {
            command(mc, "gamemode survival " + name);
            stage(mc, Landmarks.KORIN_TOWER, -18, 14, 135.0f);
        });
        return t + 220;
    }

    private static final String[][] NEW_FORMS = {
            {"saiyan", "great_ape", "super_saiyan_4", "super_saiyan_god", "super_saiyan_blue", "ultra_instinct", "ultra_ego", "legendary_super_saiyan"},
            {"half_saiyan", "beast", "ultimate"},
            {"frieza_race", "frieza_final_form", "golden_frieza", "black_frieza"},
            {"majin", "super_buu", "kid_buu"},
            {"bio_android", "semi_perfect", "perfect"},
            {"namekian", "orange_piccolo"},
            {"kaioshin", "fused_zamasu"},
            {"demon", "third_eye"},
            {"human", "max_power"}};

    private static int newForms(int t) {
        for (String[] group : NEW_FORMS) {
            Race race = Race.byId(group[0]);
            at(t, mc -> {
                hideHud(mc, true);
                command(mc, "tenkaichi reset " + name);
            });
            at(t + 10, mc -> {
                Appearance a = Appearance.defaultsFor(race);
                Services.NETWORK.sendToServer(new Packets.CreateCharacter(race.ordinal(), a.presentation, 1.0f, a.build, a.skinColor, a.eyeStyle, a.eyeColor,
                        a.eyebrowStyle, a.hairStyle, a.hairColor, a.expression, a.auraColor, a.outfit, a.outfitPrimary, a.outfitSecondary, a.featureColor, true, false));
                mc.gui.setScreen(null);
            });
            at(t + 20, mc -> {
                command(mc, "tenkaichi mission reset " + name);
                command(mc, "tenkaichi setstat " + name + " all 60");
                command(mc, "tenkaichi restore " + name);
                mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            });
            t += 30;
            for (int i = 1; i < group.length; i++) {
                String form = "dbtenkaichi:" + group[i];
                String label = group[i];
                at(t, mc -> {
                    command(mc, "tenkaichi restore " + name);
                    forceForm(mc, null);
                });
                at(t + 6, mc -> {
                    hideHud(mc, false);
                    forceForm(mc, form);
                });
                at(t + 6 + 70, mc -> shot(mc, "cine_" + label + "_a"));
                at(t + 6 + 130, mc -> shot(mc, "cine_" + label + "_b"));
                at(t + 6 + 215, mc -> {
                    hideHud(mc, true);
                    mc.gui.setScreen(new Preview(20.0f));
                });
                at(t + 6 + 219, mc -> shot(mc, "form_" + label));
                at(t + 6 + 221, mc -> mc.gui.setScreen(null));
                t += 235;
            }
        }
        at(t, mc -> forceForm(mc, null));
        return t + 10;
    }

    private static int transformation(int t, String form, String label, int[] offsets, String[] names) {
        at(t, mc -> {
            command(mc, "tenkaichi restore " + name);
            forceForm(mc, null);
        });
        at(t + 6, mc -> {
            hideHud(mc, false);
            forceForm(mc, form);
        });
        for (int i = 0; i < offsets.length; i++) {
            String shot = label + "_" + names[i];
            at(t + 6 + offsets[i], mc -> shot(mc, shot));
        }
        int after = t + 6 + offsets[offsets.length - 1] + 12;
        at(after, mc -> {
            hideHud(mc, true);
            mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            mc.gui.setScreen(new Preview(20.0f));
        });
        at(after + 4, mc -> shot(mc, label + "_full"));
        at(after + 6, mc -> mc.gui.setScreen(null));
        return after + 14;
    }

    private static int technique(int t, String id, String label, boolean beam) {
        at(t, mc -> {
            command(mc, "tenkaichi restore " + name);
            assign(mc, id);
            mc.player.setYRot(mc.player.getYRot() + 40.0f);
            mc.player.setXRot(beam ? -4.0f : 12.0f);
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        });
        at(t + 4, mc -> action(ActionType.SKILL_PRESS, 0));
        if (beam) {
            at(t + 40, mc -> shot(mc, label + "_charge"));
            at(t + 50, mc -> {
                action(ActionType.SKILL_RELEASE, 0);
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            });
            at(t + 62, mc -> shot(mc, label + "_fire"));
            return t + 110;
        }
        at(t + 10, mc -> shot(mc, label + "_fire"));
        at(t + 11, mc -> {
            action(ActionType.SKILL_RELEASE, 0);
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        });
        at(t + 20, mc -> shot(mc, label + "_impact"));
        at(t + 50, mc -> {
            command(mc, "gamemode spectator " + name);
            command(mc, "execute as " + name + " at @s run tp @s ^ ^14 ^-10 facing ^ ^ ^14");
        });
        at(t + 80, mc -> shot(mc, label + "_crater"));
        at(t + 82, mc -> command(mc, "gamemode survival " + name));
        return t + 100;
    }

    private static int trial(int t, String form, String label) {
        at(t, mc -> {
            command(mc, "tenkaichi restore " + name);
            forceForm(mc, null);
            command(mc, "tenkaichi trial form " + name + " " + form);
        });
        at(t + 40, mc -> {
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            mc.player.setYRot(mc.player.getYRot() + 25.0f);
            shot(mc, label + "_arena");
        });
        at(t + 112, mc -> shot(mc, label + "_fight"));
        at(t + 116, mc -> command(mc, "tenkaichi trial end " + name + " win"));
        return t + 150;
    }

    private static void counterBeam(Minecraft mc) {
        IntegratedServer server = mc.getSingleplayerServer();
        if (server == null) {
            return;
        }
        server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayerByName(name);
            if (sp == null) {
                return;
            }
            var raiders = sp.level().getEntitiesOfClass(com.dbtenkaichi.entity.RaiderEntity.class, sp.getBoundingBox().inflate(40));
            if (!raiders.isEmpty()) {
                var raider = raiders.getFirst();
                raider.lookAtPoint(sp.getEyePosition());
                sp.level().addFreshEntity(com.dbtenkaichi.entity.KiBeamEntity.create(sp.level(), raider, com.dbtenkaichi.ki.Techniques.RAIDER_CANNON, 0.8));
            }
        });
    }

    private static void stage(Minecraft mc, String site, int dx, int dz, float yaw) {
        IntegratedServer server = mc.getSingleplayerServer();
        BlockPos pos = Landmarks.sites().get(site);
        if (server == null || pos == null) {
            return;
        }
        server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayerByName(name);
            if (sp == null) {
                return;
            }
            int x = pos.getX() + dx;
            int z = pos.getZ() + dz - 6;
            sp.level().getChunk(x >> 4, z >> 4);
            int y = sp.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            sp.teleportTo(x + 0.5, y, z + 0.5);
            sp.setYRot(yaw);
            sp.setYHeadRot(yaw);
        });
    }

    private static void tour(Minecraft mc, String site, int dx, int dy, int dz, int lx, int ly, int lz) {
        BlockPos pos = Landmarks.sites().get(site);
        if (pos != null) {
            command(mc, String.format(Locale.ROOT, "tp %s %d %d %d facing %d %d %d", name, pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz, pos.getX() + lx, pos.getY() + ly, pos.getZ() + lz));
        }
    }

    private static void forceForm(Minecraft mc, String formId) {
        IntegratedServer server = mc.getSingleplayerServer();
        if (server == null) {
            return;
        }
        server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayerByName(name);
            if (sp == null) {
                return;
            }
            var pd = com.dbtenkaichi.server.PlayerManager.data(sp);
            if (formId == null) {
                TransformationSystem.forceRevert(sp, pd, null);
                return;
            }
            var form = com.dbtenkaichi.form.FormRegistry.get(formId, false);
            if (form != null) {
                TransformationSystem.begin(sp, pd, form, TransformationSystem.CINEMATIC_AUTO, true, sp.level().getGameTime());
            }
        });
    }

    private static void assign(Minecraft mc, String id) {
        Services.NETWORK.sendToServer(new Packets.AssignSlot(0, id));
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

    private static void hideHud(Minecraft mc, boolean hidden) {
        try {
            java.lang.reflect.Field field = mc.gui.hud.getClass().getDeclaredField("isHidden");
            field.setAccessible(true);
            field.setBoolean(mc.gui.hud, hidden);
        } catch (ReflectiveOperationException | RuntimeException e) {
            Tenkaichi.LOG.warn("[DBT-MARKETING] could not hide the HUD", e);
        }
    }

    private static void shot(Minecraft mc, String label) {
        Screenshot.grab(mc.gameDirectory, "mkt_" + label + ".png", mc.gameRenderer.mainRenderTarget(), 1, message -> Tenkaichi.LOG.info("[DBT-MARKETING] {}: {}", label, message.getString()));
    }

    private static final class Preview extends net.minecraft.client.gui.screens.Screen {
        private final float yaw;

        Preview(float yaw) {
            super(net.minecraft.network.chat.Component.empty());
            this.yaw = yaw;
        }

        @Override
        public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                com.dbtenkaichi.client.screen.EntityPreview.draw(g, 0, 0, this.width, this.height, (int) (this.height * 0.3f), yaw, -5.0f, 1.0f, mc.player);
            }
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }
}
