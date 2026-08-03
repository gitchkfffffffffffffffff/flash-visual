package com.example.client;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.Map;

public class HudDrag {
    private static final Map<String, int[]> AREAS = new LinkedHashMap<>();
    private static String active = null;
    private static int grabDX = 0;
    private static int grabDY = 0;
    private static int mx = 0;
    private static int my = 0;
    private static boolean leftDown = false;

    public static void setArea(String name, int x, int y, int w, int h) {
        AREAS.put(name, new int[]{x, y, w, h});
    }

    public static String active() {
        return active;
    }

    public static void beginFrame(Minecraft client) {
        AREAS.clear();
        readPointer(client);
    }

    public static void endFrame(Minecraft client) {
        readPointer(client);
        boolean down = leftDown;
        if (!down) {
            active = null;
            return;
        }
        if (active == null) {
            for (Map.Entry<String, int[]> e : AREAS.entrySet()) {
                int[] r = e.getValue();
                if (mx >= r[0] && mx <= r[0] + r[2] && my >= r[1] && my <= r[1] + r[3]) {
                    active = e.getKey();
                    int[] p = HudPos.get(active, r[0], r[1]);
                    grabDX = mx - p[0];
                    grabDY = my - p[1];
                    break;
                }
            }
        }
        if (active != null) {
            int[] p = HudPos.get(active, 0, 0);
            p[0] = mx - grabDX;
            p[1] = my - grabDY;
        }
    }

    private static void readPointer(Minecraft client) {
        if (client.getWindow() == null || client.player == null) {
            leftDown = false;
            return;
        }
        long handle = client.getWindow().handle();
        double[] px = new double[1];
        double[] py = new double[1];
        GLFW.glfwGetCursorPos(handle, px, py);
        double scale = client.getWindow().getGuiScale();
        mx = (int) (px[0] / scale);
        my = (int) (py[0] / scale);
        leftDown = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
    }
}