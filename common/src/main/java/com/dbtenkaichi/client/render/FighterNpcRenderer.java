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
    public boolean shouldRender(FighterNpc entity, net.minecraft.client.renderer.culling.Frustum frustum, double x, double y, double z, float partial) {
        if (entity instanceof com.dbtenkaichi.entity.StoryNpc npc && npc.hidden()) {
            return false;
        }
        return super.shouldRender(entity, frustum, x, y, z, partial);
    }

    @Override
    protected boolean shouldShowName(FighterNpc entity, double distanceSqr) {
        if (com.dbtenkaichi.client.story.ScenePlayer.active()) {
            return false;
        }
        return super.shouldShowName(entity, distanceSqr);
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
        state.showHat = true;
        state.showJacket = true;
        state.showLeftPants = true;
        state.showRightPants = true;
        state.showLeftSleeve = true;
        state.showRightSleeve = true;
        state.showCape = false;
        state.isSpectator = false;
    }

    @Override
    protected void scale(AvatarRenderState state, PoseStack poseStack) {
        super.scale(state, poseStack);
        EntityVisual visual = TenkaichiLayers.visual(state);
        if (visual != null && Math.abs(visual.look().height - 1.0f) > 0.01f) {
            float h = visual.look().height;
            poseStack.scale(h, h, h);
        }
        if (visual != null) {
            float[] widths = {0.93f, 1.0f, 1.09f, 1.2f};
            float width = widths[Math.max(0, Math.min(3, visual.look().build))] * (visual.look().presentation == 1 ? 0.93f : 1.0f);
            float grow = com.dbtenkaichi.client.cinematic.CinematicDirector.growScale(visual);
            if (width != 1.0f || grow != 1.0f) {
                poseStack.scale(width * grow, grow, width * grow);
            }
        }
    }

    @Override
    public Identifier getTextureLocation(AvatarRenderState state) {
        EntityVisual visual = TenkaichiLayers.visual(state);
        if (visual == null) {
            return DefaultPlayerSkin.getDefaultSkin().body().texturePath();
        }
        return CustomSkins.textureFor(visual.race, visual.look(), visual.eyeColor(), visual.hairColor());
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
