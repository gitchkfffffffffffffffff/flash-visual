package com.example.client;

import com.example.DupeGivePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ItemGiveScreen extends Screen {
    private static final int COLS = 10;
    private static final int SLOT = 18;
    private static final int GRID_Y = 74;

    private final Minecraft client = Minecraft.getInstance();
    private final List<ItemStack> allItems = new ArrayList<>();
    private List<ItemStack> filtered = new ArrayList<>();
    private EditBox searchBox;
    private EditBox countBox;
    private int scroll = 0;
    private ItemStack selected = null;

    public ItemGiveScreen() {
        super(Component.literal("Выдача предметов"));
        BuiltInRegistries.ITEM.stream().forEach(item -> {
            ItemStack stack = new ItemStack(item);
            if (!stack.isEmpty()) {
                allItems.add(stack);
            }
        });
        filtered = allItems;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int topX = cx - 190;

        searchBox = new EditBox(client.font, topX + 70, 28, 180, 18, Component.literal("Поиск"));
        searchBox.setMaxLength(64);
        searchBox.setResponder(s -> updateFilter());
        addRenderableWidget(searchBox);

        countBox = new EditBox(client.font, topX + 130, 28, 50, 18, Component.literal("Кол-во"));
        countBox.setMaxLength(3);
        countBox.setFilter(s -> s.matches("\\d*"));
        countBox.setValue("1");
        addRenderableWidget(countBox);

        int btnY = height - 44;
        addRenderableWidget(new Ui.StyledButton(cx - 250, btnY, 100, 20, Component.literal("Выдать"), Ui.GREEN,
            b -> give()));
        addRenderableWidget(new Ui.StyledButton(cx - 140, btnY, 44, 20, Component.literal("-1"), 0xFF555555,
            b -> changeCount(-1)));
        addRenderableWidget(new Ui.StyledButton(cx - 92, btnY, 44, 20, Component.literal("+1"), 0xFF555555,
            b -> changeCount(1)));
        addRenderableWidget(new Ui.StyledButton(cx - 44, btnY, 60, 20, Component.literal("Стек"), 0xFF777777,
            b -> setStack()));
        addRenderableWidget(new Ui.StyledButton(cx + 150, btnY, 100, 20, Component.literal("Назад"), 0xFF444444,
            b -> client.setScreen(new DupeGuiScreen())));
    }

    private void updateFilter() {
        String query = searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            filtered = allItems;
        } else {
            filtered = new ArrayList<>();
            for (ItemStack stack : allItems) {
                String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
                String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
                if (name.contains(query) || id.contains(query)) {
                    filtered.add(stack);
                }
            }
        }
        scroll = 0;
    }

    private void changeCount(int delta) {
        int amount = Math.max(1, parseCount() + delta);
        countBox.setValue(String.valueOf(amount));
    }

    private void setStack() {
        if (selected != null) {
            countBox.setValue(String.valueOf(selected.getMaxStackSize()));
        }
    }

    private void give() {
        Player player = client.player;
        if (player == null || selected == null) {
            return;
        }
        ItemStack stack = selected.copy();
        stack.setCount(parseCount());
        if (client.isSingleplayer() || DupeModClient.serverHasMod) {
            ClientPlayNetworking.send(new DupeGivePayload(stack, null));
        } else {
            spawnStack(player.level(), player.getX(), player.getY() + 0.5, player.getZ(), stack);
        }
        player.displayClientMessage(Component.literal("Выдано: " + stack.getHoverName().getString() + " x" + stack.getCount()), false);
    }

    private int parseCount() {
        try {
            return Math.max(1, Math.min(selected.getMaxStackSize(), Integer.parseInt(countBox.getValue())));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void spawnStack(Level level, double x, double y, double z, ItemStack stack) {
        ItemEntity entity = new ItemEntity(level, x, y, z, stack);
        entity.setDeltaMovement(new Vec3((Math.random() - 0.5) * 0.2, 0.25, (Math.random() - 0.5) * 0.2));
        level.addFreshEntity(entity);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        if (event.buttonInfo().button() == 0) {
            int idx = slotIndexAt((int) event.x(), (int) event.y());
            if (idx >= 0) {
                selected = filtered.get(idx);
                return true;
            }
        }
        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = Math.max(0, (filtered.size() + COLS - 1) / COLS - visibleRows());
        scroll = Math.max(0, Math.min(maxScroll, scroll + (verticalAmount > 0 ? -1 : 1)));
        return true;
    }

    private int visibleRows() {
        return Math.max(1, (height - 80 - GRID_Y) / SLOT);
    }

    private int slotIndexAt(int mouseX, int mouseY) {
        int gridX = width / 2 - COLS * SLOT / 2;
        int startIdx = scroll * COLS;
        for (int i = 0; i < visibleRows() * COLS; i++) {
            int idx = startIdx + i;
            if (idx >= filtered.size()) {
                break;
            }
            int x = gridX + (i % COLS) * SLOT;
            int y = GRID_Y + (i / COLS) * SLOT;
            if (mouseX >= x && mouseX < x + SLOT && mouseY >= y && mouseY < y + SLOT) {
                return idx;
            }
        }
        return -1;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);

        int cx = width / 2;
        Ui.gradientText(gui, client.font, "Выдача предметов", cx - client.font.width("Выдача предметов") / 2, 6,
            0xFF00A8FF, 0xFF9DFFE0);

        int topX = cx - 190;
        Ui.panel(gui, topX, 22, 380, 30, Ui.PULSE_PANEL, Ui.PULSE_ACCENT);
        gui.drawString(client.font, Component.literal("Поиск"), topX + 8, 30, 0xFF9A9A9A);
        gui.drawString(client.font, Component.literal("Кол-во"), topX + 245, 30, 0xFF9A9A9A);

        int gridX = cx - COLS * SLOT / 2;
        int gridBottom = height - 76;
        Ui.panel(gui, gridX - 6, GRID_Y - 6, COLS * SLOT + 12, gridBottom - GRID_Y + 12, Ui.PULSE_PANEL, 0xFF26304A);

        int startIdx = scroll * COLS;
        for (int i = 0; i < visibleRows() * COLS; i++) {
            int idx = startIdx + i;
            if (idx >= filtered.size()) {
                break;
            }
            int x = gridX + (i % COLS) * SLOT;
            int y = GRID_Y + (i / COLS) * SLOT;
            ItemStack stack = filtered.get(idx);
            gui.fill(x, y, x + SLOT, y + SLOT, 0x33000000);
            gui.renderItem(stack, x + 1, y + 1);
            if (stack == selected) {
                gui.fill(x, y, x + SLOT, y + SLOT, 0x3355FF55);
                gui.renderOutline(x - 1, y - 1, SLOT + 2, SLOT + 2, 0xFF2ECC40);
            } else if (mouseX >= x && mouseX < x + SLOT && mouseY >= y && mouseY < y + SLOT) {
                gui.fill(x, y, x + SLOT, y + SLOT, 0x33FFFFFF);
                gui.renderOutline(x, y, SLOT, SLOT, 0xFFFFFFFF);
            }
        }

        if (selected != null) {
            Ui.panel(gui, 20, height - 74, 240, 26, Ui.PULSE_PANEL, Ui.PULSE_ACCENT);
            String name = selected.getHoverName().getString();
            String id = BuiltInRegistries.ITEM.getKey(selected.getItem()).toString();
            gui.drawString(client.font, Component.literal(name), 26, height - 70, 0xFFFFFF);
            gui.drawString(client.font, Component.literal(id), 26 + client.font.width(name) + 10, height - 70, 0xFF9A9A9A);
        }

        gui.drawString(client.font, Component.literal("Прокрутка колесом мыши · " + filtered.size() + " предметов"),
            gridX, gridBottom + 4, 0xFF6A6A6A);
    }
}
