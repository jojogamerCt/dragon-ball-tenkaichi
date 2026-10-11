package com.dbtenkaichi.fabric;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.platform.IPlatform;
import com.dbtenkaichi.player.PlayerData;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.file.Path;

public class FabricPlatform implements IPlatform {
    public static final AttachmentType<PlayerData> PLAYER_DATA = AttachmentRegistry.create(Tenkaichi.id("player_data"),
            builder -> builder.persistent(PlayerData.CODEC).initializer(PlayerData::new).copyOnDeath());

    @Override
    public String loaderName() {
        return "Fabric";
    }

    @Override
    public Path configDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public PlayerData playerData(Player player) {
        return player.getAttachedOrCreate(PLAYER_DATA);
    }

    @Override
    public boolean canBreakBlock(ServerPlayer player, BlockPos pos, BlockState state) {
        return PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(player.level(), player, pos, state, player.level().getBlockEntity(pos));
    }
}
