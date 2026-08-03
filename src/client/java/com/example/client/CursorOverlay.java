package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

public class CursorOverlay {
    public static boolean enabled = false;

    public static void render(GuiGraphics gui) {
        Minecraft client = Minecraft.getInstance();
        if (!enabled || client.getWindow() == null) {
            return;
        }
        var window = client.getWindow();
        long handle = window.handle();
        double[] mx = new double[1];
        double[] my = new double[1];
        GLFW.glfwGetCursorPos(handle, mx, my);
        double scale = window.getGuiScale();
        int x = (int) Math.round(mx[0] / scale);
        int y = (int) Math.round(my[0] / scale);
        GLFW.glfwSetInputMode(handle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
        draw(gui, x, y);
    }

    private static void draw(GuiGraphics gui, int x, int y) {
        drawDiamond(gui, x, y, 7, 0x3800CFFF);
        drawDiamond(gui, x, y, 5, 0xFFFFAA00);
        drawDiamond(gui, x, y, 3, 0xFFFFD24A);
        drawDiamond(gui, x, y, 1, 0xFFFFFFFF);
    }

    private static void drawDiamond(GuiGraphics gui, int x, int y, int half, int color) {
        for (int dy = -half; dy <= half; dy++) {
            int hw = half - Math.abs(dy);
            if (hw < 0) {
                continue;
            }
            gui.fill(x - hw, y + dy, x + hw + 1, y + dy + 1, color);
        }
    }

    public static void restore() {
        Minecraft client = Minecraft.getInstance();
        if (client.getWindow() == null) {
            return;
        }
        long handle = client.getWindow().handle();
        GLFW.glfwSetInputMode(handle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
    }
}
