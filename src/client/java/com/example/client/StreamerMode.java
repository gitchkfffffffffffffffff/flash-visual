package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

public class StreamerMode {
    public static boolean enabled = false;
    public static boolean hideNick = true;
    public static boolean hideCoords = true;
    public static boolean hideIp = true;
    public static boolean hideMusic = true;
    public static boolean hideNameTags = true;

    public static String ownNick(Minecraft client) {
        if (!enabled || !hideNick) {
            return null;
        }
        String active = AltManager.getActive();
        if (active != null && !active.isEmpty()) {
            return active;
        }
        try {
            return client.getUser().getName();
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String serverIp(Minecraft client) {
        if (!enabled || !hideIp) {
            return null;
        }
        try {
            if (client.getConnection() != null && client.getConnection().getConnection() != null) {
                String ip = client.getConnection().getConnection().getRemoteAddress().toString();
                String host = ip;
                int slash = ip.lastIndexOf('/');
                if (slash >= 0) {
                    host = ip.substring(slash + 1);
                }
                int colon = host.lastIndexOf(':');
                if (colon > 0) {
                    host = host.substring(0, colon);
                }
                return host;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static String coordsString() {
        if (!enabled || !hideCoords) {
            return null;
        }
        return "???";
    }

    public static boolean isOn() {
        return enabled;
    }

    public static String statusLine() {
        return "Streamer mode: " + (enabled ? "ON" : "OFF")
            + (enabled ? "  · ник/IP/координаты скрыты" : "");
    }
}
