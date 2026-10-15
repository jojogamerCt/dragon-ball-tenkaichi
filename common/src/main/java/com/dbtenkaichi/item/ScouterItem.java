package com.dbtenkaichi.item;

import com.dbtenkaichi.combat.CombatFxKind;
import com.dbtenkaichi.combat.CombatSystem;
import com.dbtenkaichi.registry.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ScouterItem extends Item {
    public ScouterItem(Properties properties) {
        super(properties);
    }

    public static boolean worn(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ScouterItem;
    }

    public static void overload(ServerPlayer player) {
        if (!worn(player)) {
            return;
        }
        player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        CombatSystem.fx(player, CombatFxKind.KI_IMPACT, player, player, player.getEyePosition(), 0.6f);
        CombatSystem.playSound(player.level(), player, ModSounds.KI_IMPACT.get(), 1.0f, 1.6f);
        player.sendSystemMessage(Component.translatable("message.dbtenkaichi.scouter_overload"));
    }
}
