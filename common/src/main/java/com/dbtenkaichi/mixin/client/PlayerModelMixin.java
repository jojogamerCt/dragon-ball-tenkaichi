package com.dbtenkaichi.mixin.client;

import com.dbtenkaichi.client.EntityVisual;
import com.dbtenkaichi.client.anim.PoseApplier;
import com.dbtenkaichi.client.render.RenderGuard;
import com.dbtenkaichi.client.render.TenkaichiLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"))
    private void dbtenkaichi$applyPose(AvatarRenderState state, CallbackInfo ci) {
        try {
            EntityVisual visual = TenkaichiLayers.visual(state);
            if (visual != null) {
                PoseApplier.apply((PlayerModel) (Object) this, visual, state.ageInTicks);
            }
        } catch (RuntimeException e) {
            RenderGuard.report("pose", e);
        }
    }
}
