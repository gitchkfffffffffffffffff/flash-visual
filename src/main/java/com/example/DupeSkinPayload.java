package com.example;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DupeSkinPayload(byte[] png) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DupeSkinPayload> TYPE = new CustomPacketPayload.Type<>(
        Identifier.fromNamespaceAndPath("flash-visual", "skin")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, DupeSkinPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.BYTE_ARRAY, DupeSkinPayload::png,
        DupeSkinPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
