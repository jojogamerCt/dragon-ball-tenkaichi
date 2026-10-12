package com.dbtenkaichi.registry;

import com.dbtenkaichi.platform.Services;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ModParticles {
    public static final Map<String, Supplier<ParticleType<ColorParticleOption>>> ALL = new LinkedHashMap<>();

    public static final Supplier<ParticleType<ColorParticleOption>> KI_SPARK = register("ki_spark");
    public static final Supplier<ParticleType<ColorParticleOption>> AURA_FLAME = register("aura_flame");
    public static final Supplier<ParticleType<ColorParticleOption>> ENERGY_ORB = register("energy_orb");
    public static final Supplier<ParticleType<ColorParticleOption>> IMPACT_STAR = register("impact_star");
    public static final Supplier<ParticleType<ColorParticleOption>> SHOCK_RING = register("shock_ring");
    public static final Supplier<ParticleType<ColorParticleOption>> DUST_PUFF = register("dust_puff");
    public static final Supplier<ParticleType<ColorParticleOption>> LIGHTNING = register("lightning");
    public static final Supplier<ParticleType<ColorParticleOption>> SPEED_LINE = register("speed_line");

    private ModParticles() {
    }

    private static Supplier<ParticleType<ColorParticleOption>> register(String name) {
        Supplier<ParticleType<ColorParticleOption>> supplier = Services.REGISTRY.register(BuiltInRegistries.PARTICLE_TYPE, name, ModParticles::colored);
        ALL.put(name, supplier);
        return supplier;
    }

    private static ParticleType<ColorParticleOption> colored() {
        return new ParticleType<>(false) {
            @Override
            public MapCodec<ColorParticleOption> codec() {
                return ColorParticleOption.codec(this);
            }

            @Override
            @SuppressWarnings({"unchecked", "rawtypes"})
            public StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption> streamCodec() {
                return (StreamCodec) ColorParticleOption.streamCodec(this);
            }
        };
    }

    public static ColorParticleOption of(Supplier<ParticleType<ColorParticleOption>> type, int rgb) {
        return ColorParticleOption.create(type.get(), 0xFF000000 | (rgb & 0xFFFFFF));
    }

    public static void init() {
    }
}
