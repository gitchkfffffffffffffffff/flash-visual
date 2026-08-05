package com.example.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.Scoreboard;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ScoreboardHud {
    public static boolean enabled = true;

    private static int grabDX = 0;
    private static int grabDY = 0;
    private static boolean active = false;

    public static void render(GuiGraphics gui, Minecraft client) {
        Player player = client.player;
        if (player == null) {
            return;
        }
        Scoreboard sb = client.level.getScoreboard();
        Objective obj = sb.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (obj == null) {
            return;
        }
        Font font = client.font;

        String title = obj.getDisplayName().getString();
        List<PlayerScoreEntry> all = new ArrayList<>(sb.listPlayerScores(obj));
        all.sort(Comparator.comparingInt(PlayerScoreEntry::value));
        List<PlayerScoreEntry> rows = new ArrayList<>();
        for (int i = all.size() - 1; i >= 0 && rows.size() < 15; i--) {
            PlayerScoreEntry e = all.get(i);
            if (!e.isHidden()) {
                rows.add(e);
            }
        }

        int titleW = font.width(title);
        int textW = titleW;
        List<String> names = new ArrayList<>();
        List<String> nums = new ArrayList<>();
        for (PlayerScoreEntry e : rows) {
            String n = e.ownerName().getString();
            String v = e.formatValue(obj.numberFormat()).getString();
            names.add(n);
            nums.add(v);
            int rowW = font.width(n) + font.width(v);
            if (rowW > textW) {
                textW = rowW;
            }
        }
        int padX = 6;
        int panelW = textW + padX * 2 + 8;
        int rowH = 9;
        int panelH = rowH * rows.size() + 14 + 2;

        int sw = client.getWindow().getGuiScaledWidth();
        int[] pos = HudPos.get("scoreboard", sw - panelW - 4, 2);
        int x = pos[0];
        int y = pos[1];
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }

        int bg = 0xE6000000;
        Ui.panel(gui, x, y, panelW, panelH, bg, 0xFF1A1A1A);

        long t = System.currentTimeMillis();
        float phase = (t % 12000L) / 12000f;
        int[] a = blend3(phase, 0x00CFFF, 0xB44AFF, 0xFFAA00);
        int accent = rgb(a[0], a[1], a[2]);
        Ui.gradientText(gui, font, title, x + (panelW - titleW) / 2, y + 3, accent, 0xFFFFFFFF);
        gui.fill(x + 2, y + 12, x + panelW - 2, y + 13, 0x33FFFFFF);
        gui.fill(x + 2, y + 13, x + panelW - 2, y + 14, accent);

        int ly = y + 16;
        int maxNumW = 0;
        for (String v : nums) {
            int w = font.width(v);
            if (w > maxNumW) {
                maxNumW = w;
            }
        }
        for (int i = 0; i < names.size(); i++) {
            int col = (i & 1) == 0 ? 0xFFD6D6DA : 0xFFB9BDC6;
            if (!names.get(i).isEmpty()) {
                char fc = names.get(i).charAt(0);
                if (fc == '\u00a7' && names.get(i).length() > 1) {
                    col = plainColorOf(names.get(i).charAt(1), 0xFFD6D6DA);
                }
            }
            gui.drawString(font, Component.literal(names.get(i)), x + 5, ly, col);
            gui.drawString(font, Component.literal(nums.get(i)), x + panelW - 5 - maxNumW, ly, 0xFFFFFF);
            ly += rowH;
        }

        drag(client, x, y, panelW, panelH);
    }

    private static void drag(Minecraft client, int x, int y, int w, int h) {
        if (client.getWindow() == null || client.player == null) {
            return;
        }
        long handle = client.getWindow().handle();
        if (GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_PRESS) {
            active = false;
            return;
        }
        double[] px = new double[1];
        double[] py = new double[1];
        GLFW.glfwGetCursorPos(handle, px, py);
        double scale = client.getWindow().getGuiScale();
        int mx = (int) (px[0] / scale);
        int my = (int) (py[0] / scale);
        if (!active) {
            if (mx >= x && mx <= x + w && my >= y && my <= y + h) {
                active = true;
                int[] p = HudPos.get("scoreboard", x, y);
                grabDX = mx - p[0];
                grabDY = my - p[1];
            }
        }
        if (active) {
            int[] p = HudPos.get("scoreboard", 0, 0);
            p[0] = mx - grabDX;
            p[1] = my - grabDY;
        }
    }

    private static int plainColorOf(char code, int def) {
        ChatFormatting cf = ChatFormatting.getByCode(code);
        if (cf == null || cf.getColor() == null) {
            return def;
        }
        int col = cf.getColor();
        return 0xFF000000 | (col & 0xFFFFFF);
    }

    private static int[] blend(int c1, int c2, float t) {
        return new int[]{
            (int) (((c1 >> 16) & 0xFF) + (((c2 >> 16) & 0xFF) - ((c1 >> 16) & 0xFF)) * t),
            (int) (((c1 >> 8) & 0xFF) + (((c2 >> 8) & 0xFF) - ((c1 >> 8) & 0xFF)) * t),
            (int) ((c1 & 0xFF) + ((c2 & 0xFF) - (c1 & 0xFF)) * t)
        };
    }

    private static int[] blend3(float p, int ra, int rb, int rc) {
        if (p < 0.5f) {
            return blend(ra, rb, p * 2f);
        }
        return blend(rb, rc, (p - 0.5f) * 2f);
    }

    private static int rgb(int r, int g, int b) {
        return 0xFF000000 | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }
}