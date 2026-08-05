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
        int panelW = PAD + ARMOR_COLS * TILE + 6 + gridW + 6 + TILE + PAD;
        int panelH = PAD * 2 + ROWS * TILE;

        Ui.panel(gui, x, y, panelW, panelH, Ui.PULSE_PANEL, Ui.PULSE_LINE);
        int accent = Ui.PULSE_ACCENT;
        gui.fillGradient(x, y, x + panelW, y + 2, accent, (accent & 0x00FFFFFF));
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
        drawSlot(gui, client.font, inv.getItem(40), gx + GRID_COLS * TILE + 6, gy + (ROWS - 1) * TILE);
    }

    private static void drawSlot(GuiGraphics gui, Font font, ItemStack stack, int sx, int sy) {
        int r = 5;
        Ui.roundRect(gui, sx - 1, sy - 1, SLOT + 2, SLOT + 2, r, Ui.PULSE_LINE);
        Ui.roundRect(gui, sx, sy, SLOT, SLOT, r, 0x8C000000);
        gui.fillGradient(sx, sy, sx + SLOT, sy + 3, 0x22000000, 0x00000000);
        if (!stack.isEmpty()) {
            gui.renderItem(stack, sx + 1, sy + 1);
            gui.renderItemDecorations(font, stack, sx + 1, sy + 1);
        }
        gui.fill(sx + 1, sy + 1, sx + SLOT - 1, sy + 2, 0x1EFFFFFF);
    }
}