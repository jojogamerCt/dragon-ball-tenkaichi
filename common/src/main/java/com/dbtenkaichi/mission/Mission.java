package com.dbtenkaichi.mission;

import java.util.ArrayList;
import java.util.List;

public final class Mission {
    public enum Kind {
        GOTO,
        SCENE,
        FIGHT,
        CATCH,
        HIT,
        TECHNIQUE,
        DEFEAT,
        FLAG,
        BATTLE_POWER,
        DELIVER
    }

    public record Fighter(String character, String tag, double power, int role, float threshold, String form, double x, double y, double z) {
    }

    public static final class Stage {
        public final Kind kind;
        public String site = "";
        public double radius = 10.0;
        public String target = "";
        public int count = 1;
        public long amount;
        public final List<Fighter> fighters = new ArrayList<>();
        public final List<String> onStart = new ArrayList<>();
        public final List<String> onComplete = new ArrayList<>();

        Stage(Kind kind) {
            this.kind = kind;
        }

        public String objectiveKey(String missionId, int index) {
            return "mission.dbtenkaichi." + missionId + ".objective." + index;
        }
    }

    public final String id;
    public final boolean story;
    public final List<Stage> stages = new ArrayList<>();
    public long rewardTp;
    public final List<String> rewardSkills = new ArrayList<>();
    public String next = "";
    public String prerequisite = "";
    public long minBattlePower;

    Mission(String id, boolean story) {
        this.id = id;
        this.story = story;
    }

    public String titleKey() {
        return "mission.dbtenkaichi." + id;
    }

    public String descriptionKey() {
        return "mission.dbtenkaichi." + id + ".desc";
    }

    Mission stage(Stage stage) {
        stages.add(stage);
        return this;
    }

    Mission reward(long tp, String... skills) {
        rewardTp = tp;
        rewardSkills.addAll(List.of(skills));
        return this;
    }

    Mission next(String next) {
        this.next = next;
        return this;
    }

    Mission requires(String prerequisite, long minBattlePower) {
        this.prerequisite = prerequisite;
        this.minBattlePower = minBattlePower;
        return this;
    }

    static Stage go(String site, double radius, String... onStart) {
        Stage s = new Stage(Kind.GOTO);
        s.site = site;
        s.radius = radius;
        s.onStart.addAll(List.of(onStart));
        return s;
    }

    static Stage scene(String scene, String... onComplete) {
        Stage s = new Stage(Kind.SCENE);
        s.target = scene;
        s.onComplete.addAll(List.of(onComplete));
        return s;
    }

    static Stage fight(String site, Fighter... fighters) {
        Stage s = new Stage(Kind.FIGHT);
        s.site = site;
        s.radius = 64.0;
        s.fighters.addAll(List.of(fighters));
        return s;
    }

    static Stage catchNpc(String site, String character, double power) {
        Stage s = new Stage(Kind.CATCH);
        s.site = site;
        s.radius = 64.0;
        s.target = character;
        s.fighters.add(new Fighter(character, character, power, 0, -1.0f, null, 6, 0, 6));
        return s;
    }

    static Stage hitNpc(String site, String character, int count) {
        Stage s = new Stage(Kind.HIT);
        s.site = site;
        s.radius = 64.0;
        s.target = character;
        s.count = count;
        s.fighters.add(new Fighter(character, character, 0.4, 0, -1.0f, null, -6, 0, 6));
        return s;
    }

    static Stage technique(String id) {
        Stage s = new Stage(Kind.TECHNIQUE);
        s.target = id;
        return s;
    }

    static Stage defeat(String what, int count) {
        Stage s = new Stage(Kind.DEFEAT);
        s.target = what;
        s.count = count;
        return s;
    }

    static Stage flag(String flag) {
        Stage s = new Stage(Kind.FLAG);
        s.target = flag;
        return s;
    }

    static Stage battlePower(long bp) {
        Stage s = new Stage(Kind.BATTLE_POWER);
        s.amount = bp;
        return s;
    }

    static Stage deliver(String site, String item, int count) {
        Stage s = new Stage(Kind.DELIVER);
        s.site = site;
        s.radius = 6.0;
        s.target = item;
        s.count = count;
        return s;
    }

    static Fighter enemy(String character, double power, float threshold, String form, double x, double z) {
        return new Fighter(character, character, power, com.dbtenkaichi.entity.StoryNpc.ROLE_ENEMY, threshold, form, x, 0, z);
    }

    static Fighter ally(String character, double power, double x, double z) {
        return new Fighter(character, character, power, com.dbtenkaichi.entity.StoryNpc.ROLE_ALLY, -1.0f, null, x, 0, z);
    }

    static Fighter extra(String character, double x, double z) {
        return new Fighter(character, character, 0.5, com.dbtenkaichi.entity.StoryNpc.ROLE_ACTOR, -1.0f, null, x, 0, z);
    }
}
