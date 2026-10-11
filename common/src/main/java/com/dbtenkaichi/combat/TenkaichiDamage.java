package com.dbtenkaichi.combat;

import com.dbtenkaichi.Tenkaichi;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public final class TenkaichiDamage {
    public static final ResourceKey<DamageType> MARTIAL_ARTS = key("martial_arts");
    public static final ResourceKey<DamageType> KI_BLAST = key("ki_blast");
    public static final ResourceKey<DamageType> KI_BEAM = key("ki_beam");
    public static final ResourceKey<DamageType> KI_EXPLOSION = key("ki_explosion");

    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    private TenkaichiDamage() {
    }

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(Tenkaichi.MOD_ID, name));
    }

    public static DamageSource source(ServerLevel level, ResourceKey<DamageType> type, @Nullable Entity direct, @Nullable Entity causing) {
        return new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(type), direct, causing);
    }

    public static boolean isTenkaichi(DamageSource source) {
        return source.is(MARTIAL_ARTS) || source.is(KI_BLAST) || source.is(KI_BEAM) || source.is(KI_EXPLOSION);
    }

    public static boolean isKi(DamageSource source) {
        return source.is(KI_BLAST) || source.is(KI_BEAM) || source.is(KI_EXPLOSION);
    }

    public static boolean applyingDirect() {
        return DEPTH.get() > 0;
    }

    public static boolean hurt(LivingEntity target, ServerLevel level, DamageSource source, float amount) {
        DEPTH.set(DEPTH.get() + 1);
        try {
            return target.hurtServer(level, source, amount);
        } finally {
            DEPTH.set(DEPTH.get() - 1);
        }
    }
}
