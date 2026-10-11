package com.dbtenkaichi.item;

import com.dbtenkaichi.player.PlayerData;
import com.dbtenkaichi.server.PlayerManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SenzuBeanItem extends Item {
    public SenzuBeanItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide()) {
            entity.setHealth(entity.getMaxHealth());
            entity.clearFire();
            if (entity instanceof ServerPlayer player) {
                PlayerData data = PlayerManager.data(player);
                data.setKi(data.runtime().cachedMaxKi);
                data.setStamina(data.runtime().cachedMaxStamina);
                data.setFatigue(data.fatigue() * 0.5);
                data.runtime().exhaustedUntil = 0;
                data.runtime().vitalsDirty = true;
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
