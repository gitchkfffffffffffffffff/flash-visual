package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Ui {
    public static final int ACCENT = 0xFFFFAA00;
    public static final int PANEL = 0xD0121212;
    public static final int GREEN = 0xFF2ECC40;
    public static final int RED = 0xFFFF4136;
    public static final int CYAN = 0xFF00D0FF;
    public static final int PURPLE = 0xFFB44AFF;

    public static int PULSE_BG = 0xFF0A0E18;
    public static int PULSE_PANEL = 0xC00B0F1A;
    public static int PULSE_ACCENT = 0xFF00CFFF;
    public static int PULSE_LINE = 0xFF1E2A3E;

    public static void applyAccent(int accent) {
        PULSE_ACCENT = accent;
        PULSE_LINE = mix(accent, 0xFF000000, 0.60f);
    }

    public static void applyPalette(int bg, int panel, int line) {
        PULSE_BG = bg;
        PULSE_PANEL = panel;
        PULSE_LINE = line;
    }

    public static void roundRect(GuiGraphics gui, int x, int y, int w, int h, int r, int color) {
        int rr = Math.max(1, Math.min(r, Math.min(w, h) / 2));
        int x0 = x + rr;
        int x1 = x + w - rr;
        int y0 = y + rr;
        int y1 = y + h - rr;
        if (x0 <= x1 && y0 <= y1) {
            gui.fill(x0, y, x1, y + h, color);
            gui.fill(x, y0, x + w, y1, color);
        }
        int rr2 = rr * rr;
        for (int cy = 0; cy < rr; cy++) {
            int dy = rr - 1 - cy;
            int dx = (int) Math.round(Math.sqrt(Math.max(0, rr2 - dy * dy)));
            gui.fill(x, y + cy, x + dx, y + cy + 1, color);
            gui.fill(x + w - dx, y + cy, x + w, y + cy + 1, color);
            gui.fill(x, y + h - 1 - cy, x + dx, y + h - cy, color);
            gui.fill(x + w - dx, y + h - 1 - cy, x + w, y + h - cy, color);
        }
    }

    public static void panel(GuiGraphics gui, int x, int y, int w, int h, int bg, int line) {
        int r = 8;
        roundRect(gui, x, y, w, h, r, bg);
        int rr = r;
        int rr2 = rr * rr;
        for (int cy = 0; cy < rr; cy++) {
            int dy = rr - 1 - cy;
            int dx = (int) Math.round(Math.sqrt(Math.max(0, rr2 - dy * dy)));
            gui.fill(x, y + cy, x + dx, y + cy + 1, line);
            gui.fill(x + w - dx, y + cy, x + w, y + cy + 1, line);
            gui.fill(x, y + h - 1 - cy, x + dx, y + h - cy, line);
            gui.fill(x + w - dx, y + h - 1 - cy, x + w, y + h - cy, line);
        }
        gui.fill(x + rr, y, x + w - rr, y + 1, line);
        gui.fill(x + rr, y + h - 1, x + w - rr, y + h, line);
        gui.fill(x, y + rr, x + 1, y + h - rr, line);
        gui.fill(x + w - 1, y + rr, x + w, y + h - rr, line);
    }

    public static void section(GuiGraphics gui, Font font, String text, int x, int y, int lineW) {
        gui.drawString(font, Component.literal(text), x, y, 0xFF9A9A9A);
        gui.fill(x + font.width(text) + 6, y + 4, x + lineW, y + 5, 0x44FFAA00);
    }

    public static void gradientText(GuiGraphics gui, Font font, String text, int x, int y, int from, int to) {
        int r0 = (from >> 16) & 0xFF, g0 = (from >> 8) & 0xFF, b0 = from & 0xFF;
        int r1 = (to >> 16) & 0xFF, g1 = (to >> 8) & 0xFF, b1 = to & 0xFF;
        int cursor = x;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            float t = text.length() <= 1 ? 0 : (float) i / (text.length() - 1);
            int r = (int) (r0 + (r1 - r0) * t);
            int g = (int) (g0 + (g1 - g0) * t);
            int b = (int) (b0 + (b1 - b0) * t);
            gui.drawString(font, String.valueOf(c), cursor, y, (0xFF << 24) | (r << 16) | (g << 8) | b);
            cursor += font.width(String.valueOf(c));
        }
    }

    public static int mix(int color, int with, float t) {
        int a = (color >>> 24) & 0xFF;
        int r = (int) (((color >> 16) & 0xFF) * (1 - t) + ((with >> 16) & 0xFF) * t);
        int g = (int) (((color >> 8) & 0xFF) * (1 - t) + ((with >> 8) & 0xFF) * t);
        int b = (int) ((color & 0xFF) * (1 - t) + (with & 0xFF) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static class StyledButton extends Button {
        private int accent;

        public StyledButton(int x, int y, int w, int h, Component message, int accent, OnPress onPress) {
            super(x, y, w, h, message, onPress, DEFAULT_NARRATION);
            this.accent = accent;
        }

        public void setAccent(int accent) {
            this.accent = accent;
        }

        @Override
        protected void renderContents(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            int r = 6;
            int bg, line;
            if (!active) {
                bg = 0x44111111;
                line = 0xFF333333;
            } else if (isHoveredOrFocused()) {
                bg = 0xFF000000 | mix(accent & 0xFFFFFF, 0xFFFFFF, 0.30f);
                line = 0xFFFFFFFF;
            } else {
                bg = 0xB0000000 | (accent & 0xFFFFFF);
                line = accent;
            }
            roundRect(gui, x, y, w, h, r, bg);
            roundRect(gui, x, y, w, h, r, line);
            gui.drawCenteredString(Minecraft.getInstance().font, getMessage(), x + w / 2, y + (h - 8) / 2, 0xFFFFFFFF);
        }
    }

    public static class PulseSlider extends Button {
        private final Supplier<Float> getter;
        private final DoubleConsumer setter;
        private final float min;
        private final float max;
        private final Function<Float, String> fmt;

        public PulseSlider(int x, int y, int w, int h, String name, float min, float max,
                           Supplier<Float> getter, DoubleConsumer setter, Function<Float, String> fmt) {
            super(x, y, w, h, Component.literal(name), b -> {
            }, DEFAULT_NARRATION);
            this.getter = getter;
            this.setter = setter;
            this.min = min;
            this.max = max;
            this.fmt = fmt;
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean inside) {
            setFrom(event.x());
        }

        @Override
        protected void onDrag(MouseButtonEvent event, double dragX, double dragY) {
            setFrom(event.x());
        }

        private void setFrom(double mx) {
            float t = (float) Math.max(0.0, Math.min(1.0, (mx - (getX() + 13)) / Math.max(1, getWidth() - 46)));
            setter.accept(min + (max - min) * t);
        }

        @Override
        protected void renderContents(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            roundRect(gui, x, y, w, h, 6, 0xFF0E1420);
            roundRect(gui, x, y, 3, h, 6, PULSE_ACCENT);
            Font font = Minecraft.getInstance().font;
            gui.drawString(font, getMessage(), x + 13, y + (h - 8) / 2, 0xFFB6BDC9);
            String right = fmt.apply(getter.get());
            int rw = font.width(right);
            int trackX = x + 13 + font.width(getMessage()) + 10;
            int trackW = w - 13 - rw - 20 - trackX;
            if (trackW < 10) {
                trackW = 10;
            }
            int trackY = y + h / 2 - 1;
            roundRect(gui, trackX, trackY, trackW, 2, 1, 0xFF253047);
            float t = Math.max(0, Math.min(1, (getter.get() - min) / Math.max(0.0001f, max - min)));
            int fill = (int) (trackW * t);
            roundRect(gui, trackX, trackY, fill, 2, 1, PULSE_ACCENT);
            roundRect(gui, trackX + fill - 2, y + 4, 4, h - 8, 2, 0xFFFFFFFF);
            gui.drawString(font, right, x + w - rw - 10, y + (h - 8) / 2, PULSE_ACCENT);
        }
    }

    public static class PulseRow extends Button {
        private final Supplier<Boolean> enabled;
        private final Supplier<String> rightText;
        private final Runnable pressAction;

        public PulseRow(int x, int y, int w, int h, String name, Supplier<Boolean> enabled,
                        Supplier<String> rightText, Runnable pressAction) {
            super(x, y, w, h, Component.literal(name), b -> {
            }, DEFAULT_NARRATION);
            this.enabled = enabled;
            this.rightText = rightText;
            this.pressAction = pressAction;
        }

        @Override
        public void onPress(InputWithModifiers input) {
            pressAction.run();
        }

        @Override
        protected void renderContents(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
            boolean on = enabled.get();
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            int r = 6;
            int bg;
            if (on) {
                bg = isHoveredOrFocused() ? 0xFF14263F : 0xFF101C30;
            } else {
                bg = isHoveredOrFocused() ? 0xFF141A28 : 0xFF0E1420;
            }
            roundRect(gui, x, y, w, h, r, bg);
            roundRect(gui, x, y, 3, h, r, on ? PULSE_ACCENT : 0xFF253047);
            Font font = Minecraft.getInstance().font;
            gui.drawString(font, getMessage(), x + 13, y + (h - 8) / 2, on ? 0xFFFFFFFF : 0xFFB6BDC9);
            String right = rightText.get();
            gui.drawString(font, right, x + w - font.width(right) - 12, y + (h - 8) / 2,
                on ? PULSE_ACCENT : 0xFF556070);
        }
    }

    public static class PulseCategory extends Button {
        private final boolean active;

        public PulseCategory(int x, int y, int w, int h, String name, boolean active, OnPress onPress) {
            super(x, y, w, h, Component.literal(name), onPress, DEFAULT_NARRATION);
            this.active = active;
        }

        @Override
        protected void renderContents(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            int r = 6;
            int bg = active ? 0xFF13243C : (isHoveredOrFocused() ? 0xFF131B2C : 0xFF0E1524);
            roundRect(gui, x, y, w, h, r, bg);
            roundRect(gui, x, y, 2, h, r, active ? PULSE_ACCENT : 0xFF253047);
            Font font = Minecraft.getInstance().font;
            gui.drawString(font, getMessage(), x + 11, y + (h - 8) / 2, active ? 0xFF00CFFF : 0xFF9AA4B2);
        }
    }
}
