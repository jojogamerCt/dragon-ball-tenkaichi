package com.dbtenkaichi.mixin;

import com.dbtenkaichi.movement.FlightSystem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @Shadow
    private boolean clientIsFloating;

    @Shadow
    private int aboveGroundTickCount;

    @Inject(method = "tick", at = @At("HEAD"))
    private void dbtenkaichi$allowKiFlight(CallbackInfo ci) {
        if (this.player != null && FlightSystem.isKiFlying(this.player)) {
            this.clientIsFloating = false;
            this.aboveGroundTickCount = 0;
        }
    }
}
