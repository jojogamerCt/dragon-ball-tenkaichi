package com.dbtenkaichi.mixin;

import com.dbtenkaichi.world.SafeZones;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Inject(method = "mayInteract", at = @At("HEAD"), cancellable = true)
    private void dbtenkaichi$protectLandmarks(Entity entity, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (SafeZones.blocksPlayer((ServerLevel) (Object) this, entity, pos)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void dbtenkaichi$safeZoneSpawns(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (SafeZones.deniesSpawn((ServerLevel) (Object) this, entity)) {
            cir.setReturnValue(false);
        }
    }
}
