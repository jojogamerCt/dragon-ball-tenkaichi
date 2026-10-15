package com.dbtenkaichi.item;

import com.dbtenkaichi.player.Appearance;
import com.dbtenkaichi.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.DyedItemColor;

public final class Outfits {
    public static final int TOP = 0;
    public static final int PANTS = 1;
    public static final int BOOTS = 2;
    public static final String SECONDARY = "dbtenkaichi_secondary";
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private Outfits() {
    }

    public static Item item(int style, int piece) {
        return switch (style * 3 + piece) {
            case 0 -> ModItems.GI_TOP.get();
            case 1 -> ModItems.GI_PANTS.get();
            case 2 -> ModItems.GI_BOOTS.get();
            case 3 -> ModItems.BATTLE_ARMOR_TOP.get();
            case 4 -> ModItems.BATTLE_ARMOR_PANTS.get();
            case 5 -> ModItems.BATTLE_ARMOR_BOOTS.get();
            case 6 -> ModItems.CASUAL_TOP.get();
            case 7 -> ModItems.CASUAL_PANTS.get();
            case 8 -> ModItems.CASUAL_BOOTS.get();
            default -> null;
        };
    }

    public static int style(ItemStack stack, int piece) {
        if (stack == null || stack.isEmpty()) {
            return -1;
        }
        for (int style = 0; style < Appearance.OUTFIT_NATURAL; style++) {
            if (stack.is(item(style, piece))) {
                return style;
            }
        }
        return -1;
    }

    public static int primary(ItemStack stack, int fallback) {
        return DyedItemColor.getOrDefault(stack, fallback) & 0xFFFFFF;
    }

    public static int secondary(ItemStack stack, int fallback) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? fallback : data.copyTag().getIntOr(SECONDARY, fallback) & 0xFFFFFF;
    }

    public static ItemStack create(int style, int piece, int primary, int secondary) {
        ItemStack stack = new ItemStack(item(style, piece));
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(primary & 0xFFFFFF));
        CompoundTag tag = new CompoundTag();
        tag.putInt(SECONDARY, secondary & 0xFFFFFF);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    public static void equip(ServerPlayer player, Appearance appearance) {
        int style = appearance.outfit;
        if (style < 0 || style >= Appearance.OUTFIT_NATURAL) {
            return;
        }
        for (int piece = TOP; piece <= BOOTS; piece++) {
            ItemStack stack = create(style, piece, appearance.outfitPrimary, appearance.outfitSecondary);
            EquipmentSlot slot = SLOTS[piece];
            if (player.getItemBySlot(slot).isEmpty()) {
                player.setItemSlot(slot, stack);
            } else {
                player.getInventory().placeItemBackInInventory(stack, net.minecraft.util.Prediction.SERVER_ONLY);
            }
        }
    }
}
