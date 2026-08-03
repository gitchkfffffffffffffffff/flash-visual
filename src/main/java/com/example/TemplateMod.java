package com.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateMod implements ModInitializer {
	public static final String MOD_ID = "flash-visual";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		DupeCommand.register();

		PayloadTypeRegistry.playS2C().register(DupeHelloPayload.TYPE, DupeHelloPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(DupeGivePayload.TYPE, DupeGivePayload.CODEC);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
			sender.sendPacket(new DupeHelloPayload())
		);

		ServerPlayNetworking.registerGlobalReceiver(DupeGivePayload.TYPE, (payload, context) ->
			context.server().execute(() -> {
				ServerPlayer player = context.player();
				if (player == null) {
					return;
				}
				ItemStack stack = payload.stack().copy();
				if (!stack.isEmpty()) {
					ItemEntity entity = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack);
					player.level().addFreshEntity(entity);
				}
				if (payload.armorStandUuid() != null) {
					Entity stand = player.level().getEntity(payload.armorStandUuid());
					if (stand instanceof ArmorStand) {
						stand.setPos(player.getX(), player.getY(), player.getZ());
					}
				}
				player.sendSystemMessage(Component.literal("Выдано: " + stack.getHoverName().getString() + " x" + stack.getCount()));
			})
		);

		LOGGER.info("Dupe Mod initialized!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
