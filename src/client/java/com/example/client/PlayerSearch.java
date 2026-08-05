package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlayerSearch {
    public static boolean enabled = false;
    public static String query = "";
    public static int minLength = 3;

    private static long cacheUntil = 0;
    private static final List<PlayerInfo> results = new ArrayList<>();

    public static void setQuery(String q) {
        query = q == null ? "" : q.trim();
        results.clear();
    }

    public static int resultCount() {
        return results.size();
    }

    public static void refresh(Minecraft client) {
        results.clear();
        if (client.getConnection() == null || query.isEmpty()) {
            return;
        }
        String q = query.toLowerCase();
        List<PlayerInfo> all = new ArrayList<>(client.getConnection().getOnlinePlayers());
        all.sort(Comparator.comparing(p -> p.getProfile().name()));
        for (PlayerInfo pi : all) {
            String name = pi.getProfile().name();
            if (name.toLowerCase().contains(q)) {
                results.add(pi);
            }
        }
    }

    public static void tick(Minecraft client) {
        if (!enabled || query.length() < minLength) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now > cacheUntil) {
            cacheUntil = now + 1000;
            refresh(client);
        }
    }

    public static void render(GuiGraphics gui, Minecraft client) {
        if (!enabled || query.length() < minLength) {
            return;
        }
        Font font = client.font;
        int w = 170;
        int rowH = 14;
        int max = 12;
        int n = Math.min(results.size(), max);
        if (n == 0) {
            return;
        }
        int panelH = n * rowH + 26;

        int[] pos = HudPos.get("playersearch", 6, 240);
        int x = pos[0];
        int y = pos[1];
        Ui.panel(gui, x, y, w, panelH, Ui.PULSE_PANEL, Ui.PULSE_LINE);
        HudDrag.setArea("playersearch", x, y, w, panelH);

        gui.fill(x, y, x + w, y + 1, Ui.PULSE_ACCENT);
        String head = "ПОИСК: " + query + " (" + results.size() + ")";
        gui.drawString(font, Component.literal(font.plainSubstrByWidth(head, w - 10)), x + 4, y + 3, Ui.PULSE_ACCENT);

        int ry = y + 15;
        for (int i = 0; i < n; i++) {
            PlayerInfo pi = results.get(i);
            String name = pi.getProfile().name();
            gui.drawString(font, Component.literal(name), x + 4, ry, 0xFFFFFFFF);
            int ping = pi.getLatency();
            String ps = ping < 0 ? "?" : ping + "ms";
            int pc = ping < 50 ? Ui.GREEN : (ping < 150 ? 0xFFFFAA00 : Ui.RED);
            gui.drawString(font, Component.literal(ps), x + w - font.width(ps) - 8, ry, pc);
            ry += rowH;
        }
    }
}
