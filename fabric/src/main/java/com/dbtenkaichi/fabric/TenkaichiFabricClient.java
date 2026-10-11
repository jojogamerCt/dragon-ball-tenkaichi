package com.dbtenkaichi.fabric;

import com.dbtenkaichi.client.TenkaichiClient;
import com.dbtenkaichi.client.hud.TenkaichiHud;
import com.dbtenkaichi.client.input.ClientController;
import com.dbtenkaichi.client.input.Keybinds;
import com.dbtenkaichi.client.net.ClientPayloads;
import com.dbtenkaichi.client.render.TenkaichiLayers;
import com.dbtenkaichi.client.render.TenkaichiModels;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.function.Consumer;

public class TenkaichiFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TenkaichiClient.init();
        FabricNetwork.clientSender = ClientPlayNetworking::send;
        ClientPayloads.registerAll(new ClientPayloads.Registrar() {
            @Override
            public <T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> type, Consumer<T> handler) {
                ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> context.client().execute(() -> handler.accept(payload)));
            }
        });
        KeyMapping.Category.register(Keybinds.CATEGORY.id());
        for (KeyMapping key : Keybinds.ALL) {
            KeyMappingHelper.registerKeyMapping(key);
        }
        TenkaichiModels.registerLayers((location, supplier) -> ModelLayerRegistry.registerModelLayer(location, supplier::get));
        TenkaichiClient.registerRenderers(new TenkaichiClient.RendererRegistrar() {
            @Override
            public <T extends Entity> void register(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
                EntityRendererRegistry.register(type, provider);
            }
        });
        LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, renderer, helper, context) -> {
            if (renderer instanceof AvatarRenderer<?> avatar) {
                for (RenderLayer<AvatarRenderState, PlayerModel> layer : TenkaichiLayers.create(avatar, context.getModelSet())) {
                    helper.register(layer);
                }
            }
        });
        HudElementRegistry.addLast(TenkaichiClient.HUD_ID, TenkaichiHud::render);
        ClientTickEvents.START_CLIENT_TICK.register(ClientController::startTick);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(ClientController::onDisconnect));
    }
}
