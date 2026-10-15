package com.dbtenkaichi.mission;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class MissionState {
    private final Map<String, Active> active = new LinkedHashMap<>();
    private final Set<String> completed = new LinkedHashSet<>();
    private final Set<String> flags = new LinkedHashSet<>();
    private String tracked = "";
    private boolean waypoint = true;

    public static final class Active {
        public final String id;
        public int stage;
        public int progress;
        public long stageStart;

        public Active(String id) {
            this.id = id;
        }
    }

    public Map<String, Active> active() {
        return active;
    }

    public Active get(String id) {
        return active.get(id);
    }

    public boolean isActive(String id) {
        return active.containsKey(id);
    }

    public boolean isCompleted(String id) {
        return completed.contains(id);
    }

    public Set<String> completed() {
        return completed;
    }

    public Active start(String id, long now) {
        Active a = new Active(id);
        a.stageStart = now;
        active.put(id, a);
        if (tracked.isEmpty() || !active.containsKey(tracked)) {
            tracked = id;
        }
        return a;
    }

    public void complete(String id) {
        active.remove(id);
        completed.add(id);
        if (tracked.equals(id)) {
            tracked = active.isEmpty() ? "" : active.keySet().iterator().next();
        }
    }

    public void abandon(String id) {
        active.remove(id);
        if (tracked.equals(id)) {
            tracked = active.isEmpty() ? "" : active.keySet().iterator().next();
        }
    }

    public void reset() {
        active.clear();
        completed.clear();
        flags.clear();
        tracked = "";
    }

    public String tracked() {
        return tracked;
    }

    public void track(String id) {
        if (active.containsKey(id)) {
            tracked = id;
        }
    }

    public boolean waypoint() {
        return waypoint;
    }

    public void setWaypoint(boolean waypoint) {
        this.waypoint = waypoint;
    }

    public boolean flag(String flag) {
        return flags.contains(flag);
    }

    public void setFlag(String flag, boolean value) {
        if (value) {
            flags.add(flag);
        } else {
            flags.remove(flag);
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        CompoundTag list = new CompoundTag();
        for (Active a : active.values()) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("stage", a.stage);
            entry.putInt("progress", a.progress);
            entry.putLong("start", a.stageStart);
            list.put(a.id, entry);
        }
        tag.put("active", list);
        tag.put("completed", strings(completed));
        tag.put("flags", strings(flags));
        tag.putString("tracked", tracked);
        tag.putBoolean("waypoint", waypoint);
        return tag;
    }

    public void load(CompoundTag tag) {
        active.clear();
        completed.clear();
        flags.clear();
        CompoundTag list = tag.getCompoundOrEmpty("active");
        for (String id : list.keySet()) {
            CompoundTag entry = list.getCompoundOrEmpty(id);
            Active a = new Active(id);
            a.stage = entry.getIntOr("stage", 0);
            a.progress = entry.getIntOr("progress", 0);
            a.stageStart = entry.getLongOr("start", 0L);
            active.put(id, a);
        }
        read(tag.getListOrEmpty("completed"), completed);
        read(tag.getListOrEmpty("flags"), flags);
        tracked = tag.getStringOr("tracked", "");
        waypoint = tag.getBooleanOr("waypoint", true);
    }

    private static ListTag strings(Set<String> values) {
        ListTag list = new ListTag();
        for (String value : values) {
            list.add(StringTag.valueOf(value));
        }
        return list;
    }

    private static void read(ListTag list, Set<String> out) {
        for (Tag element : list) {
            element.asString().ifPresent(out::add);
        }
    }
}
