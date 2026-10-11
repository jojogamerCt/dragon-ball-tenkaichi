package com.dbtenkaichi.mixin.client;

import com.dbtenkaichi.client.input.ClientController;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Inject(method = "applyInput", at = @At("TAIL"))
    private void dbtenkaichi$lockInput(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        if (ClientController.suppressMovement()) {
            self.xxa = 0.0f;
            self.zza = 0.0f;
            self.setJumping(false);
        } else if (ClientController.flyingLocally()) {
            self.xxa = 0.0f;
            self.zza = 0.0f;
        }
    }
}
