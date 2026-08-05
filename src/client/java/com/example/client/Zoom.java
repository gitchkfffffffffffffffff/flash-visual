package com.example.client;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class Zoom {
    public static boolean enabled = true;
    public static double factor = 0.25;

    public static boolean isHolding() {
        if (!enabled) {
            return false;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.getWindow() == null || client.screen != null) {
            return false;
        }
        int key = Binds.get(Binds.ZOOM);
        if (key < 0) {
            return false;
        }
        return GLFW.glfwGetKey(client.getWindow().handle(), key) == GLFW.GLFW_PRESS;
    }
}