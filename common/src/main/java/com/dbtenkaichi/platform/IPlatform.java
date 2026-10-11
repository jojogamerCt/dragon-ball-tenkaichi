package com.dbtenkaichi.platform;

import com.dbtenkaichi.player.PlayerData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.file.Path;

public interface IPlatform {
    String loaderName();

    Path configDirectory();

    boolean isModLoaded(String modId);

    boolean isDevelopment();

    PlayerData playerData(Player player);

    boolean canBreakBlock(ServerPlayer player, BlockPos pos, BlockState state);
}
