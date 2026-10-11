package com.dbtenkaichi.client.render;

import com.dbtenkaichi.client.ClientState;
import com.dbtenkaichi.client.EntityVisual;
import com.dbtenkaichi.client.anim.PoseApplier;
import com.dbtenkaichi.entity.FighterNpc;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public class FighterNpcRenderer extends LivingEntityRenderer<FighterNpc, AvatarRenderState, PlayerModel> {
    public FighterNpcRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel(context.bakeLayer(TenkaichiModels.FIGHTER), false), 0.5f);
        for (RenderLayer<AvatarRenderState, PlayerModel> layer : TenkaichiLayers.create(this, context.getModelSet())) {
            this.addLayer(layer);
        }
    }

    @Override
    public AvatarRenderState createRenderState() {
        return new AvatarRenderState();
    }

    @Override
    public void extractRenderState(FighterNpc entity, AvatarRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTicks, this.itemModelResolver);
        state.skin = DefaultPlayerSkin.getDefaultSkin();
        state.id = entity.getId();
        state.showHat = false;
        state.showJacket = false;
        state.showLeftPants = false;
        state.showRightPants = false;
        state.showLeftSleeve = false;
        state.showRightSleeve = false;
        state.showCape = false;
        state.isSpectator = false;
    }

    @Override
    public Identifier getTextureLocation(AvatarRenderState state) {
        EntityVisual visual = TenkaichiLayers.visual(state);
        if (visual == null) {
            return DefaultPlayerSkin.getDefaultSkin().body().texturePath();
        }
        return CustomSkins.textureFor(visual.appearance, visual.eyeColor(), visual.hairColor());
    }

    @Override
    protected void setupRotations(AvatarRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        Minecraft mc = Minecraft.getInstance();
        Entity entity = mc.level == null ? null : mc.level.getEntity(state.id);
        if (entity != null) {
            PoseApplier.applyBodyTransform(poseStack, entity, ClientState.forEntity(entity), bodyRot, entity.getBbHeight());
        }
    }
}
