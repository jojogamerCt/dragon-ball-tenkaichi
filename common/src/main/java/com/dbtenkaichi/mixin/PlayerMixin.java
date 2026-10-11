package com.dbtenkaichi.mixin;

import com.dbtenkaichi.movement.FlightSystem;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "getDesiredPose", at = @At("HEAD"), cancellable = true)
    private void dbtenkaichi$noCrouchWhileFlying(CallbackInfoReturnable<Pose> cir) {
        Player self = (Player) (Object) this;
        if (!self.isSleeping() && !self.isSwimming() && !self.isFallFlying() && FlightSystem.isKiFlying(self)) {
            cir.setReturnValue(Pose.STANDING);
        }
    }
}
