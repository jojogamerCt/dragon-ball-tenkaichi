package com.dbtenkaichi.client.screen;

import com.dbtenkaichi.client.ClientState;
import com.dbtenkaichi.client.hud.MissionHud;
import com.dbtenkaichi.client.ui.TenkaichiButton;
import com.dbtenkaichi.client.ui.TenkaichiUi;
import com.dbtenkaichi.network.ActionType;
import com.dbtenkaichi.network.Packets;
import com.dbtenkaichi.platform.Services;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class MissionScreen extends Screen {
    private long lastSync;
    private int lastHash;

    public MissionScreen() {
        super(Component.translatable("screen.dbtenkaichi.missions"));
    }

    private static List<CompoundTag> list(String key) {
        List<CompoundTag> out = new ArrayList<>();
        ListTag tags = MissionHud.data().getListOrEmpty(key);
        for (Tag tag : tags) {
            if (tag instanceof CompoundTag c) {
                out.add(c);
            }
        }
        return out;
    }

    @Override
    protected void init() {
        if (lastSync == 0) {
            Services.NETWORK.sendToServer(new Packets.Request("missions_sync", ""));
            lastSync = ClientState.clientTicks;
        }
        lastHash = MissionHud.data().hashCode();
        int left = this.width / 2 - 190;
        int right = this.width / 2 + 6;
        String tracked = MissionHud.data().getStringOr("tracked", "");
        int y = 52;
        for (CompoundTag entry : list("active")) {
            String id = entry.getStringOr("id", "");
            boolean story = entry.getBooleanOr("story", false);
            Component title = Component.literal(id.equals(tracked) ? "★ " : "").append(Component.translatable("mission.dbtenkaichi." + id));
            Button track = TenkaichiButton.create(title, b -> Services.NETWORK.sendToServer(new Packets.Request("mission_track", id)))
                    .bounds(left, y, story ? 184 : 150, 18).build();
            addRenderableWidget(track);
            if (!story) {
                addRenderableWidget(TenkaichiButton.create(Component.literal("✕"), b -> Services.NETWORK.sendToServer(new Packets.Request("mission_abandon", id)))
                        .bounds(left + 154, y, 30, 18).build());
            }
            y += 30;
        }
        y = 52;
        for (CompoundTag entry : list("available")) {
            if (entry.getBooleanOr("story", false) && !entry.getBooleanOr("ready", false)) {
                continue;
            }
            String id = entry.getStringOr("id", "");
            boolean ready = entry.getBooleanOr("ready", false);
            Component label = Component.translatable(entry.getBooleanOr("story", false) ? "screen.dbtenkaichi.missions.start_story" : "screen.dbtenkaichi.missions.accept");
            Button accept = TenkaichiButton.create(label, b -> Services.NETWORK.sendToServer(new Packets.Request("mission_accept", id)))
                    .bounds(right + 120, y + 2, 64, 14).build();
            accept.active = ready;
            addRenderableWidget(accept);
            y += 32;
            if (y > this.height - 70) {
                break;
            }
        }
        boolean waypoint = MissionHud.data().getBooleanOr("waypoint", true);
        addRenderableWidget(TenkaichiButton.create(Component.translatable("screen.dbtenkaichi.missions.waypoint", Component.translatable(waypoint ? "options.on" : "options.off")),
                b -> Services.NETWORK.sendToServer(new Packets.Action(ActionType.WAYPOINT_TOGGLE.ordinal(), 0))).bounds(left, this.height - 30, 184, 18).build());
    }

    @Override
    public void tick() {
        super.tick();
        if (MissionHud.data().hashCode() != lastHash) {
            rebuildWidgets();
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
        TenkaichiUi.background(g, this.width, this.height);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
        TenkaichiUi.panel(g, this.width / 2 - 200, 4, 400, this.height - 8);
        TenkaichiUi.header(g, this.font, Component.translatable("screen.dbtenkaichi.missions"), this.width / 2, 14);
        int left = this.width / 2 - 190;
        int right = this.width / 2 + 6;
        g.text(this.font, Component.translatable("screen.dbtenkaichi.missions.active"), left, 38, TenkaichiUi.GOLD, true);
        g.text(this.font, Component.translatable("screen.dbtenkaichi.missions.available"), right, 38, TenkaichiUi.GOLD, true);
        TenkaichiUi.divider(g, left, 48, 184);
        TenkaichiUi.divider(g, right, 48, 184);
        super.extractRenderState(g, mouseX, mouseY, partial);
        int y = 52;
        for (CompoundTag entry : list("active")) {
            int stage = entry.getIntOr("stage", 0);
            int stages = Math.max(1, entry.getIntOr("stages", 1));
            TenkaichiUi.gauge(g, left + 2, y + 21, 180, 4, stage / (float) stages, 0.0f, entry.getBooleanOr("story", false) ? 0xFFFFC440 : 0xFF7CC8FF);
            y += 30;
        }
        y = 52;
        for (CompoundTag entry : list("available")) {
            if (entry.getBooleanOr("story", false) && !entry.getBooleanOr("ready", false)) {
                continue;
            }
            String id = entry.getStringOr("id", "");
            boolean ready = entry.getBooleanOr("ready", false);
            boolean done = entry.getBooleanOr("done", false);
            Component title = Component.translatable("mission.dbtenkaichi." + id);
            g.text(this.font, title, right, y + 4, ready ? TenkaichiUi.TEXT : 0xFF888888, true);
            String desc = Component.translatable("mission.dbtenkaichi." + id + ".desc").getString();
            if (this.font.width(desc) > 180) {
                desc = this.font.plainSubstrByWidth(desc, 180 - this.font.width("...")).stripTrailing() + "...";
            }
            g.text(this.font, desc, right, y + 15, TenkaichiUi.MUTED, true);
            Component info = done ? Component.translatable("screen.dbtenkaichi.missions.repeat", entry.getLongOr("reward", 0))
                    : Component.translatable("screen.dbtenkaichi.missions.reward", entry.getLongOr("reward", 0));
            if (!ready && entry.getLongOr("bp", 0) > 0) {
                info = Component.translatable("screen.dbtenkaichi.missions.requires_bp", entry.getLongOr("bp", 0));
            }
            g.text(this.font, info, right + 2, y + 24, ready ? 0xFF7CFFB0 : 0xFFFF7A6A, true);
            y += 32;
            if (y > this.height - 70) {
                break;
            }
        }
        g.text(this.font, Component.translatable("screen.dbtenkaichi.missions.completed", MissionHud.data().getIntOr("completed", 0)), right, this.height - 26, TenkaichiUi.MUTED, true);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
