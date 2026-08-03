package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class AutoTotem {
    public static boolean enabled = false;
    public static float healthThreshold = 0.5f;
    public static int delay = 1;
    private static int counter = 0;

    private static Item totemItem() {
        Identifier id = Identifier.fromNamespaceAndPath("minecraft", "totem_of_undying");
        Item item = BuiltInRegistries.ITEM.getValue(id);
        if (item == null) {
            return BuiltInRegistries.ITEM.stream()
                .filter(i -> i.toString().contains("totem_of_undying"))
                .findFirst()
                .orElse(null);
        }
        return item;
    }

    public static void tick(Minecraft client) {
        if (!enabled) {
            return;
        }
        LocalPlayer player = client.player;
        if (player == null || client.level == null) {
            return;
        }
        counter++;
        if (counter < delay) {
            return;
        }
        counter = 0;

        ItemStack offhand = player.getOffhandItem();
        Item totem = totemItem();
        if (totem == null) {
            return;
        }
        if (offhand.getItem() == totem && offhand.getCount() > 0) {
            return;
        }

        float hp = player.getHealth();
        float maxHp = Math.max(1f, player.getMaxHealth());
        float ratio = hp / maxHp;
        boolean low = ratio <= healthThreshold;

        if (low) {
            int slot = findTotemSlot(player, totem);
            if (slot >= 0) {
                swapToOffhand(client, player, slot);
            }
            return;
        }

        if (offhand.getItem() == totem) {
            int slot = findTotemSlot(player, totem);
            if (slot >= 0) {
                swapToOffhand(client, player, slot);
            }
        }
    }

    private static int findTotemSlot(LocalPlayer player, Item totem) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == totem) {
                return i;
            }
        }
        return -1;
    }

    private static void swapToOffhand(Minecraft client, LocalPlayer player, int hotbarSlot) {
        int invSlot = hotbarSlot;
        if (client.gameMode != null) {
            client.gameMode.handleInventoryMouseClick(
                player.inventoryMenu.containerId,
                invSlot,
                40,
                net.minecraft.world.inventory.ClickType.SWAP,
                player
            );
        }
    }
}
