package com.dbtenkaichi.neoforge;

import com.dbtenkaichi.Tenkaichi;
import com.dbtenkaichi.platform.IPlatform;
import com.dbtenkaichi.player.PlayerData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.nio.file.Path;
import java.util.function.Supplier;

public class NeoForgePlatform implements IPlatform {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Tenkaichi.MOD_ID);
    public static final Supplier<AttachmentType<PlayerData>> PLAYER_DATA = ATTACHMENTS.register("player_data",
            () -> AttachmentType.builder(PlayerData::new).serialize(PlayerData.CODEC.fieldOf("data")).copyOnDeath().build());

    @Override
    public String loaderName() {
        return "NeoForge";
    }

    @Override
    public Path configDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopment() {
        return !FMLEnvironment.isProduction();
    }

    @Override
    public PlayerData playerData(Player player) {
        return player.getData(PLAYER_DATA);
    }

    @Override
    public boolean canBreakBlock(ServerPlayer player, BlockPos pos, BlockState state) {
        BreakBlockEvent event = new BreakBlockEvent(player.level(), pos, state, player);
        NeoForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }
}
