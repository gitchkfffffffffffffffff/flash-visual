package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class MultiEditBox extends AbstractWidget {
    private final Minecraft client = Minecraft.getInstance();
    private final StringBuilder text = new StringBuilder();
    private int cursor = 0;
    private int scroll = 0;
    private String placeholder = "";

    public MultiEditBox(int x, int y, int w, int h) {
        super(x, y, w, h, Component.literal(""));
    }

    public void setPlaceholder(String s) {
        placeholder = s == null ? "" : s;
    }

    public String getValue() {
        return text.toString();
    }

    public void setValue(String s) {
        text.setLength(0);
        text.append(s == null ? "" : s);
        cursor = text.length();
        scroll = 0;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        if (!isMouseOver(event.x(), event.y())) {
            return false;
        }
        setFocused(true);
        cursor = clickToCursor((int) event.x(), (int) event.y());
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!isFocused()) {
            return false;
        }
        int key = event.key();
        if (key == GLFW.GLFW_KEY_BACKSPACE) {
            if (cursor > 0) {
                text.deleteCharAt(cursor - 1);
                cursor--;
            }
            return true;
        }
        if (key == GLFW.GLFW_KEY_DELETE) {
            if (cursor < text.length()) {
                text.deleteCharAt(cursor);
            }
            return true;
        }
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            insertAtCursor('\n');
            return true;
        }
        if (key == GLFW.GLFW_KEY_LEFT) {
            cursor = Math.max(0, cursor - 1);
            return true;
        }
        if (key == GLFW.GLFW_KEY_RIGHT) {
            cursor = Math.min(text.length(), cursor + 1);
            return true;
        }
        if (key == GLFW.GLFW_KEY_HOME) {
            cursor = 0;
            return true;
        }
        if (key == GLFW.GLFW_KEY_END) {
            cursor = text.length();
            return true;
        }
        if (key == GLFW.GLFW_KEY_UP) {
            moveCursorLine(-1);
            return true;
        }
        if (key == GLFW.GLFW_KEY_DOWN) {
            moveCursorLine(1);
            return true;
        }
        return false;
    }

    private void moveCursorLine(int dir) {
        List<String> lines = wrappedLines();
        if (lines.isEmpty()) {
            return;
        }
        int[] pos = cursorPos(lines);
        int lineNo = pos[1];
        int col = pos[0];
        int target = Math.max(0, Math.min(lines.size() - 1, lineNo + dir));
        if (target == lineNo) {
            return;
        }
        String s = lines.get(target);
        int best = 0;
        for (int i = 0; i <= s.length(); i++) {
            int w = client.font.width(s.substring(0, i));
            best = i;
            if (w >= col) {
                break;
            }
        }
        int result = 0;
        for (int i = 0; i < target; i++) {
            result += lines.get(i).length() + 1;
        }
        cursor = Math.min(text.length(), result + best);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (!isFocused()) {
            return false;
        }
        String s = event.codepointAsString();
        if (s != null && !s.isEmpty()) {
            text.insert(cursor, s);
            cursor += s.length();
        }
        return true;
    }

    private void insertAtCursor(char c) {
        text.insert(cursor, c);
        cursor++;
    }

    @Override
    protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();
        Ui.roundRect(gui, x, y, w, h, 6, 0xFF101010);
        Ui.roundRect(gui, x, y, w, h, 6, isFocused() ? Ui.PULSE_ACCENT : 0xFF2A2A2A);

        int padX = 5;
        int lineH = client.font.lineHeight + 2;
        int topY = y + 4;
        List<String> lines = wrappedLines();
        if (lines.isEmpty() && !placeholder.isEmpty()) {
            gui.drawString(client.font, placeholder, x + padX, topY, 0xFFFFFFFF);
        }
        int visible = Math.max(1, (h - 8) / lineH);
        if (scroll > Math.max(0, lines.size() - visible)) {
            scroll = Math.max(0, lines.size() - visible);
        }
        if (isFocused()) {
            int cursorLine = cursorPos(lines)[1];
            if (cursorLine < scroll) {
                scroll = cursorLine;
            } else if (cursorLine >= scroll + visible) {
                scroll = cursorLine - visible + 1;
            }
        }
        int start = scroll;
        for (int i = start; i < lines.size() && i < start + visible; i++) {
            gui.drawString(client.font, lines.get(i), x + padX, topY + (i - start) * lineH, 0xFFFFFFFF);
        }

        int total = Math.max(1, lines.size());
        if (total > visible) {
            int trackH = h - 8;
            int thumbH = Math.max(12, trackH * visible / total);
            int maxScroll = lines.size() - visible;
            int thumbY = y + 4 + (trackH - thumbH - 4) * scroll / Math.max(1, maxScroll);
            gui.fill(x + w - 4, y + 4, x + w - 2, y + 4 + trackH, 0xFF1B2436);
            gui.fill(x + w - 4, thumbY, x + w - 2, thumbY + thumbH, 0xFF6EE7B7);
        }

        if (isFocused()) {
            int[] pos = cursorPos(lines);
            int cy = topY + (pos[1] - start) * lineH;
            if (cy >= y && cy < y + h - 1) {
                int cx = x + padX + pos[0];
                gui.fill(cx, cy, cx + 1, cy + client.font.lineHeight, 0xFFFFFFFF);
            }
        }
    }

    private List<String> wrappedLines() {
        int width = Math.max(8, getWidth() - 10);
        List<String> result = new ArrayList<>();
        String full = text.toString();
        if (full.isEmpty()) {
            return result;
        }
        StringBuilder current = new StringBuilder();
        for (String line : full.split("\n", -1)) {
            for (String word : line.split(" ")) {
                String trial = current.length() == 0 ? word : current + " " + word;
                if (client.font.width(trial) > width && current.length() > 0) {
                    result.add(current.toString());
                    current.setLength(0);
                }
                if (current.length() > 0) {
                    current.append(' ');
                }
                current.append(word);
            }
            result.add(current.toString());
            current.setLength(0);
        }
        if (current.length() > 0) {
            result.add(current.toString());
        }
        return result;
    }

    private int[] cursorPos(List<String> lines) {
        int remaining = cursor;
        for (int i = 0; i < lines.size(); i++) {
            String s = lines.get(i);
            if (remaining <= s.length()) {
                return new int[]{client.font.width(s.substring(0, remaining)), i};
            }
            remaining -= s.length() + 1;
        }
        String last = lines.isEmpty() ? "" : lines.get(lines.size() - 1);
        return new int[]{client.font.width(last), Math.max(0, lines.size() - 1)};
    }

    private int clickToCursor(int mx, int my) {
        int lineH = client.font.lineHeight + 2;
        int relY = my - (getY() + 4);
        int lineNo = scroll + relY / lineH;
        if (lineNo < 0) {
            return 0;
        }
        List<String> lines = wrappedLines();
        if (lineNo >= lines.size()) {
            return text.length();
        }
        String s = lines.get(lineNo);
        int padX = 5;
        int relX = mx - (getX() + padX);
        int idx = 0;
        for (int i = 0; i <= s.length(); i++) {
            if (client.font.width(s.substring(0, i)) >= relX) {
                idx = i;
                break;
            }
            idx = i;
        }
        int result = 0;
        for (int i = 0; i < lineNo; i++) {
            result += lines.get(i).length() + 1;
        }
        return Math.min(text.length(), result + idx);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narration) {
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }
        int lineH = client.font.lineHeight + 2;
        int visible = Math.max(1, (getHeight() - 8) / lineH);
        int maxScroll = Math.max(0, wrappedLines().size() - visible);
        scroll = Math.max(0, Math.min(maxScroll, scroll + (verticalAmount > 0 ? -1 : 1)));
        return true;
    }
}
