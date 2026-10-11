package com.dbtenkaichi.neoforge;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.command.TenkaichiCommands;
import com.dbtenkaichi.form.FormReloadListener;
import com.dbtenkaichi.network.TenkaichiNetwork;
import com.dbtenkaichi.registry.ModEntities;
import com.dbtenkaichi.server.PlayerManager;
import com.dbtenkaichi.server.ServerEvents;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(Tenkaichi.MOD_ID)
public class TenkaichiNeoForge {
    public TenkaichiNeoForge(IEventBus modBus, ModContainer container, Dist dist) {
        Tenkaichi.init();
        NeoForgeRegistration.attach(modBus);
        NeoForgePlatform.ATTACHMENTS.register(modBus);
        modBus.addListener(TenkaichiNeoForge::registerPayloads);
        modBus.addListener(TenkaichiNeoForge::registerAttributes);

        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> ServerEvents.onServerTick(event.getServer()));
        NeoForge.EVENT_BUS.addListener((ServerStoppingEvent event) -> ServerEvents.onServerStopping(event.getServer()));
        NeoForge.EVENT_BUS.addListener((AddServerReloadListenersEvent event) -> event.addListener(FormReloadListener.ID, new FormReloadListener()));
        NeoForge.EVENT_BUS.addListener((OnDatapackSyncEvent event) -> {
            if (event.getPlayer() == null) {
                ServerEvents.onDataReloaded(event.getPlayerList().getServer());
            }
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                PlayerManager.onJoin(player);
            }
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                PlayerManager.onLeave(player);
            }
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.Clone event) -> {
            if (event.getEntity() instanceof ServerPlayer clone && event.getOriginal() instanceof ServerPlayer original) {
                ServerEvents.onClone(original, clone, !event.isWasDeath());
            }
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                PlayerManager.onRespawn(player, !event.isEndConquered());
            }
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerChangedDimensionEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                PlayerManager.onRespawn(player, false);
            }
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.StartTracking event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                PlayerManager.onStartTracking(player, event.getTarget());
            }
        });
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> TenkaichiCommands.register(event.getDispatcher()));

        if (dist.isClient()) {
            TenkaichiNeoForgeClient.init(modBus);
        }
        ServerEvents.logStartup();
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(TenkaichiNetwork.VERSION);
        for (TenkaichiNetwork.Serverbound<?> spec : TenkaichiNetwork.SERVERBOUND) {
            serverbound(registrar, spec);
        }
        for (TenkaichiNetwork.Clientbound<?> spec : TenkaichiNetwork.CLIENTBOUND) {
            clientbound(registrar, spec);
        }
    }

    private static <T extends CustomPacketPayload> void serverbound(PayloadRegistrar registrar, TenkaichiNetwork.Serverbound<T> spec) {
        registrar.playToServer(spec.type(), spec.codec(), (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) {
                spec.handler().accept(payload, player);
            }
        });
    }

    private static <T extends CustomPacketPayload> void clientbound(PayloadRegistrar registrar, TenkaichiNetwork.Clientbound<T> spec) {
        registrar.playToClient(spec.type(), spec.codec());
    }

    @SuppressWarnings("unchecked")
    private static void registerAttributes(EntityAttributeCreationEvent event) {
        ModEntities.ATTRIBUTES.forEach((type, attributes) -> event.put((EntityType<? extends LivingEntity>) type.get(), attributes.get().build()));
    }
}
