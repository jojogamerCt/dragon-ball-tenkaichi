package com.dbtenkaichi.registry;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.item.SenzuBeanItem;
import com.dbtenkaichi.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.Consumables;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ModItems {
    public static final List<Supplier<? extends Item>> TAB_ITEMS = new ArrayList<>();

    public static final Supplier<Item> SENZU_BEAN = register("senzu_bean", props -> new SenzuBeanItem(props
            .food(new FoodProperties(4, 1.0f, true), Consumables.defaultFood().consumeSeconds(0.6f).build())
            .useCooldown(10.0f)
            .stacksTo(16)
            .rarity(Rarity.RARE)));
    public static final Supplier<Item> MASTER_SPAWN_EGG = register("master_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(ModEntities.MASTER.get())));
    public static final Supplier<Item> RAIDER_SPAWN_EGG = register("saiyan_raider_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(ModEntities.RAIDER.get())));

    public static final Supplier<CreativeModeTab> TAB = Services.REGISTRY.register(BuiltInRegistries.CREATIVE_MODE_TAB, "main",
            () -> Services.REGISTRY.creativeTabBuilder()
                    .title(Component.translatable("itemGroup.dbtenkaichi.main"))
                    .icon(() -> new ItemStack(SENZU_BEAN.get()))
                    .displayItems((params, output) -> TAB_ITEMS.forEach(item -> output.accept(item.get())))
                    .build());

    private ModItems() {
    }

    private static Supplier<Item> register(String name, Function<Item.Properties, Item> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Tenkaichi.MOD_ID, name));
        Supplier<Item> supplier = Services.REGISTRY.register(BuiltInRegistries.ITEM, name, () -> factory.apply(new Item.Properties().setId(key)));
        TAB_ITEMS.add(supplier);
        return supplier;
    }

    public static void init() {
    }
}
