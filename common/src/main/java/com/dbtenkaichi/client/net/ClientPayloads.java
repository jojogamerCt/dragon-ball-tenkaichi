package com.dbtenkaichi.client.net;

import com.dbtenkaichi.network.Packets;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.function.Consumer;

public final class ClientPayloads {
    private ClientPayloads() {
    }

    public static void registerAll(Registrar registrar) {
        registrar.register(Packets.SyncData.TYPE, ClientPacketHandler::syncData);
        registrar.register(Packets.Vitals.TYPE, ClientPacketHandler::vitals);
        registrar.register(Packets.Appearance.TYPE, ClientPacketHandler::appearance);
        registrar.register(Packets.EntityState.TYPE, ClientPacketHandler::entityState);
        registrar.register(Packets.Anim.TYPE, ClientPacketHandler::anim);
        registrar.register(Packets.CombatFx.TYPE, ClientPacketHandler::combatFx);
        registrar.register(Packets.Forms.TYPE, ClientPacketHandler::forms);
        registrar.register(Packets.OpenScreen.TYPE, ClientPacketHandler::openScreen);
        registrar.register(Packets.Clash.TYPE, ClientPacketHandler::clash);
        registrar.register(Packets.Cinematic.TYPE, ClientPacketHandler::cinematic);
        registrar.register(Packets.ServerConfig.TYPE, ClientPacketHandler::serverConfig);
    }

    public interface Registrar {
        <T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> type, Consumer<T> handler);
    }
}
