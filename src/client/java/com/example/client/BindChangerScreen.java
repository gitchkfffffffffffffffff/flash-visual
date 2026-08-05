package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class BindChangerScreen extends Screen {
    private final Minecraft client = Minecraft.getInstance();
    private String listening = null;
    private final List<Button> rows = new ArrayList<>();

    public BindChangerScreen() {
        super(Component.literal("Бинды"));
    }

    @Override
    protected void init() {
        int x = width / 2 - 150, w = 300;
        int y = 48;
        for (String action : Binds.actions()) {
            BindRow row = new BindRow(x, y, w, 22, action);
            addRenderableWidget(row);
            rows.add(row);
            y += 26;
        }
        addRenderableWidget(new Ui.StyledButton(width / 2 - 100, y + 6, 200, 20, Component.literal("Сбросить все бинды"), 0xFF555555,
            b -> {
                Binds.resetAll();
                listening = null;
            }));
        addRenderableWidget(new Ui.StyledButton(width / 2 - 100, height - 40, 100, 20, Component.literal("Назад"), 0xFF444444,
            b -> client.setScreen(new DupeGuiScreen())));
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (listening != null) {
            if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
                listening = null;
            } else {
                Binds.set(listening, event.key());
                listening = null;
            }
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        gui.fill(0, 0, width, height, Ui.PULSE_BG);
        gui.fill(0, 0, width, 2, Ui.PULSE_ACCENT);
        super.render(gui, mouseX, mouseY, partialTick);

        Font font = client.font;
        Ui.gradientText(gui, font, "Бинды", width / 2 - font.width("Бинды") / 2, 8, 0xFF00A8FF, 0xFF9DFFE0);
        String hint = listening != null
            ? "Нажми клавишу для «" + Binds.label(listening) + "» · ESC — отмена"
            : "Клик по строке — сменить клавишу";
        gui.drawString(font, Component.literal(hint), width / 2 - 140, 34,
            listening != null ? Ui.PULSE_ACCENT : 0xFF9A9A9A);
    }

    private class BindRow extends Button {
        private final String action;

        BindRow(int x, int y, int w, int h, String action) {
            super(x, y, w, h, Component.literal(Binds.label(action)), b -> {
            }, DEFAULT_NARRATION);
            this.action = action;
        }

        @Override
        public void onPress(InputWithModifiers input) {
            listening = action;
        }

        @Override
        protected void renderContents(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
            boolean listeningThis = action.equals(listening);
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            boolean hover = isHoveredOrFocused();
            gui.fill(x, y, x + w, y + h, listeningThis ? 0x53131313 : (hover ? 0x30131313 : 0x2E101010));
            gui.fill(x, y, x + 3, y + h, listeningThis ? Ui.PULSE_ACCENT : 0xFF2A2A2A);
            Font font = Minecraft.getInstance().font;
            gui.drawString(font, getMessage(), x + 12, y + (h - 8) / 2, listeningThis ? 0xFFFFFFFF : 0xFF9A9A9A);
            String key = listeningThis ? "…" : Binds.keyName(action);
            gui.drawString(font, key, x + w - font.width(key) - 12, y + (h - 8) / 2, Ui.PULSE_ACCENT);
        }
    }
}