package com.example.client;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class FreeLook {
    public static double freeYaw = 0;
    public static double freePitch = 0;

    public static boolean isActive() {
        Minecraft client = Minecraft.getInstance();
        if (client.getWindow() == null || client.player == null || client.level == null) {
            return false;
        }
        if (client.screen != null) {
            return false;
        }
        long handle = client.getWindow().handle();
        return GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS;
    }

    public static void addDelta(double accumulatedDX, double accumulatedDY) {
        Minecraft client = Minecraft.getInstance();
        double sens = client.options.sensitivity().get();
        double k = (0.6 * sens + 0.2);
        k = k * k * k;
        k = k * 8.0;
        double yaw = accumulatedDX * k;
        double pitch = accumulatedDY * k;
        if (client.options.invertMouseX().get()) {
            yaw = -yaw;
        }
        if (client.options.invertMouseY().get()) {
            pitch = -pitch;
        }
        freeYaw += yaw;
        freePitch += pitch;
        if (freePitch > 90) {
            freePitch = 90;
        }
        if (freePitch < -90) {
            freePitch = -90;
        }
    }

    public static float yawOffset() {
        return (float) freeYaw;
    }

    public static float pitchOffset() {
        return (float) freePitch;
    }
}