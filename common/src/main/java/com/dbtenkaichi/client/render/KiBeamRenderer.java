package com.dbtenkaichi.client.render;

import com.dbtenkaichi.config.ConfigManager;
import com.dbtenkaichi.entity.KiBeamEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class KiBeamRenderer extends EntityRenderer<KiBeamEntity, KiBeamRenderer.State> {
    public KiBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(KiBeamEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.length = entity.clientLengthO + (entity.clientLength - entity.clientLengthO) * partialTicks;
        state.radius = entity.radius();
        state.direction = entity.direction();
        state.core = entity.coreColor();
        state.glow = entity.glowColor();
        state.beamState = entity.state();
        state.time = entity.tickCount + partialTicks;
        state.spiral = com.dbtenkaichi.ki.SkillStyle.of(entity.techniqueId()) == com.dbtenkaichi.ki.SkillStyle.SPECIAL_BEAM_CANNON;
    }

    @Override
    protected AABB getBoundingBoxForCulling(KiBeamEntity entity, float partialTicks) {
        Vec3 start = entity.position();
        Vec3 end = start.add(entity.direction().scale(Math.max(entity.clientLength, entity.length())));
        return new AABB(start, end).inflate(entity.radius() * 3.0 + 1.0);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        try {
            float fade = state.beamState == KiBeamEntity.STATE_FADING ? 0.45f : 1.0f;
            boolean clashing = state.beamState == KiBeamEntity.STATE_CLASHING;
            int quality = ConfigManager.client().quality().auraLayers;
            collector.submitCustomGeometry(poseStack, RenderTypes.lightning(),
                    (pose, buffer) -> {
                        EnergyMesh.beam(pose.pose(), buffer, state.direction, state.length, state.spiral ? state.radius * 0.4f : state.radius, state.core, state.glow, state.time, fade, clashing, quality);
                        if (state.spiral) {
                            EnergyMesh.spiral(pose.pose(), buffer, state.direction, state.length, state.radius, state.glow, state.time, fade);
                        }
                    });
        } catch (RuntimeException e) {
            RenderGuard.report("ki_beam", e);
        }
        super.submit(state, poseStack, collector, camera);
    }

    public static class State extends EntityRenderState {
        public float length;
        public float radius;
        public boolean spiral;
        public Vec3 direction = new Vec3(0, 0, 1);
        public int core;
        public int glow;
        public byte beamState;
        public float time;
    }
}
