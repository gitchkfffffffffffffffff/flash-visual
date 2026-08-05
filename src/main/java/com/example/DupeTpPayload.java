package com.example;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DupeTpPayload(double x, double y, double z, String nick) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DupeTpPayload> TYPE = new CustomPacketPayload.Type<>(
        Identifier.fromNamespaceAndPath("flash-visual", "tp")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, DupeTpPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.DOUBLE, DupeTpPayload::x,
        ByteBufCodecs.DOUBLE, DupeTpPayload::y,
        ByteBufCodecs.DOUBLE, DupeTpPayload::z,
        ByteBufCodecs.STRING_UTF8, DupeTpPayload::nick,
        DupeTpPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}