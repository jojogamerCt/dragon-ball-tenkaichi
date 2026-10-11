package com.dbtenkaichi.mixin.client;

import com.dbtenkaichi.client.fx.CameraEffects;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    private float xRot;

    @Shadow
    private float yRot;

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Inject(method = "alignWithEntity", at = @At("TAIL"))
    private void dbtenkaichi$shake(float partialTicks, CallbackInfo ci) {
        float dy = CameraEffects.yawOffset(partialTicks);
        float dx = CameraEffects.pitchOffset(partialTicks);
        if (dy != 0.0f || dx != 0.0f) {
            this.setRotation(this.yRot + dy, this.xRot + dx);
        }
    }
}
