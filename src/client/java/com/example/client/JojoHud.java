package com.example.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import net.minecraft.server.packs.resources.Resource;

public class JojoHud {
    public static boolean enabled = false;
    private static Identifier location = null;
    private static DynamicTexture texture = null;
    private static int imgW = 1;
    private static int imgH = 1;
    private static boolean tried = false;

    public static void render(GuiGraphics gui, Minecraft client) {
        ensureTexture(client);
        int[] pos = HudPos.get("jojo", client.getWindow().getGuiScaledWidth() - 120, 60);
        int px = pos[0];
        int py = pos[1];
        int w = 108;
        int h = Math.max(40, Math.min(280, (int) (w * (imgH / (float) Math.max(1, imgW)))));

        if (location != null) {
            gui.blit(location, px, py, w, h, 0.0f, 0.0f, imgW, imgH);
            HudDrag.setArea("jojo", px, py, w, h);
        } else {
            Ui.panel(gui, px, py, w, 120, 0xC00B0F1A, Ui.PULSE_ACCENT);
            HudDrag.setArea("jojo", px, py, w, 120);
            gui.drawCenteredString(client.font, "ДЖОДЖО", px + w / 2, py + 46, 0xFFFFAA00);
            gui.drawCenteredString(client.font, "положи sprite", px + w / 2, py + 62, 0xFF9A9A9A);
            gui.drawCenteredString(client.font, "config/flash-visual/jojo.png", px + w / 2, py + 74, 0xFF6A6A6A);
        }
    }

    private static void ensureTexture(Minecraft client) {
        if (tried) {
            return;
        }
        tried = true;
        NativeImage img = null;

        java.nio.file.Path cfgPath = client.gameDirectory.toPath()
            .resolve("config").resolve("flash-visual").resolve("jojo.png");
        if (Files.exists(cfgPath)) {
            try (InputStream in = Files.newInputStream(cfgPath)) {
                img = NativeImage.read(in);
            } catch (IOException | RuntimeException ignored) {
            }
        }

        if (img == null) {
            try {
                List<Resource> bundled = client.getResourceManager()
                    .getResourceStack(Identifier.fromNamespaceAndPath("flash-visual", "textures/jojo.png"));
                if (!bundled.isEmpty()) {
                    try (InputStream in = bundled.get(bundled.size() - 1).open()) {
                        img = NativeImage.read(in);
                    }
                }
            } catch (IOException | RuntimeException ignored) {
            }
        }

        if (img == null) {
            return;
        }
        imgW = img.getWidth();
        imgH = img.getHeight();
        location = Identifier.fromNamespaceAndPath("flashvisual", "jojo");
        texture = new DynamicTexture(() -> "flashvisual/jojo", img);
        client.getTextureManager().register(location, texture);
    }
}
