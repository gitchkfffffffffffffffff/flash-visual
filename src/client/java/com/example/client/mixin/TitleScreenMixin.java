package com.example.client.mixin;

import com.example.client.Features;
import com.example.client.Ui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    private static final int P = 56;
    private static final float[] PX = new float[P];
    private static final float[] PY = new float[P];
    private static final float[] PV = new float[P];
    private static final float[] PH = new float[P];
    private static final int[] PT = new int[P];
    private static boolean inited = false;

    @Inject(method = "init", at = @At("TAIL"))
    private void flashVisual$layout(CallbackInfo ci) {
        if (!Features.beautifulMenu) {
            return;
        }
        net.minecraft.client.gui.screens.Screen self = (net.minecraft.client.gui.screens.Screen) (Object) this;
        java.util.List<net.minecraft.client.gui.components.AbstractWidget> list = new java.util.ArrayList<>();
        for (Object o : self.children()) {
            if (o instanceof net.minecraft.client.gui.components.AbstractWidget aw) {
                list.add(aw);
            }
        }
        for (net.minecraft.client.gui.components.AbstractWidget aw : list) {
            aw.setX(24);
            aw.setWidth(180);
        }
    }

    @Inject(method = "renderBackground(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
        at = @At("HEAD"), cancellable = true)
    private void flashVisual$bg(GuiGraphics gui, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!Features.beautifulMenu) {
            return;
        }
        ci.cancel();
        int w = gui.guiWidth();
        int h = gui.guiHeight();
        long t = System.currentTimeMillis();
        float phase = (t % 12000L) / 12000f;
        int[] accent = blend3(phase, 0xFF00CFFF, 0xFFB44AFF, 0xFFFFAA00);
        int ar = accent[0], ag = accent[1], ab = accent[2];
        int top = rgb(8, 11, 20);
        int bottom = rgb(Math.min(255, ar / 6 + 10), Math.min(255, ag / 6 + 14), Math.min(255, ab / 6 + 26));
        gui.fillGradient(0, 0, w, h, top, bottom);
        gui.fillGradient(0, 0, w, 120, (0x22 << 24) | (ar << 16) | (ag << 8) | ab, 0x00000000);
        int grid = (0x08 << 24) | 0x00CFFF;
        for (int x = 0; x < w; x += 48) {
            gui.fill(x, 0, x + 1, h, grid);
        }
        for (int y = 0; y < h; y += 48) {
            gui.fill(0, y, w, y + 1, grid);
        }
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("RETURN"))
    private void flashVisual$overlay(GuiGraphics gui, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!Features.beautifulMenu) {
            return;
        }
        int w = gui.guiWidth();
        int h = gui.guiHeight();
        long t = System.currentTimeMillis();

        gui.fillGradient(0, 24, w, 112, 0xD80B0F1A, 0x550B0F1A);
        Font font = Minecraft.getInstance().font;
        String title = "Flash Visual";
        String ver = "v1.1.0-pre1";
        int tw = font.width(title);
        int tx = (w - tw) / 2;
        float phase = (t % 12000L) / 12000f;
        int[] accent = blend3(phase, 0xFF00CFFF, 0xFFB44AFF, 0xFFFFAA00);
        int accentCol = rgb(accent[0], accent[1], accent[2]);

        int glow = (0x46 << 24) | (accentCol & 0xFFFFFF);
        gui.drawString(font, title, tx - 2, 44, glow);
        gui.drawString(font, title, tx + 2, 44, glow);
        gui.drawString(font, title, tx, 42, glow);
        Ui.gradientText(gui, font, title, tx, 44, 0xFFFF8800, 0xFF00CFFF);

        gui.fill((w - 120) / 2, 60, (w + 120) / 2, 61, accentCol);
        int vw = font.width(ver);
        gui.drawString(font, ver, (w - vw) / 2, 66, 0xFF9AA4B2);

        drawParticles(gui, w, h, t);
        drawVignette(gui, w, h);

        String nick = com.example.client.AltManager.getActive();
        if (nick == null || nick.isEmpty()) {
            nick = Minecraft.getInstance().getUser().getName();
        }
        if (nick != null && !nick.isEmpty()) {
            int nx = 14;
            int ny = 14;
            Ui.panel(gui, nx - 6, ny - 3, font.width(nick) + 12, 12, 0xAA0B0F1A, accentCol);
            gui.drawString(font, Component.literal(nick), nx, ny, 0xFFFFFFFF);
        }
    }

    private static void drawParticles(GuiGraphics gui, int w, int h, long t) {
        if (!inited) {
            inited = true;
            for (int i = 0; i < P; i++) {
                PX[i] = (float) Math.random();
                PY[i] = (float) Math.random();
                PV[i] = 0.010f + (float) Math.random() * 0.02f;
                PH[i] = (float) (Math.random() * Math.PI * 2);
                PT[i] = Math.random() < 0.5 ? 0xFF00CFFF : 0xFFFFAA00;
            }
        }
        float time = t / 1000f;
        for (int i = 0; i < P; i++) {
            PY[i] -= PV[i];
            PX[i] += (float) Math.sin(time * 0.8f + PH[i]) * 0.0008f;
            if (PY[i] < 0) {
                PY[i] = 1.02f;
                PX[i] = (float) Math.random();
            }
            if (PX[i] < 0) {
                PX[i] = 1.02f;
            } else if (PX[i] > 1.02f) {
                PX[i] = 0;
            }
            int alpha = 0x38 + (int) (0x28 * (0.5 + 0.5 * Math.sin(time * 1.5f + PH[i])));
            int col = (alpha << 24) | (PT[i] & 0xFFFFFF);
            int x = (int) (PX[i] * w);
            int y = (int) (PY[i] * h);
            gui.fill(x, y, x + 1, y + 1, col);
            gui.fill(x + 1, y + 1, x + 2, y + 2, (col & 0x7FFFFFFF));
        }
    }

    private static void drawVignette(GuiGraphics gui, int w, int h) {
        gui.fillGradient(0, 0, w, 30, 0x66000000, 0x00000000);
        gui.fillGradient(0, h - 30, w, h, 0x00000000, 0x66000000);
        gui.fillGradient(0, 0, 30, h, 0x55000000, 0x00000000);
        gui.fillGradient(w - 30, 0, w, h, 0x00000000, 0x55000000);
    }

    private static int[] blend(int c1, int c2, float t) {
        return new int[]{
            (int) (((c1 >> 16) & 0xFF) + (((c2 >> 16) & 0xFF) - ((c1 >> 16) & 0xFF)) * t),
            (int) (((c1 >> 8) & 0xFF) + (((c2 >> 8) & 0xFF) - ((c1 >> 8) & 0xFF)) * t),
            (int) ((c1 & 0xFF) + ((c2 & 0xFF) - (c1 & 0xFF)) * t)
        };
    }

    private static int[] blend3(float p, int a, int b, int c) {
        if (p < 0.5f) {
            return blend(a, b, p * 2f);
        }
        return blend(b, c, (p - 0.5f) * 2f);
    }

    private static int rgb(int r, int g, int b) {
        return 0xFF000000 | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }
}
