package com.dbtenkaichi.registry;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.entity.KiBeamEntity;
import com.dbtenkaichi.entity.KiBlastEntity;
import com.dbtenkaichi.entity.MasterEntity;
import com.dbtenkaichi.entity.RaiderEntity;
import com.dbtenkaichi.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ModEntities {
    public static final Map<Supplier<? extends EntityType<? extends LivingEntity>>, Supplier<AttributeSupplier.Builder>> ATTRIBUTES = new LinkedHashMap<>();

    public static final Supplier<EntityType<KiBeamEntity>> KI_BEAM = register("ki_beam",
            EntityType.Builder.<KiBeamEntity>of(KiBeamEntity::new, MobCategory.MISC).sized(0.5f, 0.5f).noLootTable().noSave().clientTrackingRange(16).updateInterval(1));
    public static final Supplier<EntityType<KiBlastEntity>> KI_BLAST = register("ki_blast",
            EntityType.Builder.<KiBlastEntity>of(KiBlastEntity::new, MobCategory.MISC).sized(0.4f, 0.4f).noLootTable().noSave().clientTrackingRange(8).updateInterval(2));
    public static final Supplier<EntityType<MasterEntity>> MASTER = register("master",
            EntityType.Builder.<MasterEntity>of(MasterEntity::new, MobCategory.MISC).sized(0.6f, 1.8f).eyeHeight(1.62f).clientTrackingRange(10));
    public static final Supplier<EntityType<RaiderEntity>> RAIDER = register("saiyan_raider",
            EntityType.Builder.<RaiderEntity>of(RaiderEntity::new, MobCategory.MONSTER).sized(0.6f, 1.9f).eyeHeight(1.7f).clientTrackingRange(10));

    static {
        ATTRIBUTES.put(MASTER, MasterEntity::createAttributes);
        ATTRIBUTES.put(RAIDER, RaiderEntity::createAttributes);
    }

    private ModEntities() {
    }

    private static <T extends net.minecraft.world.entity.Entity> Supplier<EntityType<T>> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Tenkaichi.MOD_ID, name));
        return Services.REGISTRY.register(BuiltInRegistries.ENTITY_TYPE, name, () -> builder.build(key));
    }

    public static void init() {
    }
}
