package com.dbtenkaichi.neoforge;

import com.dbtenkaichi.platform.INetwork;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.function.Consumer;

public class NeoForgeNetwork implements INetwork {
    static volatile Consumer<CustomPacketPayload> clientSender = payload -> {
    };

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        if (player.connection != null && player.connection.hasChannel(payload)) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    @Override
    public void sendToTracking(Entity entity, CustomPacketPayload payload) {
        if (!entity.level().isClientSide()) {
            PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
        }
    }

    @Override
    public void sendToTrackingAndSelf(Entity entity, CustomPacketPayload payload) {
        if (!entity.level().isClientSide()) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
        }
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        clientSender.accept(payload);
    }
}
