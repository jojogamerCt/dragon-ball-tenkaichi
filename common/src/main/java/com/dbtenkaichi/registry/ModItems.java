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

    public static final Supplier<Item> GI_TOP = outfit("gi_top", net.minecraft.world.entity.EquipmentSlot.CHEST, 3);
    public static final Supplier<Item> GI_PANTS = outfit("gi_pants", net.minecraft.world.entity.EquipmentSlot.LEGS, 2);
    public static final Supplier<Item> GI_BOOTS = outfit("gi_boots", net.minecraft.world.entity.EquipmentSlot.FEET, 1);
    public static final Supplier<Item> BATTLE_ARMOR_TOP = outfit("battle_armor_top", net.minecraft.world.entity.EquipmentSlot.CHEST, 5);
    public static final Supplier<Item> BATTLE_ARMOR_PANTS = outfit("battle_armor_pants", net.minecraft.world.entity.EquipmentSlot.LEGS, 3);
    public static final Supplier<Item> BATTLE_ARMOR_BOOTS = outfit("battle_armor_boots", net.minecraft.world.entity.EquipmentSlot.FEET, 2);
    public static final Supplier<Item> CASUAL_TOP = outfit("casual_top", net.minecraft.world.entity.EquipmentSlot.CHEST, 1);
    public static final Supplier<Item> CASUAL_PANTS = outfit("casual_pants", net.minecraft.world.entity.EquipmentSlot.LEGS, 1);
    public static final Supplier<Item> CASUAL_BOOTS = outfit("casual_boots", net.minecraft.world.entity.EquipmentSlot.FEET, 1);

    public static final Supplier<CreativeModeTab> TAB = Services.REGISTRY.register(BuiltInRegistries.CREATIVE_MODE_TAB, "main",
            () -> Services.REGISTRY.creativeTabBuilder()
                    .title(Component.translatable("itemGroup.dbtenkaichi.main"))
                    .icon(() -> new ItemStack(SENZU_BEAN.get()))
                    .displayItems((params, output) -> TAB_ITEMS.forEach(item -> output.accept(item.get())))
                    .build());

    private ModItems() {
    }

    private static Supplier<Item> outfit(String name, net.minecraft.world.entity.EquipmentSlot slot, int armor) {
        return register(name, props -> new Item(props.stacksTo(1)
                .component(net.minecraft.core.component.DataComponents.EQUIPPABLE, net.minecraft.world.item.equipment.Equippable.builder(slot)
                        .setEquipSound(net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_LEATHER).build())
                .component(net.minecraft.core.component.DataComponents.DYED_COLOR, new net.minecraft.world.item.component.DyedItemColor(0xF07A1A))
                .attributes(net.minecraft.world.item.component.ItemAttributeModifiers.builder()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR,
                                new net.minecraft.world.entity.ai.attributes.AttributeModifier(Tenkaichi.id("outfit_" + name), armor, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),
                                net.minecraft.world.entity.EquipmentSlotGroup.bySlot(slot))
                        .build())));
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
