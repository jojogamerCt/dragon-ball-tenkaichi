package com.dbtenkaichi.neoforge;

import com.dbtenkaichi.client.TenkaichiClient;
import com.dbtenkaichi.client.hud.TenkaichiHud;
import com.dbtenkaichi.client.input.ClientController;
import com.dbtenkaichi.client.input.Keybinds;
import com.dbtenkaichi.client.net.ClientPayloads;
import com.dbtenkaichi.client.render.TenkaichiLayers;
import com.dbtenkaichi.client.render.TenkaichiModels;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.function.Consumer;

public final class TenkaichiNeoForgeClient {
    private TenkaichiNeoForgeClient() {
    }

    public static void init(IEventBus modBus) {
        TenkaichiClient.init();
        NeoForgeNetwork.clientSender = payload -> ClientPacketDistributor.sendToServer(payload);
        modBus.addListener((RegisterClientPayloadHandlersEvent event) -> ClientPayloads.registerAll(new ClientPayloads.Registrar() {
            @Override
            public <T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> type, Consumer<T> handler) {
                event.register(type, (payload, context) -> handler.accept(payload));
            }
        }));
        modBus.addListener((RegisterKeyMappingsEvent event) -> {
            event.registerCategory(Keybinds.CATEGORY);
            for (KeyMapping key : Keybinds.ALL) {
                event.register(key);
            }
        });
        modBus.addListener((net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent event) -> TenkaichiClient.registerParticles((type, factory) -> event.registerSpriteSet(type, factory::apply)));
        modBus.addListener((RegisterGuiLayersEvent event) -> event.registerAboveAll(TenkaichiClient.HUD_ID, TenkaichiHud::render));
        modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) -> TenkaichiModels.registerLayers(event::registerLayerDefinition));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> TenkaichiClient.registerRenderers(new TenkaichiClient.RendererRegistrar() {
            @Override
            public <T extends Entity> void register(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
                event.registerEntityRenderer(type, provider);
            }
        }));
        modBus.addListener((EntityRenderersEvent.AddLayers event) -> {
            for (PlayerModelType skin : event.getSkins()) {
                AvatarRenderer<?> renderer = event.getPlayerRenderer(skin);
                if (renderer != null) {
                    for (RenderLayer<AvatarRenderState, PlayerModel> layer : TenkaichiLayers.create(renderer, event.getEntityModels())) {
                        renderer.addLayer(layer);
                    }
                }
            }
        });
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Pre event) -> {
            net.minecraft.resources.Identifier name = event.getName();
            if ((name.equals(net.neoforged.neoforge.client.gui.VanillaGuiLayers.PLAYER_HEALTH) || name.equals(net.neoforged.neoforge.client.gui.VanillaGuiLayers.FOOD_LEVEL)
                    || name.equals(net.neoforged.neoforge.client.gui.VanillaGuiLayers.ARMOR_LEVEL)) && TenkaichiHud.replacesVanillaBars()) {
                event.setCanceled(true);
            }
        });
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Pre event) -> ClientController.startTick(Minecraft.getInstance()));
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> ClientController.onDisconnect());
    }
}
