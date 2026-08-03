package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class AltManagerScreen extends Screen {
    private final Minecraft client = Minecraft.getInstance();
    private final List<Button> rowWidgets = new ArrayList<>();
    private EditBox nameBox;
    private boolean pendingRebuild = false;

    public AltManagerScreen() {
        super(Component.literal("Альт менеджер"));
    }

    @Override
    protected void init() {
        int cx = width / 2;
        nameBox = new EditBox(client.font, cx - 150, 42, 210, 18, Component.literal("Ник"));
        nameBox.setMaxLength(32);
        addRenderableWidget(nameBox);
        addRenderableWidget(new Ui.StyledButton(cx + 68, 42, 90, 18, Component.literal("Добавить"), Ui.GREEN,
            b -> addAlt()));
        addRenderableWidget(new Ui.StyledButton(cx - 150, height - 40, 150, 20, Component.literal("Назад"), 0xFF444444,
            b -> client.setScreen(new DupeGuiScreen())));
        addRenderableWidget(new Ui.StyledButton(cx + 8, height - 40, 142, 20, Component.literal("Снять выбор"), 0xFF555555,
            b -> {
                AltManager.deselect();
                pendingRebuild = true;
            }));
        rebuildRows();
    }

    private void addAlt() {
        String name = nameBox.getValue().trim();
        if (name.isEmpty()) {
            return;
        }
        AltManager.add(name);
        nameBox.setValue("");
        pendingRebuild = true;
    }

    private void rebuildRows() {
        for (Button b : rowWidgets) {
            removeWidget(b);
        }
        rowWidgets.clear();
        int cx = width / 2;
        int x = cx - 150, w = 300;
        int y = 72;
        for (String alt : AltManager.getAlts()) {
            boolean active = alt.equals(AltManager.getActive());
            RowWidget row = new RowWidget(x, y, w, 22, alt, active);
            addRenderableWidget(row);
            rowWidgets.add(row);
            DeleteButton del = new DeleteButton(x + w - 26, y + 2, 20, 18, alt);
            addRenderableWidget(del);
            rowWidgets.add(del);
            y += 26;
        }
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        if (pendingRebuild) {
            pendingRebuild = false;
            rebuildRows();
        }
        gui.fill(0, 0, width, height, Ui.PULSE_BG);
        gui.fill(0, 0, width, 2, Ui.PULSE_ACCENT);
        super.render(gui, mouseX, mouseY, partialTick);

        Font font = client.font;
        Ui.gradientText(gui, font, "Альт менеджер", width / 2 - font.width("Альт менеджер") / 2, 8,
            0xFF00A8FF, 0xFF9DFFE0);
        Ui.section(gui, font, "Ник альтa", width / 2 - 150, 28, 460);
        String hint = AltManager.isActive()
            ? "Активен: " + AltManager.getActive() + "  —  вступит в силу при заходе на сервер"
            : "Альт не выбран — используется ваш ник";
        gui.drawString(font, Component.literal(hint), width / 2 - 150, height - 64, 0xFF9A9A9A);
    }

    private class RowWidget extends Button {
        private final String name;
        private final boolean active;

        RowWidget(int x, int y, int w, int h, String name, boolean active) {
            super(x, y, w, h, Component.literal(name), b -> {
            }, DEFAULT_NARRATION);
            this.name = name;
            this.active = active;
        }

        @Override
        public void onPress(InputWithModifiers input) {
            AltManager.select(name);
            pendingRebuild = true;
        }

        @Override
        protected void renderContents(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            boolean hover = isHoveredOrFocused();
            gui.fill(x, y, x + w, y + h, active ? 0xFF13243C : (hover ? 0xFF141A28 : 0xFF0E1420));
            gui.fill(x, y, x + 3, y + h, active ? Ui.PULSE_ACCENT : 0xFF253047);
            Font font = Minecraft.getInstance().font;
            gui.drawString(font, getMessage(), x + 12, y + (h - 8) / 2, active ? 0xFFFFFFFF : 0xFFB6BDC9);
            if (active) {
                String tag = "ВЫБРАН";
                gui.drawString(font, tag, x + w - 34 - font.width(tag), y + (h - 8) / 2, Ui.GREEN);
            }
        }
    }

    private class DeleteButton extends Button {
        private final String name;

        DeleteButton(int x, int y, int w, int h, String name) {
            super(x, y, w, h, Component.literal("✕"), b -> {
            }, DEFAULT_NARRATION);
            this.name = name;
        }

        @Override
        public void onPress(InputWithModifiers input) {
            AltManager.remove(name);
            pendingRebuild = true;
        }

        @Override
        protected void renderContents(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            boolean hover = isHoveredOrFocused();
            gui.fill(x, y, x + w, y + h, hover ? 0xFF4A1F2A : 0xFF141A28);
            gui.renderOutline(x, y, w, h, hover ? Ui.RED : 0xFF4A3A3A);
            Font font = Minecraft.getInstance().font;
            gui.drawCenteredString(font, "✕", x + w / 2, y + (h - 8) / 2, hover ? 0xFFFFFFFF : 0xFFFF8888);
        }
    }
}
