package com.dbtenkaichi.mixin.client;

import com.dbtenkaichi.client.EntityVisual;
import com.dbtenkaichi.client.anim.PoseApplier;
import com.dbtenkaichi.client.render.CustomSkins;
import com.dbtenkaichi.client.render.RenderGuard;
import com.dbtenkaichi.client.render.TenkaichiLayers;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
    private static final float[] DBTENKAICHI_BUILD_WIDTH = {0.93f, 1.0f, 1.09f};

    @Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Lnet/minecraft/resources/Identifier;", at = @At("HEAD"), cancellable = true)
    private void dbtenkaichi$customSkin(AvatarRenderState state, CallbackInfoReturnable<Identifier> cir) {
        try {
            EntityVisual visual = TenkaichiLayers.visual(state);
            if (visual != null && !visual.appearance.useOwnSkin) {
                cir.setReturnValue(CustomSkins.textureFor(visual.race, visual.appearance, visual.eyeColor(), visual.hairColor()));
            }
        } catch (RuntimeException e) {
            RenderGuard.report("skin", e);
        }
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private void dbtenkaichi$outfitLayers(net.minecraft.world.entity.Avatar avatar, AvatarRenderState state, float partial, CallbackInfo ci) {
        try {
            EntityVisual visual = TenkaichiLayers.visual(state);
            if (visual != null && !visual.appearance.useOwnSkin) {
                state.showHat = true;
                state.showJacket = true;
                state.showLeftPants = true;
                state.showRightPants = true;
                state.showLeftSleeve = true;
                state.showRightSleeve = true;
            }
        } catch (RuntimeException e) {
            RenderGuard.report("outfit", e);
        }
    }

    @Inject(method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V", at = @At("TAIL"))
    private void dbtenkaichi$bodyTransform(AvatarRenderState state, PoseStack poseStack, float bodyRot, float entityScale, CallbackInfo ci) {
        try {
            Minecraft mc = Minecraft.getInstance();
            Entity entity = mc.level == null ? null : mc.level.getEntity(state.id);
            if (entity == null || state.isFallFlying || state.swimAmount > 0.0f) {
                return;
            }
            EntityVisual visual = TenkaichiLayers.visual(state);
            if (visual != null) {
                PoseApplier.applyBodyTransform(poseStack, entity, visual, bodyRot, entity.getBbHeight() / Math.max(0.01f, entityScale));
            }
        } catch (RuntimeException e) {
            RenderGuard.report("body", e);
        }
    }

    @Inject(method = "scale(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At("TAIL"))
    private void dbtenkaichi$build(AvatarRenderState state, PoseStack poseStack, CallbackInfo ci) {
        EntityVisual visual = TenkaichiLayers.visual(state);
        if (visual != null) {
            float width = DBTENKAICHI_BUILD_WIDTH[Math.max(0, Math.min(2, visual.appearance.build))];
            if (width != 1.0f) {
                poseStack.scale(width, 1.0f, width);
            }
        }
    }
}
