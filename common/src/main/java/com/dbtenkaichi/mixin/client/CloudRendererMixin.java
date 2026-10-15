package com.dbtenkaichi.mixin.client;

import com.dbtenkaichi.client.cinematic.CinematicDirector;
import net.minecraft.client.renderer.CloudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CloudRenderer.class)
public class CloudRendererMixin {
    @ModifyVariable(method = "prepare(ILnet/minecraft/client/CloudStatus;FILnet/minecraft/world/phys/Vec3;JF)V", at = @At("HEAD"), argsOnly = true)
    private long dbtenkaichi$cloudRush(long gameTime) {
        return gameTime + CinematicDirector.cloudOffset();
    }
}
