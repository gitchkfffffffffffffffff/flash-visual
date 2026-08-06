package com.example;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class TemplateMod implements ModInitializer {
	public static final String MOD_ID = "flash-visual";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		DupeCommand.register();

		PayloadTypeRegistry.playS2C().register(DupeHelloPayload.TYPE, DupeHelloPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(DupeGivePayload.TYPE, DupeGivePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(DupeTpPayload.TYPE, DupeTpPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(DupeSkinPayload.TYPE, DupeSkinPayload.CODEC);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			sender.sendPacket(new DupeHelloPayload());
			ServerPlayer joiner = handler.getPlayer();
			for (ServerPlayer p : server.getPlayerList().getPlayers()) {
				if (SkinServer.hasSkin(p.getUUID())) {
					joiner.connection.send(new ClientboundPlayerInfoUpdatePacket(
						ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, p));
				}
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(DupeSkinPayload.TYPE, (payload, context) ->
			context.server().execute(() -> {
				ServerPlayer player = context.player();
				if (player == null) {
					return;
				}
				if (payload.png() == null || payload.png().length == 0) {
					return;
				}
				SkinServer.setSkin(player.getUUID(), payload.png());
				addTexturesProperty(player);
				context.server().getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(
					ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, player));
			})
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

		ServerPlayNetworking.registerGlobalReceiver(DupeTpPayload.TYPE, (payload, context) ->
			context.server().execute(() -> {
				ServerPlayer source = context.player();
				if (source == null) {
					return;
				}
				ServerPlayer target;
				String nick = payload.nick().trim().toLowerCase();
				if (nick.isEmpty()) {
					target = source;
				} else {
					ServerPlayer match = null;
					for (ServerPlayer p : context.server().getPlayerList().getPlayers()) {
						if (p.getName().getString().toLowerCase().equals(nick) || p.getName().getString().toLowerCase().startsWith(nick)) {
							match = p;
							break;
						}
					}
					target = match;
				}
				if (target == null) {
					source.sendSystemMessage(Component.literal("Игрок '" + nick + "' не найден"));
					return;
				}
				Vec3 pos = new Vec3(payload.x(), payload.y(), payload.z());
				source.connection.teleport(pos.x, pos.y, pos.z, source.getYRot(), source.getXRot());
				source.sendSystemMessage(Component.literal("ТП выполнено"));
			})
		);

		LOGGER.info("Dupe Mod initialized!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	private static void addTexturesProperty(ServerPlayer player) {
		if (player == null) {
			return;
		}
		try {
			GameProfile profile = player.getGameProfile();
			String url = SkinServer.skinUrl(player.getUUID());
			if (url == null) {
				return;
			}
			String json = "{\"timestamp\":" + System.currentTimeMillis()
				+ ",\"profileId\":\"" + player.getUUID()
				+ "\",\"profileName\":\"" + profile.name()
				+ "\",\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}";
			String base64 = java.util.Base64.getEncoder().encodeToString(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
			profile.properties().removeAll("textures");
			profile.properties().put("textures", new Property("textures", base64, null));
		} catch (Exception e) {
			LOGGER.warn("Failed to spoof skin for {}", player.getName().getString(), e);
		}
	}
}
