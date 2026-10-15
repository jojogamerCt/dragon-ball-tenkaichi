package com.dbtenkaichi.client.render;

import com.dbtenkaichi.entity.KiBlastEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;

public class KiBlastRenderer extends EntityRenderer<KiBlastEntity, KiBlastRenderer.State> {
    public KiBlastRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(KiBlastEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.color = entity.color();
        state.size = entity.size();
        state.motion = entity.getDeltaMovement().scale(-1.0);
        state.time = entity.tickCount + partialTicks;
        state.core = entity.coreColor();
        state.shape = entity.shape();
        state.yaw = entity.getYRot();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        try {
            poseStack.pushPose();
            if (state.shape == KiBlastEntity.SHAPE_DISC) {
                poseStack.translate(0.0, state.size, 0.0);
                poseStack.scale(1.6f, 0.22f, 1.6f);
                poseStack.translate(0.0, -state.size, 0.0);
            }
            Vec3 trail = state.shape == KiBlastEntity.SHAPE_LASER ? state.motion.scale(2.5) : state.motion;
            collector.submitCustomGeometry(poseStack, TenkaichiRenderTypes.aura(),
                    (pose, buffer) -> EnergyMesh.orb(pose.pose(), buffer, new Vec3(0, state.size, 0), state.size, state.core, state.color, trail, state.time));
            poseStack.popPose();
        } catch (RuntimeException e) {
            RenderGuard.report("ki_blast", e);
        }
        super.submit(state, poseStack, collector, camera);
    }

    public static class State extends EntityRenderState {
        public int color;
        public float size;
        public Vec3 motion = Vec3.ZERO;
        public float time;
        public int core = 0xFFFFFF;
        public int shape;
        public float yaw;
    }
}
