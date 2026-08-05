package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
        Ui.panel(gui, x, y, W, panelH, Ui.PULSE_PANEL, Ui.PULSE_LINE);
        HudDrag.setArea("potions", x, y, W, panelH);

        int i = 0;
        for (MobEffectInstance e : effects) {
            if (i >= MAX) {
                break;
            }
            int ry = y + 4 + i * ROW_H;
            int color = e.getEffect().value().getColor() & 0xFFFFFF;
            gui.fill(x + 3, ry + 1, x + 18, ry + ROW_H - 1, 0xFF0A0A0A);
            Identifier icon = effectSprite(e);
            if (icon != null) {
                try {
                    gui.blitSprite(RenderPipelines.GUI_TEXTURED, icon, x + 3, ry + 1, 15, 15);
                } catch (RuntimeException ex) {
                    gui.fill(x + 4, ry + 2, x + 17, ry + 15, 0xFF000000 | color);
                }
            } else {
                gui.fill(x + 4, ry + 2, x + 17, ry + 15, 0xFF000000 | color);
            }
            String name = Component.translatable(e.getDescriptionId()).getString();
            String amp = amplifier(e.getAmplifier());
            String dur = duration(e);
            gui.drawString(font, Component.literal(name + amp), x + 22, ry + 2, 0xFFFFFFFF);
            gui.drawString(font, Component.literal(dur), x + W - font.width(dur) - 10, ry + 2, 0xFF9A9A9A);
            i++;
        }
    }

    private static Identifier effectSprite(MobEffectInstance e) {
        try {
            String rn = e.getEffect().getRegisteredName();
            String ns = "minecraft";
            String path = rn;
            int c = rn.indexOf(':');
            if (c > 0) {
                ns = rn.substring(0, c);
                path = rn.substring(c + 1);
            }
            if (path.isEmpty()) {
                return null;
            }
            return Identifier.fromNamespaceAndPath(ns, "effect/" + path);
        } catch (RuntimeException ex) {
            return null;
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