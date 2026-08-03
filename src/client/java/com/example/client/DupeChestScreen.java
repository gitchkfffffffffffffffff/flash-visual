package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DupeChestScreen extends Screen {
    private static final int COLS = 9;
    private static final int SLOT_SIZE = 18;

    public static UUID boundUuid = null;
    public static ItemStack boundStack = null;

    private final List<ItemStack> items;
    private final Minecraft client = Minecraft.getInstance();
    private int selectedIndex = -1;

    public DupeChestScreen(List<ItemStack> items) {
        super(Component.literal("Dupe Chest"));
        this.items = items;
    }

    public static void clearBound() {
        boundUuid = null;
        boundStack = null;
    }

    @Override
    protected void init() {
        int btnY = height - 40;
        addRenderableWidget(new Ui.StyledButton(20, btnY, 100, 20, Component.literal("Выдать"), Ui.GREEN,
            b -> giveSelected()));
        addRenderableWidget(new Ui.StyledButton(128, btnY, 130, 20, Component.literal("Отправить на стенд"), Ui.CYAN,
            b -> sendToStand()));
        addRenderableWidget(new Ui.StyledButton(266, btnY, 130, 20, Component.literal("Привязать стенд"), Ui.ACCENT,
            b -> bindToArmorStand()));
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);

        Ui.gradientText(gui, client.font, "Сундук · дуп/стенд", 12, 6, 0xFF00A8FF, 0xFF9DFFE0);

        int gridX = width / 2 + 10;
        int gridY = 30;
        int rows = (items.size() + COLS - 1) / COLS;
        Ui.panel(gui, gridX - 6, gridY - 6, COLS * SLOT_SIZE + 12, rows * SLOT_SIZE + 12, Ui.PULSE_PANEL, 0xFF26304A);

        for (int i = 0; i < items.size(); i++) {
            int x = gridX + (i % COLS) * SLOT_SIZE;
            int y = gridY + (i / COLS) * SLOT_SIZE;
            ItemStack stack = items.get(i);
            gui.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0x33000000);
            gui.renderItem(stack, x + 1, y + 1);
            gui.renderItemDecorations(client.font, stack, x + 1, y + 1);
            if (selectedIndex == i) {
                gui.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0x3355FF55);
                gui.renderOutline(x - 1, y - 1, SLOT_SIZE + 2, SLOT_SIZE + 2, 0xFF2ECC40);
            } else if (mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y && mouseY < y + SLOT_SIZE) {
                gui.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0x33FFFFFF);
                gui.renderOutline(x, y, SLOT_SIZE, SLOT_SIZE, 0xFFFFFFFF);
            }
        }

        Ui.panel(gui, 12, 26, width / 2 - 24, height - 60, Ui.PULSE_PANEL, 0xFF26304A);
        gui.drawString(client.font, Component.literal("Предмет и NBT:"), 22, 34, Ui.PULSE_ACCENT);
        if (selectedIndex >= 0 && selectedIndex < items.size()) {
            ItemStack stack = items.get(selectedIndex);
            String name = stack.getHoverName().getString() + " x" + stack.getCount();
            String nbt = stack.getComponents().stream()
                .map(c -> c.type().toString() + "=" + c.value())
                .collect(Collectors.joining(", ", "{", "}"));
            int ty = 52;
            gui.drawString(client.font, Component.literal(name), 22, ty, 0xFFFFFF);
            ty += 14;
            for (var line : client.font.split(Component.literal(nbt), width / 2 - 40)) {
                gui.drawString(client.font, line, 22, ty, 0xFF9A9A9A);
                ty += 9;
                if (ty > height - 70) {
                    break;
                }
            }
        }

        if (boundUuid != null) {
            gui.drawString(client.font, Component.literal("Стенд привязан"), 22, height - 56, 0xFF2ECC40);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        if (event.buttonInfo().button() == 0) {
            int idx = slotIndexAt((int) event.x(), (int) event.y());
            if (idx >= 0) {
                selectedIndex = idx;
                bindToArmorStand();
                return true;
            }
        }
        return super.mouseClicked(event, bl);
    }

    private int slotIndexAt(int mouseX, int mouseY) {
        int gridX = width / 2 + 10;
        int gridY = 30;
        for (int i = 0; i < items.size(); i++) {
            int x = gridX + (i % COLS) * SLOT_SIZE;
            int y = gridY + (i / COLS) * SLOT_SIZE;
            if (mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y && mouseY < y + SLOT_SIZE) {
                return i;
            }
        }
        return -1;
    }

    private void giveSelected() {
        Player player = client.player;
        if (player == null) {
            return;
        }
        if (selectedIndex < 0 || selectedIndex >= items.size()) {
            player.displayClientMessage(Component.literal("Сначала выбери предмет"), false);
            return;
        }
        ArmorStand stand = boundStandEntity();
        if (stand != null) {
            stand.setPos(player.getX(), player.getY(), player.getZ());
            client.gameMode.attack(player, stand);
            player.displayClientMessage(Component.literal("Армор стенд телепортирован к тебе и отдал предмет"), false);
        } else {
            ItemStack stack = items.get(selectedIndex).copy();
            spawnStack(player.level(), player.getX(), player.getY(), player.getZ(), stack);
            player.displayClientMessage(Component.literal("Выдано: " + stack.getHoverName().getString() + " x" + stack.getCount()), false);
        }
    }

    private void sendToStand() {
        Player player = client.player;
        if (player == null) {
            return;
        }
        if (selectedIndex < 0 || selectedIndex >= items.size()) {
            player.displayClientMessage(Component.literal("Сначала выбери предмет"), false);
            return;
        }
        ArmorStand stand = boundStandEntity();
        if (stand == null) {
            player.displayClientMessage(Component.literal("Смотри на армор стенд и кликни предмет в меню"), false);
            return;
        }
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null || menu == player.inventoryMenu) {
            player.displayClientMessage(Component.literal("Открой сундук с предметами"), false);
            return;
        }
        ItemStack target = items.get(selectedIndex);
        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            if (slot.container != player.getInventory() && ItemStack.isSameItemSameComponents(slot.getItem(), target)) {
                menu.clicked(i, 0, ClickType.QUICK_MOVE, player);
                break;
            }
        }
        if (!holdMatchingItem(target)) {
            player.displayClientMessage(Component.literal("Предмет не найден в инвентаре"), false);
            return;
        }
        client.gameMode.interact(player, stand, InteractionHand.MAIN_HAND);
        player.displayClientMessage(Component.literal("Отправлено на стенд: " + target.getHoverName().getString()), false);
    }

    private boolean holdMatchingItem(ItemStack target) {
        Player player = client.player;
        AbstractContainerMenu menu = player.containerMenu;
        Inventory inv = player.getInventory();
        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            if (slot.container == inv && slot.hasItem() && ItemStack.isSameItemSameComponents(slot.getItem(), target)) {
                menu.clicked(i, 0, ClickType.SWAP, player);
                inv.setSelectedSlot(0);
                return true;
            }
        }
        return false;
    }

    private ArmorStand boundStandEntity() {
        if (client.level == null || boundUuid == null) {
            return null;
        }
        if (client.level.getEntity(boundUuid) instanceof ArmorStand stand) {
            return stand;
        }
        return null;
    }

    private void bindToArmorStand() {
        Player player = client.player;
        if (player == null) {
            return;
        }
        if (selectedIndex < 0 || selectedIndex >= items.size()) {
            player.displayClientMessage(Component.literal("Сначала выбери предмет"), false);
            return;
        }
        if (client.hitResult == null || !(client.hitResult instanceof EntityHitResult ehr)) {
            return;
        }
        if (!(ehr.getEntity() instanceof ArmorStand stand)) {
            return;
        }
        boundUuid = stand.getUUID();
        boundStack = items.get(selectedIndex).copy();
        player.displayClientMessage(Component.literal("Армор стенд привязан: " + boundStack.getHoverName().getString() + " x" + boundStack.getCount()), false);
    }

    private void spawnStack(Level level, double x, double y, double z, ItemStack stack) {
        ItemEntity entity = new ItemEntity(level, x, y, z, stack);
        level.addFreshEntity(entity);
    }
}
