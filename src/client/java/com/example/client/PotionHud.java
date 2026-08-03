package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Collection;

public class PotionHud {
    private static final int ROW_H = 18;
    private static final int MAX = 8;
    private static final int W = 150;

    public static void render(GuiGraphics gui, Minecraft client) {
        Collection<MobEffectInstance> effects = client.player.getActiveEffects();
        if (effects.isEmpty()) {
            return;
        }
        int[] pos = HudPos.get("potions", client.getWindow().getGuiScaledWidth() - W - 6, 54);
        int x = pos[0];
        int y = pos[1];
        Font font = client.font;

        int count = Math.min(effects.size(), MAX);
        int panelH = count * ROW_H + 8;
        Ui.panel(gui, x, y, W, panelH, 0xC00B0F1A, 0xFF1E2A3E);
        HudDrag.setArea("potions", x, y, W, panelH);

        int i = 0;
        for (MobEffectInstance e : effects) {
            if (i >= MAX) {
                break;
            }
            int ry = y + 4 + i * ROW_H;
            int color = e.getEffect().value().getColor() & 0xFFFFFF;
            gui.fill(x + 4, ry, x + 7, ry + ROW_H - 2, 0xFF000000 | color);
            String name = Component.translatable(e.getDescriptionId()).getString();
            String amp = amplifier(e.getAmplifier());
            String dur = duration(e);
            gui.drawString(font, Component.literal(name + amp), x + 13, ry + 1, 0xFFFFFFFF);
            gui.drawString(font, Component.literal(dur), x + W - font.width(dur) - 10, ry + 1, 0xFF9A9A9A);
            i++;
        }
    }

    private static String amplifier(int amp) {
        if (amp <= 0) {
            return "";
        }
        String[] romans = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return amp <= romans.length ? " " + romans[amp - 1] : " " + (amp + 1);
    }

    private static String duration(MobEffectInstance e) {
        if (e.isInfiniteDuration() || e.getDuration() < 0) {
            return "∞";
        }
        int total = (e.getDuration() + 19) / 20;
        int m = total / 60;
        int s = total % 60;
        return m + ":" + (s < 10 ? "0" : "") + s;
    }
}