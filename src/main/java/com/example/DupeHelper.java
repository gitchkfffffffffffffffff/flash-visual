package com.example;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DupeHelper {
    public static List<ItemStack> collectContainerItems(Player player) {
        List<ItemStack> result = new ArrayList<>();
        if (player == null) {
            return result;
        }
        AbstractContainerMenu menu = player.containerMenu;
        if (menu != null) {
            for (Slot slot : menu.slots) {
                if (slot.container != player.getInventory() && !slot.getItem().isEmpty()) {
                    result.add(slot.getItem().copy());
                }
            }
        }
        return result;
    }

    public static void attemptContainerSyncDupe(Player player) {
        if (player == null) {
            return;
        }
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null) {
            return;
        }

        boolean moved = false;
        try {
            for (int i = 0; i < menu.slots.size(); i++) {
                Slot slot = menu.slots.get(i);
                if (slot.container != player.getInventory() && !slot.getItem().isEmpty()) {
                    menu.clicked(i, 0, ClickType.QUICK_MOVE, player);
                    moved = true;
                }
            }
        } catch (Exception e) {
            // ignore
        }

        try {
            menu.removed(player);
        } catch (Exception e) {
            // ignore
        }

        player.displayClientMessage(
            Component.literal(moved ? "Sync dupe attempted!" : "Open a chest with items first, then type .dupe"),
            false
        );
    }
}
