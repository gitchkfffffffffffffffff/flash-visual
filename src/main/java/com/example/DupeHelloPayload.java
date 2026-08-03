package com.example;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DupeHelloPayload() implements CustomPacketPayload {
    public static final Type<DupeHelloPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath("flash-visual", "hello"));
    public static final StreamCodec<ByteBuf, DupeHelloPayload> CODEC = StreamCodec.unit(new DupeHelloPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
