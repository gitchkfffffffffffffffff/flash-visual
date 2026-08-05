package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StaffHud {
    public static boolean enabled = false;
    public static final Set<String> STAFF = new HashSet<>();

    private static long cacheUntil = 0;
    private static final List<PlayerInfo> cached = new ArrayList<>();

    public static void render(GuiGraphics gui, Minecraft client) {
        if (!enabled || client.getConnection() == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now > cacheUntil || cached.isEmpty()) {
            cacheUntil = now + 250;
            cached.clear();
            cached.addAll(client.getConnection().getOnlinePlayers());
            cached.sort(Comparator.comparing(p -> p.getProfile().name()));
        }
        List<PlayerInfo> players = cached;
        if (players.isEmpty()) {
            return;
        }
        Font font = client.font;

        int w = 160;
        int rowH = 14;
        int max = 16;
        int n = Math.min(players.size(), max);
        int panelH = n * rowH + 10;

        int[] pos = HudPos.get("staff", client.getWindow().getGuiScaledWidth() - w - 6, 120);
        int x = pos[0];
        int y = pos[1];
        Ui.panel(gui, x, y, w, panelH, Ui.PULSE_PANEL, Ui.PULSE_LINE);
        HudDrag.setArea("staff", x, y, w, panelH);

        int i = 0;
        for (PlayerInfo pi : players) {
            if (i >= max) {
                break;
            }
            String name = pi.getProfile().name();
            boolean staff = STAFF.contains(name.toLowerCase());
            int ry = y + 5 + i * rowH;
            if (staff) {
                gui.fill(x + 4, ry + 2, x + 6, ry + rowH - 2, 0xFF9A9A9A);
            }
            gui.drawString(font, Component.literal(name), x + 12, ry, staff ? 0xFFFFD24A : 0xFFFFFFFF);
            int ping = pi.getLatency();
            String ps = ping < 0 ? "?" : ping + "ms";
            int pc = ping < 50 ? Ui.GREEN : (ping < 150 ? 0xFFFFAA00 : Ui.RED);
            gui.drawString(font, Component.literal(ps), x + w - font.width(ps) - 8, ry, pc);
            i++;
        }
    }
}