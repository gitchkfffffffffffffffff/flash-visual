package com.example;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public record DupeGivePayload(ItemStack stack, UUID armorStandUuid) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DupeGivePayload> TYPE = new CustomPacketPayload.Type<>(
        Identifier.fromNamespaceAndPath("flash-visual", "give")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, DupeGivePayload> CODEC = StreamCodec.composite(
        ItemStack.OPTIONAL_STREAM_CODEC, DupeGivePayload::stack,
        ByteBufCodecs.STRING_UTF8, payload -> payload.armorStandUuid() == null ? "" : payload.armorStandUuid().toString(),
        (stack, uuidString) -> new DupeGivePayload(stack, uuidString.isEmpty() ? null : UUID.fromString(uuidString))
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
