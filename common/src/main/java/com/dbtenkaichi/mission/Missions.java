package com.dbtenkaichi.mission;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.dbtenkaichi.mission.Mission.*;

public final class Missions {
    public static final String FIRST_STORY = "saiyan_saga_01_reunion";
    private static final Map<String, Mission> ALL = new LinkedHashMap<>();

    static {
        story("saiyan_saga_01_reunion")
                .stage(go("kame_house", 14))
                .stage(scene("reunion"))
                .reward(150)
                .next("saiyan_saga_02_alliance");
        story("saiyan_saga_02_alliance")
                .stage(go("raditz_field", 18))
                .stage(scene("alliance"))
                .stage(fight("raditz_field", enemy("raditz", 1.05, 0.3f, null, 0, 8), ally("piccolo", 0.85, 4, -4)))
                .stage(scene("gohan_rage", "despawn_all", "flag:halo", "teleport:check_in"))
                .reward(300)
                .next("saiyan_saga_03_snake_way");
        story("saiyan_saga_03_snake_way")
                .stage(scene("king_yemma"))
                .stage(go("king_kai_planet", 14))
                .stage(scene("king_kai_meeting"))
                .reward(200)
                .next("saiyan_saga_04_bubbles");
        story("saiyan_saga_04_bubbles")
                .stage(catchNpc("king_kai_planet", "bubbles", 0.4))
                .stage(scene("bubbles_caught"))
                .reward(200)
                .next("saiyan_saga_05_gregory");
        story("saiyan_saga_05_gregory")
                .stage(hitNpc("king_kai_planet", "gregory", 1))
                .stage(scene("gregory_hit"))
                .reward(250)
                .next("saiyan_saga_06_techniques");
        story("saiyan_saga_06_techniques")
                .stage(scene("king_kai_training", "learn:dbtenkaichi:kaioken", "learn:dbtenkaichi:spirit_bomb", "flag:gravity_trained"))
                .stage(technique("dbtenkaichi:kaioken"))
                .stage(technique("dbtenkaichi:spirit_bomb"))
                .reward(500)
                .next("saiyan_saga_07_return");
        story("saiyan_saga_07_return")
                .stage(scene("wish", "unflag:halo"))
                .stage(go("check_in", 12))
                .stage(scene("saiyans_land", "teleport:battlefield"))
                .stage(go("battlefield", 24))
                .reward(300)
                .next("saiyan_saga_08_nappa");
        story("saiyan_saga_08_nappa")
                .stage(scene("goku_arrives"))
                .stage(fight("battlefield", enemy("nappa", 1.1, 0.15f, null, 0, 10)))
                .stage(scene("nappa_end", "despawn:nappa"))
                .reward(500)
                .next("saiyan_saga_09_vegeta");
        story("saiyan_saga_09_vegeta")
                .stage(fight("battlefield", enemy("vegeta", 1.2, 0.55f, null, 0, 10)))
                .stage(scene("beam_clash"))
                .stage(fight("battlefield", enemy("vegeta", 1.25, 0.3f, null, 0, 10)))
                .stage(scene("power_ball"))
                .stage(fight("battlefield", enemy("vegeta", 1.3, 0.5f, "dbtenkaichi:great_ape", 0, 14)))
                .stage(scene("tail_cut"))
                .stage(scene("vegeta_escapes", "despawn_all"))
                .reward(1500)
                .next("saiyan_saga_10_epilogue");
        story("saiyan_saga_10_epilogue")
                .stage(go("kame_house", 14))
                .stage(scene("epilogue", "flag:saiyan_saga_complete"))
                .reward(1000);

        side("side_raider_hunt")
                .stage(defeat("dbtenkaichi:saiyan_raider", 5))
                .reward(400)
                .requires("", 2_000L);
        side("side_korin_water")
                .stage(go("korin_tower", 12))
                .stage(flag("lesson:sacred_water"))
                .reward(300)
                .requires("", 5_000L);
        side("side_roshi_lunch")
                .stage(deliver("kame_house", "minecraft:cooked_cod", 6))
                .reward(250)
                .requires("", 0L);
        side("side_power_level")
                .stage(flag("scouter_9000"))
                .reward(300)
                .requires("", 0L);
        side("side_time_chamber")
                .stage(go("kami_lookout", 16))
                .stage(flag("lesson:time_chamber"))
                .reward(500)
                .requires("", 20_000L);
        side("side_turtle_training")
                .stage(battlePower(10_000L))
                .reward(250)
                .requires("", 0L);
    }

    private Missions() {
    }

    private static Mission story(String id) {
        Mission m = new Mission(id, true);
        ALL.put(id, m);
        return m;
    }

    private static Mission side(String id) {
        Mission m = new Mission(id, false);
        ALL.put(id, m);
        return m;
    }

    public static Mission get(String id) {
        return id == null ? null : ALL.get(id);
    }

    public static Map<String, Mission> all() {
        return ALL;
    }

    public static List<Mission> side() {
        List<Mission> out = new ArrayList<>();
        for (Mission m : ALL.values()) {
            if (!m.story) {
                out.add(m);
            }
        }
        return out;
    }

    public static List<Mission> story() {
        List<Mission> out = new ArrayList<>();
        for (Mission m : ALL.values()) {
            if (m.story) {
                out.add(m);
            }
        }
        return out;
    }
}
