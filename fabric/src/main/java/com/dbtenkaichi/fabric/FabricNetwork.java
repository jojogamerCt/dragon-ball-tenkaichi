package com.dbtenkaichi.fabric;

import com.dbtenkaichi.platform.INetwork;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.function.Consumer;

public class FabricNetwork implements INetwork {
    static volatile Consumer<CustomPacketPayload> clientSender = payload -> {
    };

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        if (ServerPlayNetworking.canSend(player, payload.type())) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    @Override
    public void sendToTracking(Entity entity, CustomPacketPayload payload) {
        if (entity.level().isClientSide()) {
            return;
        }
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            sendToPlayer(player, payload);
        }
    }

    @Override
    public void sendToTrackingAndSelf(Entity entity, CustomPacketPayload payload) {
        sendToTracking(entity, payload);
        if (entity instanceof ServerPlayer player) {
            sendToPlayer(player, payload);
        }
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        clientSender.accept(payload);
    }
}
