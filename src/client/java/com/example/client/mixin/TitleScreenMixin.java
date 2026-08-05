package com.example.client.mixin;

import com.example.client.Features;
import com.example.client.ScreenAccessor;
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
public abstract class TitleScreenMixin {
    private static final int P = 40;
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
        int y = 70;
        for (net.minecraft.client.gui.components.AbstractWidget aw : list) {
            aw.setX(24);
            aw.setWidth(180);
            aw.setY(y);
            y += 24;
        }
        ((ScreenAccessor) self).flashVisual$addRenderableWidget(new Ui.StyledButton(24, y + 4, 180, 20, Component.literal("Альт менеджер"),
            Ui.PULSE_ACCENT, b -> Minecraft.getInstance().setScreen(new com.example.client.AltManagerScreen())));
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
        float phase = (t % 16000L) / 16000f;
        int[] a1 = blend3(phase, 0xFF2E2E2E, 0xFF141414, 0xFF3A3A3A);
        gui.fillGradient(0, 0, w, h, rgb(a1[0], a1[1], a1[2]), rgb(5, 5, 5));

        gui.fillGradient(0, 0, w, h, 0x12FFFFFF, 0x00000000);
        int grid = 0x0A8A8A8A;
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

        Font font = Minecraft.getInstance().font;
        String title = "Flash Visual";
        String ver = "v1.1.0-pre1";
        int tw = font.width(title);
        int tx = (w - tw) / 2;
        float phase = (t % 16000L) / 16000f;
        int[] accent = blend3(phase, 0xFFCFCFCF, 0xFF9A9A9A, 0xFF6A6A6A);
        int accentCol = rgb(accent[0], accent[1], accent[2]);

        Ui.panel(gui, tx - 70, 30, tw + 140, 54, 0xCC111111, accentCol);

        int glow = (0x38 << 24) | (accentCol & 0xFFFFFF);
        for (int ox = -3; ox <= 3; ox++) {
            for (int oy = -3; oy <= 3; oy++) {
                if (ox * ox + oy * oy <= 9) {
                    gui.drawString(font, title, tx + ox, 42 + oy, glow);
                }
            }
        }
        gui.drawString(font, title, tx + 1, 43, 0x66000000);
        Ui.gradientText(gui, font, title, tx, 42, 0xFFFFFFFF, 0xFF8A8A8A);

        int lineW = tw + 60;
        int lineX = (w - lineW) / 2;
        gui.fillGradient(lineX, 62, lineX + lineW / 2, 63, accentCol, 0x33FFFFFF);
        gui.fillGradient(lineX + lineW / 2, 62, lineX + lineW, 63, 0x33FFFFFF, accentCol);
        int vw = font.width(ver);
        gui.drawString(font, ver, (w - vw) / 2, 68, 0xFF8A8A8A);

        drawParticles(gui, w, h, t);
        drawVignette(gui, w, h);

        String nick = com.example.client.AltManager.getActive();
        if (nick == null || nick.isEmpty()) {
            nick = Minecraft.getInstance().getUser().getName();
        }
        if (com.example.client.StreamerMode.ownNick(Minecraft.getInstance()) != null) {
            nick = "Игрок";
        }
        if (nick != null && !nick.isEmpty()) {
            int nx = 14;
            int ny = 14;
            Ui.panel(gui, nx - 6, ny - 3, font.width(nick) + 12, 12, 0xAA101010, accentCol);
            gui.drawString(font, Component.literal(nick), nx, ny, 0xFFFFFFFF);
        }
    }

    private static void drawParticles(GuiGraphics gui, int w, int h, long t) {
        if (!inited) {
            inited = true;
            for (int i = 0; i < P; i++) {
                PX[i] = (float) Math.random();
                PY[i] = (float) Math.random();
                PV[i] = 0.008f + (float) Math.random() * 0.018f;
                PH[i] = (float) (Math.random() * Math.PI * 2);
                PT[i] = Math.random() < 0.5 ? 0xFFCFCFCF : 0xFF7A7A7A;
            }
        }
        float time = t / 1000f;
        int[] xs = new int[P];
        int[] ys = new int[P];
        int[] alpha = new int[P];
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
            alpha[i] = 0x38 + (int) (0x28 * (0.5 + 0.5 * Math.sin(time * 1.5f + PH[i])));
            xs[i] = (int) (PX[i] * w);
            ys[i] = (int) (PY[i] * h);
        }
        for (int i = 0; i < P; i++) {
            for (int j = i + 1; j < P; j++) {
                int dx = xs[i] - xs[j];
                int dy = ys[i] - ys[j];
                int d2 = dx * dx + dy * dy;
                if (d2 < 170 * 170) {
                    int lc = (0x1A << 24) | ((0x9A9A9A) & 0xFFFFFF);
                    if (d2 < 90 * 90) {
                        lc = (0x2E << 24) | ((0xCFCFCF) & 0xFFFFFF);
                    }
                    gui.fill(xs[i], ys[i], xs[j], ys[j] + 1, lc);
                }
            }
        }
        for (int i = 0; i < P; i++) {
            int col = (alpha[i] << 24) | (PT[i] & 0xFFFFFF);
            int x = xs[i];
            int y = ys[i];
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
