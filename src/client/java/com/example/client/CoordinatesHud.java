package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class CoordinatesHud {
    public static boolean enabled = false;

    public static void render(GuiGraphics gui, Minecraft client) {
        Player player = client.player;
        if (player == null) {
            return;
        }
        Font font = client.font;
        String text;
        if (StreamerMode.coordsString() != null) {
            text = "XYZ  " + StreamerMode.coordsString() + "  " + StreamerMode.coordsString();
        } else {
            int x = (int) Math.floor(player.getX());
            int y = (int) Math.floor(player.getY());
            int z = (int) Math.floor(player.getZ());
            String facing = facingName(player.getDirection());
            text = "XYZ  " + x + " / " + y + " / " + z + "  " + facing;
        }
        int w = font.width(text) + 14;
        int h = 14;

        int[] pos = HudPos.get("coords", 4, 216);
        int px = pos[0];
        int py = pos[1];
        Ui.panel(gui, px, py, w, h, 0xA0121212, 0x33FFAA00);
        HudDrag.setArea("coords", px, py, w, h);
        gui.drawString(font, Component.literal("XYZ"), px + 5, py + 3, 0xFF9A9A9A);
        int numX = px + 5 + font.width("XYZ") + 3;
        if (StreamerMode.coordsString() != null) {
            gui.drawString(font, Component.literal(StreamerMode.coordsString() + " / " + StreamerMode.coordsString() + " / " + StreamerMode.coordsString()), numX, py + 3, 0xFFCFCFCF);
        } else {
            int x = (int) Math.floor(player.getX());
            int y = (int) Math.floor(player.getY());
            int z = (int) Math.floor(player.getZ());
            gui.drawString(font, Component.literal(x + " / " + y + " / " + z), numX, py + 3, 0xFFCFCFCF);
            String facing = facingName(player.getDirection());
            gui.drawString(font, Component.literal(facing), numX + font.width(x + " / " + y + " / " + z) + 5, py + 3, 0xFF9A9A9A);
        }
    }

    private static String facingName(Direction dir) {
        return switch (dir) {
            case NORTH -> "Север";
            case SOUTH -> "Юг";
            case WEST -> "Запад";
            default -> "Восток";
        };
    }
}