package com.example.client;

import net.minecraft.client.Minecraft;

public class Fullbright {
    public static boolean enabled = false;
    private static double savedGamma = 1.0;

    public static void toggle(Minecraft client) {
        enabled = !enabled;
        if (client.options == null) {
            return;
        }
        if (enabled) {
            savedGamma = client.options.gamma().get();
            client.options.gamma().set(15.0);
        } else {
            client.options.gamma().set(savedGamma);
        }
    }
}
