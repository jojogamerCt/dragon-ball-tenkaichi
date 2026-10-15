package com.dbtenkaichi.network;

import com.dbtenkaichi.server.ServerPacketHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.function.BiConsumer;

public final class TenkaichiNetwork {
    public static final String VERSION = "1";

    public static final List<Serverbound<?>> SERVERBOUND = List.of(
            new Serverbound<>(Packets.CreateCharacter.TYPE, Packets.CreateCharacter.CODEC, ServerPacketHandler::createCharacter),
            new Serverbound<>(Packets.Action.TYPE, Packets.Action.CODEC, ServerPacketHandler::action),
            new Serverbound<>(Packets.AssignSlot.TYPE, Packets.AssignSlot.CODEC, ServerPacketHandler::assignSlot),
            new Serverbound<>(Packets.TrainingInput.TYPE, Packets.TrainingInput.CODEC, ServerPacketHandler::trainingInput),
            new Serverbound<>(Packets.Request.TYPE, Packets.Request.CODEC, ServerPacketHandler::request)
    );

    public static final List<Clientbound<?>> CLIENTBOUND = List.of(
            new Clientbound<>(Packets.SyncData.TYPE, Packets.SyncData.CODEC),
            new Clientbound<>(Packets.Vitals.TYPE, Packets.Vitals.CODEC),
            new Clientbound<>(Packets.Appearance.TYPE, Packets.Appearance.CODEC),
            new Clientbound<>(Packets.EntityState.TYPE, Packets.EntityState.CODEC),
            new Clientbound<>(Packets.Anim.TYPE, Packets.Anim.CODEC),
            new Clientbound<>(Packets.CombatFx.TYPE, Packets.CombatFx.CODEC),
            new Clientbound<>(Packets.Forms.TYPE, Packets.Forms.CODEC),
            new Clientbound<>(Packets.OpenScreen.TYPE, Packets.OpenScreen.CODEC),
            new Clientbound<>(Packets.Clash.TYPE, Packets.Clash.CODEC),
            new Clientbound<>(Packets.Cinematic.TYPE, Packets.Cinematic.CODEC),
            new Clientbound<>(Packets.ServerConfig.TYPE, Packets.ServerConfig.CODEC),
            new Clientbound<>(Packets.Data.TYPE, Packets.Data.CODEC)
    );

    private TenkaichiNetwork() {
    }

    public record Serverbound<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec,
                                                             BiConsumer<T, ServerPlayer> handler) {
    }

    public record Clientbound<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
    }
}
