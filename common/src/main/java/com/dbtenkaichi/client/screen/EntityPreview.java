package com.dbtenkaichi.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class EntityPreview {
    private EntityPreview() {
    }

    public static void draw(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int size, float yaw, float pitch, float heightScale, LivingEntity entity) {
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super LivingEntity, ?> renderer = dispatcher.getRenderer(entity);
        EntityRenderState state = renderer.createRenderState(entity, 1.0f);
        state.shadowPieces.clear();
        state.outlineColor = 0;
        if (state instanceof LivingEntityRenderState living) {
            living.bodyRot = 180.0f + yaw;
            living.yRot = 0.0f;
            living.xRot = 0.0f;
            living.boundingBoxWidth = living.boundingBoxWidth / living.scale;
            living.boundingBoxHeight = living.boundingBoxHeight / living.scale;
            living.scale = heightScale;
        }
        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf xRotation = new Quaternionf().rotateX(pitch * (float) (Math.PI / 180.0));
        rotation.mul(xRotation);
        Vector3f translation = new Vector3f(0.0f, state.boundingBoxHeight * heightScale / 2.0f + 0.0625f, 0.0f);
        g.entity(state, size, translation, rotation, xRotation, x0, y0, x1, y1);
    }
}
