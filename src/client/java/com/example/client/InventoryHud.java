package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class InventoryHud {
    private static final int TILE = 18;
    private static final int SLOT = 16;
    private static final int PAD = 6;
    private static final int ARMOR_COLS = 4;
    private static final int GRID_COLS = 9;
    private static final int ROWS = 4;

    public static void render(GuiGraphics gui, Minecraft client) {
        int[] pos = HudPos.get("inventory", 4, 124);
        int x = pos[0];
        int y = pos[1];
        Inventory inv = client.player.getInventory();

        int gridW = GRID_COLS * TILE;
        int panelW = PAD + ARMOR_COLS * TILE + 6 + gridW + PAD;
        int panelH = PAD * 2 + ROWS * TILE;

        Ui.panel(gui, x, y, panelW, panelH, 0xC00B0F1A, 0xFF1E2A3E);
        HudDrag.setArea("inventory", x, y, panelW, panelH);

        int gx = x + PAD + ARMOR_COLS * TILE + 6;
        int gy = y + PAD;

        int ax = x + PAD;
        int ay = y + PAD;
        EquipmentSlot[] armor = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int i = 0; i < ARMOR_COLS; i++) {
            drawSlot(gui, client.font, inv.getItem(36 + (ARMOR_COLS - 1 - i)), ax, ay + i * TILE);
        }

        for (int r = 0; r < ROWS - 1; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                drawSlot(gui, client.font, inv.getItem(9 + r * GRID_COLS + c), gx + c * TILE, gy + r * TILE);
            }
        }
        for (int c = 0; c < GRID_COLS; c++) {
            drawSlot(gui, client.font, inv.getItem(c), gx + c * TILE, gy + (ROWS - 1) * TILE);
        }
        drawSlot(gui, client.font, inv.getItem(40), gx + GRID_COLS * TILE, gy + (ROWS - 1) * TILE);
    }

    private static void drawSlot(GuiGraphics gui, Font font, ItemStack stack, int sx, int sy) {
        gui.fill(sx, sy, sx + SLOT, sy + SLOT, 0x66111B2E);
        gui.renderOutline(sx, sy, SLOT, SLOT, 0xFF1E2A3E);
        if (!stack.isEmpty()) {
            gui.renderItem(stack, sx, sy);
            gui.renderItemDecorations(font, stack, sx, sy);
        }
    }
}