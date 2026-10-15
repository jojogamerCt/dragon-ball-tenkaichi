package com.dbtenkaichi.mixin;

import com.dbtenkaichi.server.ServerEvents;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @WrapMethod(method = "hurtServer")
    private boolean dbtenkaichi$wrapHurt(ServerLevel level, DamageSource source, float damage, Operation<Boolean> original) {
        LivingEntity self = (LivingEntity) (Object) this;
        float modified = com.dbtenkaichi.form.FormEffects.incoming(self, level, source, damage);
        if (damage > 0 && modified <= 0) {
            return false;
        }
        modified = ServerEvents.modifyIncomingDamage(self, level, source, modified);
        boolean result = original.call(level, source, modified);
        if (result) {
            ServerEvents.afterDamage(self, level, source, modified);
            com.dbtenkaichi.form.FormEffects.after(self, level, source, modified);
        }
        return result;
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void dbtenkaichi$onDie(DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level().isClientSide() && !self.isRemoved()) {
            ServerEvents.onLivingDeath(self, source);
        }
    }
}
