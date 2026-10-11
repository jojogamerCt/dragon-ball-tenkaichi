package com.dbtenkaichi.client.net;

import com.dbtenkaichi.anim.AnimClip;
import com.dbtenkaichi.client.ClientState;
import com.dbtenkaichi.client.EntityVisual;
import com.dbtenkaichi.client.fx.CombatFxHandler;
import com.dbtenkaichi.client.screen.CharacterCreatorScreen;
import com.dbtenkaichi.client.screen.TrainingScreen;
import com.dbtenkaichi.config.ConfigManager;
import com.dbtenkaichi.form.FormRegistry;
import com.dbtenkaichi.network.Packets;
import com.dbtenkaichi.platform.Services;
import com.dbtenkaichi.player.Appearance;
import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.player.RuntimeState;
import com.dbtenkaichi.race.Race;
import com.dbtenkaichi.server.PlayerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class ClientPacketHandler {
    private ClientPacketHandler() {
    }

    public static void syncData(Packets.SyncData packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        PlayerData data = Services.PLATFORM.playerData(mc.player);
        RuntimeState runtime = data.runtime();
        boolean flying = runtime.flying;
        boolean fast = runtime.fastFlight;
        data.load(packet.data());
        runtime.flying = flying;
        runtime.fastFlight = fast;
        EntityVisual visual = ClientState.visual(mc.player.getId());
        visual.race = data.race();
        visual.appearance = data.appearance().copy();
        visual.created = data.isCreated();
        visual.form = data.form();
    }

    public static void vitals(Packets.Vitals packet) {
        ClientState.ki = packet.ki();
        ClientState.maxKi = Math.max(1.0f, packet.maxKi());
        ClientState.stamina = packet.stamina();
        ClientState.maxStamina = Math.max(1.0f, packet.maxStamina());
        ClientState.fatigue = packet.fatigue();
        ClientState.battlePower = packet.battlePower();
        ClientState.trainingPoints = packet.trainingPoints();
        ClientState.flags = packet.flags();
        ClientState.chargeTicks = packet.chargeTicks();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            PlayerData data = Services.PLATFORM.playerData(mc.player);
            RuntimeState rt = data.runtime();
            rt.flying = (packet.flags() & RuntimeState.FLAG_FLYING) != 0;
            rt.fastFlight = (packet.flags() & RuntimeState.FLAG_FAST_FLIGHT) != 0;
            data.setKi(packet.ki());
            data.setStamina(packet.stamina());
            rt.cachedMaxKi = packet.maxKi();
            rt.cachedMaxStamina = packet.maxStamina();
        }
    }

    public static void appearance(Packets.Appearance packet) {
        EntityVisual visual = ClientState.visual(packet.entityId());
        visual.race = Race.byOrdinal(packet.race());
        Appearance appearance = Appearance.defaultsFor(visual.race);
        appearance.load(packet.appearance());
        visual.appearance = appearance;
        visual.created = packet.appearance().getBooleanOr("created", false);
    }

    public static void entityState(Packets.EntityState packet) {
        EntityVisual visual = ClientState.visual(packet.entityId());
        visual.form = packet.form();
        visual.flags = packet.flags();
        visual.kiRatio = packet.kiRatio();
        visual.mastery = packet.formMastery();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && packet.entityId() == mc.player.getId()) {
            ClientState.flags = packet.flags();
            PlayerData data = Services.PLATFORM.playerData(mc.player);
            data.runtime().flying = (packet.flags() & RuntimeState.FLAG_FLYING) != 0;
            data.runtime().fastFlight = (packet.flags() & RuntimeState.FLAG_FAST_FLIGHT) != 0;
            data.setForm(packet.form());
        }
    }

    public static void anim(Packets.Anim packet) {
        EntityVisual visual = ClientState.visual(packet.entityId());
        AnimClip clip = AnimClip.byOrdinal(packet.clip());
        visual.anim.play(clip, packet.durationTicks(), packet.variant(), ClientState.clientTicks);
    }

    public static void combatFx(Packets.CombatFx packet) {
        CombatFxHandler.handle(packet);
    }

    public static void forms(Packets.Forms packet) {
        FormRegistry.applyClient(packet.definitions());
    }

    public static void serverConfig(Packets.ServerConfig packet) {
        ConfigManager.applyServerConfig(packet.json());
    }

    public static void openScreen(Packets.OpenScreen packet) {
        Minecraft mc = Minecraft.getInstance();
        switch (packet.screen()) {
            case PlayerManager.SCREEN_CREATOR -> mc.gui.setScreen(new CharacterCreatorScreen(packet.data()));
            case PlayerManager.SCREEN_TRAINING -> {
                Screen current = mc.gui.screen();
                if (current instanceof TrainingScreen training) {
                    training.update(packet.data());
                } else {
                    mc.gui.setScreen(new TrainingScreen(packet.data()));
                }
            }
            default -> {
            }
        }
    }

    public static void clash(Packets.Clash packet) {
        if (packet.phase() == 2) {
            ClientState.clashId = -1;
            return;
        }
        ClientState.clashId = packet.clashId();
        ClientState.clashFirst = packet.firstId();
        ClientState.clashSecond = packet.secondId();
        ClientState.clashBalance = packet.balance();
        ClientState.clashPulse = packet.pulseTick();
        ClientState.clashUpdated = ClientState.clientTicks;
        CombatFxHandler.clashTick(packet);
    }

    public static void cinematic(Packets.Cinematic packet) {
        EntityVisual visual = ClientState.visual(packet.entityId());
        visual.cinematicStart = ClientState.clientTicks;
        visual.cinematicTicks = packet.ticks();
        visual.cinematicFull = packet.full();
        visual.cinematicForm = packet.form();
        CombatFxHandler.cinematic(packet);
    }
}
