package com.dbtenkaichi.client.render;

import com.dbtenkaichi.entity.PowerBallEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;

public class PowerBallRenderer extends EntityRenderer<PowerBallEntity, PowerBallRenderer.State> {
    public PowerBallRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PowerBallEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.size = entity.size();
        state.time = entity.tickCount + partialTicks;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        try {
            collector.submitCustomGeometry(poseStack, TenkaichiRenderTypes.aura(),
                    (pose, buffer) -> EnergyMesh.orb(pose.pose(), buffer, new Vec3(0, state.size, 0), state.size, 0xFFF4E0, 0xE8F0FF, Vec3.ZERO, state.time));
        } catch (RuntimeException e) {
            RenderGuard.report("power_ball", e);
        }
        super.submit(state, poseStack, collector, camera);
    }

    public static class State extends EntityRenderState {
        public float size;
        public float time;
    }
}
