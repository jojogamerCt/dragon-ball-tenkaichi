package com.dbtenkaichi.story;

import java.util.ArrayList;
import java.util.List;

public final class StoryScene {
    public final String id;
    public final int duration;
    public final String site;
    public boolean remote;
    public PlayerSpec player = new PlayerSpec(0, 0, 0, 0, false);
    public final List<ActorSpec> actors = new ArrayList<>();
    public final List<Action> actions = new ArrayList<>();
    public final List<Line> lines = new ArrayList<>();
    public final List<Shot> shots = new ArrayList<>();
    public final List<Effect> effects = new ArrayList<>();

    public record ActorSpec(String name, String character, double x, double y, double z, float yaw, boolean hidden, boolean keep, boolean fly) {
    }

    public record PlayerSpec(double x, double y, double z, float yaw, boolean hidden) {
    }

    public record Action(int at, String type, String actor, String other, String value, double x, double y, double z, double amount) {
    }

    public record Line(int from, int to, String speaker, String key) {
    }

    public record Cam(String target, float yaw, float pitch, float distance, float height) {
    }

    public record Shot(int from, int to, Cam start, Cam end, boolean ease) {
    }

    public record Effect(int from, int to, String kind, int color, float a, float b) {
    }

    StoryScene(String id, String site, int duration) {
        this.id = id;
        this.site = site;
        this.duration = duration;
    }

    public static StoryScene create(String id, String site, int duration) {
        return new StoryScene(id, site, duration);
    }

    public StoryScene remote() {
        this.remote = true;
        return this;
    }

    public StoryScene player(double x, double y, double z, float yaw) {
        this.player = new PlayerSpec(x, y, z, yaw, false);
        return this;
    }

    public StoryScene hiddenPlayer(double x, double y, double z) {
        this.player = new PlayerSpec(x, y, z, 0, true);
        return this;
    }

    public StoryScene actor(String name, String character, double x, double y, double z, float yaw) {
        actors.add(new ActorSpec(name, character, x, y, z, yaw, false, false, false));
        return this;
    }

    public StoryScene hiddenActor(String name, String character, double x, double y, double z, float yaw) {
        actors.add(new ActorSpec(name, character, x, y, z, yaw, true, false, false));
        return this;
    }

    public StoryScene kept(String name, String character, double x, double y, double z, float yaw) {
        actors.add(new ActorSpec(name, character, x, y, z, yaw, false, true, false));
        return this;
    }

    public StoryScene flying(String name, String character, double x, double y, double z, float yaw) {
        actors.add(new ActorSpec(name, character, x, y, z, yaw, false, false, true));
        return this;
    }

    public StoryScene act(int at, String type, String actor, String other, String value, double x, double y, double z, double amount) {
        actions.add(new Action(at, type, actor, other, value, x, y, z, amount));
        return this;
    }

    public StoryScene move(int at, String actor, double x, double y, double z, double speed, boolean fly) {
        return act(at, "move", actor, fly ? "fly" : "walk", "", x, y, z, speed);
    }

    public StoryScene teleport(int at, String actor, double x, double y, double z) {
        return act(at, "teleport", actor, "", "", x, y, z, 0);
    }

    public StoryScene look(int at, String actor, String other) {
        return act(at, "look", actor, other, "", 0, 0, 0, 0);
    }

    public StoryScene anim(int at, String actor, String clip, int duration) {
        return act(at, "anim", actor, "", clip, 0, 0, 0, duration);
    }

    public StoryScene fx(int at, String actor, String kind, double strength) {
        return act(at, "fx", actor, "", kind, 0, 1, 0, strength);
    }

    public StoryScene beam(int at, String actor, String target, String technique, double charge) {
        return act(at, "beam", actor, target, technique, 0, 0, 0, charge);
    }

    public StoryScene blast(int at, String actor, String target, String technique, double power) {
        return act(at, "blast", actor, target, technique, 0, 0, 0, power);
    }

    public StoryScene form(int at, String actor, String form, int ticks) {
        return act(at, "form", actor, "", form, 0, 0, 0, ticks);
    }

    public StoryScene show(int at, String actor) {
        return act(at, "show", actor, "", "", 0, 0, 0, 0);
    }

    public StoryScene hide(int at, String actor) {
        return act(at, "hide", actor, "", "", 0, 0, 0, 0);
    }

    public StoryScene aura(int at, String actor, boolean on) {
        return act(at, "aura", actor, "", "", 0, 0, 0, on ? 1 : 0);
    }

    public StoryScene fly(int at, String actor, boolean on) {
        return act(at, "fly", actor, "", "", 0, 0, 0, on ? 1 : 0);
    }

    public StoryScene sound(int at, String actor, String sound, double pitch) {
        return act(at, "sound", actor, "", sound, 0, 0, 0, pitch);
    }

    public StoryScene despawn(int at, String actor) {
        return act(at, "despawn", actor, "", "", 0, 0, 0, 0);
    }

    public StoryScene explode(int at, String actor, double radius) {
        return act(at, "explode", actor, "", "", 0, 1, 0, radius);
    }

    public StoryScene say(int from, int to, String speaker) {
        lines.add(new Line(from, to, speaker, "scene.dbtenkaichi." + id + "." + (lines.size() + 1)));
        return this;
    }

    public static Cam cam(String target, float yaw, float pitch, float distance, float height) {
        return new Cam(target, yaw, pitch, distance, height);
    }

    public StoryScene shot(int from, int to, Cam start, Cam end) {
        shots.add(new Shot(from, to, start, end, true));
        return this;
    }

    public StoryScene cut(int from, int to, Cam start, Cam end) {
        shots.add(new Shot(from, to, start, end, false));
        return this;
    }

    public StoryScene hold(int from, int to, Cam cam) {
        shots.add(new Shot(from, to, cam, cam, false));
        return this;
    }

    public StoryScene fade(int from, int to, float a, float b) {
        effects.add(new Effect(from, to, "fade", 0x000000, a, b));
        return this;
    }

    public StoryScene flash(int from, int to, int color, float a, float b) {
        effects.add(new Effect(from, to, "flash", color, a, b));
        return this;
    }

    public StoryScene tint(int from, int to, int color, float a, float b) {
        effects.add(new Effect(from, to, "tint", color, a, b));
        return this;
    }

    public StoryScene shake(int from, int to) {
        effects.add(new Effect(from, to, "shake", 0, 1, 0));
        return this;
    }

    public StoryScene title(int from, int to, String key) {
        effects.add(new Effect(from, to, "title:" + key, 0xFFFFFF, 1, 1));
        return this;
    }

    public Shot shotAt(int t) {
        Shot found = null;
        for (Shot shot : shots) {
            if (t >= shot.from && t < shot.to) {
                found = shot;
            }
        }
        return found;
    }

    public Line lineAt(int t) {
        for (Line line : lines) {
            if (t >= line.from && t < line.to) {
                return line;
            }
        }
        return null;
    }

    public ActorSpec actorSpec(String name) {
        for (ActorSpec spec : actors) {
            if (spec.name.equals(name)) {
                return spec;
            }
        }
        return null;
    }
}
