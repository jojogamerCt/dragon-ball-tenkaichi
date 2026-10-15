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
            PlayerModel model = (PlayerModel) (Object) this;
            if (visual != null) {
                PoseApplier.apply(model, visual, state.ageInTicks);
            }
            if (model.getClass() == PlayerModel.class) {
                boolean show = visual == null || !visual.greatApe();
                model.head.visible = show;
                model.body.visible = show;
                model.rightArm.visible = show;
                model.leftArm.visible = show;
                model.rightLeg.visible = show;
                model.leftLeg.visible = show;
            }
        } catch (RuntimeException e) {
            RenderGuard.report("pose", e);
        }
    }
}
