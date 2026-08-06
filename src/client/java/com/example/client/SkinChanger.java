package com.example.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class SkinChanger {
	private static final Logger LOGGER = LoggerFactory.getLogger("flash-visual-skin");
	public static final Identifier SKIN_ID = Identifier.fromNamespaceAndPath("flash-visual", "custom_skin");

	private static PlayerSkin cached;
	private static boolean tried = false;

	public static PlayerSkin getCustomSkin() {
		if (!tried) {
			tried = true;
			Path path = findSkinFile();
			if (path != null) {
				load(Minecraft.getInstance(), path);
			} else {
				loadDefaultSkin(Minecraft.getInstance());
			}
		}
		return cached;
	}

	private static void loadDefaultSkin(Minecraft mc) {
		InputStream in = SkinChanger.class.getResourceAsStream("/assets/flash-visual/textures/skin.png");
		if (in != null) {
			load(mc, in);
		}
	}

	private static void load(Minecraft mc, Path path) {
		try (InputStream in = Files.newInputStream(path)) {
			load(mc, in);
		} catch (IOException | RuntimeException e) {
			LOGGER.warn("Failed to load custom skin from {}", path, e);
		}
	}

	private static void load(Minecraft mc, InputStream in) {
		try {
			NativeImage image = NativeImage.read(in);
			DynamicTexture texture = new DynamicTexture(() -> "flash-visual custom skin", image);
			mc.getTextureManager().register(SKIN_ID, texture);
			ClientAsset.Texture body = new ClientAsset.ResourceTexture(SKIN_ID);
			PlayerSkin base = DefaultPlayerSkin.getDefaultSkin();
			cached = PlayerSkin.insecure(body, base.cape(), base.elytra(), base.model());
		} catch (IOException | RuntimeException e) {
			LOGGER.warn("Failed to load custom skin resource", e);
		}
	}

	private static Path findSkinFile() {
		Path config = FabricLoader.getInstance().getConfigDir().resolve("flash-visual/skin.png");
		if (Files.isRegularFile(config)) {
			return config;
		}
		Path game = FabricLoader.getInstance().getGameDir().resolve("flash-visual/skin.png");
		if (Files.isRegularFile(game)) {
			return game;
		}
		return null;
	}
}
