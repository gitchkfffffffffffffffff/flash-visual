package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

import java.util.ArrayList;
import java.util.List;

public class FriendListScreen extends Screen {
    private final Minecraft client = Minecraft.getInstance();
    private final List<Button> rowWidgets = new ArrayList<>();
    private EditBox nameBox;
    private boolean pendingRebuild = false;

    public FriendListScreen() {
        super(Component.literal("Список друзей"));
    }

    @Override
    protected void init() {
        int cx = width / 2;
        nameBox = new EditBox(client.font, cx - 150, 42, 210, 18, Component.literal("Ник"));
        nameBox.setMaxLength(32);
        addRenderableWidget(nameBox);
        addRenderableWidget(new Ui.StyledButton(cx + 68, 42, 90, 18, Component.literal("Добавить"), Ui.GREEN,
            b -> addFriend()));
        addRenderableWidget(new Ui.StyledButton(cx + 162, 42, 60, 18, Component.literal("Цель"), Ui.ACCENT,
            b -> addTargetAsFriend()));
        addRenderableWidget(new Ui.StyledButton(cx - 150, height - 40, 150, 20, Component.literal("Назад"), 0xFF444444,
            b -> client.setScreen(new DupeGuiScreen())));
        addRenderableWidget(new Ui.StyledButton(cx + 8, height - 40, 142, 20, Component.literal("Очистить всё"), 0xFF555555,
            b -> {
                Friends.clear();
                pendingRebuild = true;
            }));
        rebuildRows();
    }

    private void addFriend() {
        String name = nameBox.getValue().trim();
        if (name.isEmpty()) {
            return;
        }
        if (Friends.add(name)) {
            nameBox.setValue("");
        }
        pendingRebuild = true;
    }

    private void addTargetAsFriend() {
        if (client.hitResult instanceof EntityHitResult ehr && ehr.getEntity() instanceof Player p) {
            Friends.add(p.getName().getString());
            pendingRebuild = true;
        } else if (client.player != null) {
            client.player.displayClientMessage(Component.literal("Наведи прицел на игрока"), false);
        }
    }

    private void rebuildRows() {
        for (Button b : rowWidgets) {
            removeWidget(b);
        }
        rowWidgets.clear();
        int cx = width / 2;
        int x = cx - 150, w = 300;
        int y = 72;
        for (String name : Friends.all()) {
            RowWidget row = new RowWidget(x, y, w, 22, name);
            addRenderableWidget(row);
            rowWidgets.add(row);
            DeleteButton del = new DeleteButton(x + w - 26, y + 2, 20, 18, name);
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
        Ui.gradientText(gui, font, "Список друзей", width / 2 - font.width("Список друзей") / 2, 8,
            0xFF00A8FF, 0xFF9DFFE0);
        Ui.section(gui, font, "Ник игрока", width / 2 - 150, 28, 460);
        gui.drawString(font, Component.literal("Кнопка 'Цель' — добавить того, на кого наведён прицел"),
            width / 2 - 150, height - 64, 0xFF9A9A4A);
    }

    private class RowWidget extends Button {
        private final String name;

        RowWidget(int x, int y, int w, int h, String name) {
            super(x, y, w, h, Component.literal(name), b -> {
            }, DEFAULT_NARRATION);
            this.name = name;
        }

        @Override
        public void onPress(InputWithModifiers input) {
        }

        @Override
        protected void renderContents(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            boolean hover = isHoveredOrFocused();
            gui.fill(x, y, x + w, y + h, hover ? 0xFF1A2A1A : 0xFF0E1A0E);
            gui.fill(x, y, x + 3, y + h, Ui.GREEN);
            Font font = Minecraft.getInstance().font;
            gui.drawString(font, getMessage(), x + 12, y + (h - 8) / 2, 0xFFB6F0B6);
            String tag = "ДРУГ";
            gui.drawString(font, tag, x + w - 24 - font.width(tag), y + (h - 8) / 2, Ui.GREEN);
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
            Friends.remove(name);
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
