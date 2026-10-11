package com.dbtenkaichi.platform;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public interface INetwork {
    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    void sendToTracking(Entity entity, CustomPacketPayload payload);

    void sendToTrackingAndSelf(Entity entity, CustomPacketPayload payload);

    void sendToServer(CustomPacketPayload payload);
}
