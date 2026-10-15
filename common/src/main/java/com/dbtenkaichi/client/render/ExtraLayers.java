package com.dbtenkaichi.client.render;

import com.dbtenkaichi.client.EntityVisual;
import com.dbtenkaichi.form.FormDefinition;
import com.dbtenkaichi.player.RuntimeState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;

final class ExtraLayers extends RenderLayer<AvatarRenderState, PlayerModel> {
    private final TenkaichiLayers.HairModel apeFur;
    private final TenkaichiLayers.HairModel apeSkin;
    private final TenkaichiLayers.HairModel apeEyes;
    private final TenkaichiLayers.HairModel ssj4Fur;
    private final TenkaichiLayers.HairModel scouterFrame;
    private final TenkaichiLayers.HairModel scouterLens;
    private final TenkaichiLayers.HairModel halo;
    private final TenkaichiLayers.HairModel tailWrap;
    private final TenkaichiLayers.HairModel[] frieza;

    ExtraLayers(RenderLayerParent<AvatarRenderState, PlayerModel> parent, EntityModelSet set) {
        super(parent);
        this.apeFur = new TenkaichiLayers.HairModel(set.bakeLayer(TenkaichiModels.GREAT_APE_FUR));
        this.apeSkin = new TenkaichiLayers.HairModel(set.bakeLayer(TenkaichiModels.GREAT_APE_SKIN));
        this.apeEyes = new TenkaichiLayers.HairModel(set.bakeLayer(TenkaichiModels.GREAT_APE_EYES));
        this.ssj4Fur = new TenkaichiLayers.HairModel(set.bakeLayer(TenkaichiModels.SSJ4_FUR));
        this.scouterFrame = new TenkaichiLayers.HairModel(set.bakeLayer(TenkaichiModels.SCOUTER_FRAME));
        this.scouterLens = new TenkaichiLayers.HairModel(set.bakeLayer(TenkaichiModels.SCOUTER_LENS));
        this.halo = new TenkaichiLayers.HairModel(set.bakeLayer(TenkaichiModels.HALO));
        this.tailWrap = new TenkaichiLayers.HairModel(set.bakeLayer(TenkaichiModels.TAIL_WRAP));
        this.frieza = new TenkaichiLayers.HairModel[]{
                new TenkaichiLayers.HairModel(set.bakeLayer(TenkaichiModels.FRIEZA_SECOND_HORNS)),
                new TenkaichiLayers.HairModel(set.bakeLayer(TenkaichiModels.FRIEZA_SECOND_DOME)),
                new TenkaichiLayers.HairModel(set.bakeLayer(TenkaichiModels.FRIEZA_THIRD_DOME)),
                new TenkaichiLayers.HairModel(set.bakeLayer(TenkaichiModels.FRIEZA_FINAL_DOME))};
    }

    private static void draw(SubmitNodeCollector collector, PoseStack poseStack, AvatarRenderState state, TenkaichiLayers.HairModel model, int light, int color) {
        collector.submitModel(model, state, poseStack, RenderTypes.entityCutout(TenkaichiLayers.HAIR_TEXTURE), light, OverlayTexture.NO_OVERLAY, 0xFF000000 | color, null, state.outlineColor);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
        try {
            if (state.isInvisible) {
                return;
            }
            EntityVisual visual = TenkaichiLayers.visual(state);
            if (visual == null) {
                return;
            }
            FormDefinition def = visual.formDefinition();
            int feature = def != null && def.visual.featureColor != null ? parse(def.visual.featureColor, 0xB070D0) : 0xB070D0;
            if (visual.greatApe()) {
                draw(collector, poseStack, state, apeFur, light, 0x6A3E1E);
                draw(collector, poseStack, state, apeSkin, light, 0xD8A070);
                draw(collector, poseStack, state, apeEyes, LightCoordsUtil.FULL_BRIGHT, 0xFF2A1A);
                return;
            }
            if ("ssj4".equals(visual.body())) {
                draw(collector, poseStack, state, ssj4Fur, light, 0xB8282A);
            }
            int style = visual.featureStyle();
            if (style == 1) {
                draw(collector, poseStack, state, frieza[0], light, 0xF2EEE6);
                draw(collector, poseStack, state, frieza[1], light, feature);
            } else if (style == 2 || style == 3) {
                draw(collector, poseStack, state, frieza[style], light, feature);
            }
            if (visual.scouter) {
                draw(collector, poseStack, state, scouterFrame, light, 0xE8E8E8);
                draw(collector, poseStack, state, scouterLens, LightCoordsUtil.FULL_BRIGHT, 0x5AF07A);
            }
            if (visual.halo) {
                draw(collector, poseStack, state, halo, LightCoordsUtil.FULL_BRIGHT, 0xFFE860);
            }
            if (visual.tailState == 1) {
                draw(collector, poseStack, state, tailWrap, light, 0x5A3418);
            }
            if (visual.has(RuntimeState.FLAG_BARRIER) || visual.has(RuntimeState.FLAG_PETRIFIED)) {
                boolean stone = visual.has(RuntimeState.FLAG_PETRIFIED);
                float time = state.ageInTicks;
                collector.submitCustomGeometry(poseStack, TenkaichiRenderTypes.aura(),
                        (pose, buffer) -> EnergyMesh.orb(pose.pose(), buffer, new Vec3(0, 1.0, 0), stone ? 0.55f : 0.95f, stone ? 0x8A8A8A : 0xBFF4FF, stone ? 0x6A6A6A : 0x50C8FF, Vec3.ZERO, time));
            }
        } catch (RuntimeException e) {
            RenderGuard.report("extra_layers", e);
        }
    }

    private static int parse(String hex, int fallback) {
        try {
            return Integer.parseInt(hex.replace("#", ""), 16) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
