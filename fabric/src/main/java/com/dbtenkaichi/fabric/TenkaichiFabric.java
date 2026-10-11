package com.dbtenkaichi.fabric;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.command.TenkaichiCommands;
import com.dbtenkaichi.form.FormReloadListener;
import com.dbtenkaichi.network.TenkaichiNetwork;
import com.dbtenkaichi.registry.ModEntities;
import com.dbtenkaichi.server.PlayerManager;
import com.dbtenkaichi.server.ServerEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class TenkaichiFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Tenkaichi.init();
        ModEntities.ATTRIBUTES.forEach((type, attributes) -> FabricDefaultAttributeRegistry.register((EntityType<? extends LivingEntity>) type.get(), attributes.get()));
        registerNetwork();
        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(FormReloadListener.ID, new FormReloadListener());

        ServerTickEvents.END_SERVER_TICK.register(ServerEvents::onServerTick);
        ServerLifecycleEvents.SERVER_STOPPING.register(ServerEvents::onServerStopping);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> {
            if (success) {
                ServerEvents.onDataReloaded(server);
            }
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> PlayerManager.onJoin(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> PlayerManager.onLeave(handler.player));
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> ServerEvents.onClone(oldPlayer, newPlayer, alive));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> PlayerManager.onRespawn(newPlayer, !alive));
        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, from, to) -> PlayerManager.onRespawn(player, false));
        EntityTrackingEvents.START_TRACKING.register((entity, player) -> PlayerManager.onStartTracking(player, entity));
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> TenkaichiCommands.register(dispatcher));
        ServerEvents.logStartup();
    }

    private static void registerNetwork() {
        for (TenkaichiNetwork.Serverbound<?> spec : TenkaichiNetwork.SERVERBOUND) {
            registerServerbound(spec);
        }
        for (TenkaichiNetwork.Clientbound<?> spec : TenkaichiNetwork.CLIENTBOUND) {
            registerClientbound(spec);
        }
    }

    private static <T extends CustomPacketPayload> void registerServerbound(TenkaichiNetwork.Serverbound<T> spec) {
        PayloadTypeRegistry.serverboundPlay().register(spec.type(), spec.codec());
        ServerPlayNetworking.registerGlobalReceiver(spec.type(), (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> spec.handler().accept(payload, player));
        });
    }

    private static <T extends CustomPacketPayload> void registerClientbound(TenkaichiNetwork.Clientbound<T> spec) {
        PayloadTypeRegistry.clientboundPlay().register(spec.type(), spec.codec());
    }
}
