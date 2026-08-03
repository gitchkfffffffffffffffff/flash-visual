package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

public class EspRenderer {
    public static boolean playerEsp = false;
    public static boolean mobEsp = false;
    public static boolean itemEsp = false;

    private static final double RANGE = 64.0;
    private static int tick = 0;

    public static boolean anyEnabled() {
        return playerEsp || mobEsp || itemEsp;
    }

    public static void tick(Minecraft client) {
        if (!anyEnabled()) {
            return;
        }
        tick++;
        if (tick < 10) {
            return;
        }
        tick = 0;
        if (client.level == null || client.player == null) {
            return;
        }
        double rangeSq = RANGE * RANGE;
        for (Entity entity : client.level.entitiesForRendering()) {
            if (entity == client.player || entity.isRemoved()) {
                continue;
            }
            if (entity.distanceToSqr(client.player) > rangeSq) {
                entity.setGlowingTag(false);
                continue;
            }
            boolean shouldGlow = (playerEsp && entity instanceof Player)
                || (mobEsp && entity instanceof Monster)
                || (itemEsp && entity instanceof ItemEntity);
            entity.setGlowingTag(shouldGlow);
        }
    }

    public static void clear(Minecraft client) {
        if (client.level == null) {
            return;
        }
        for (Entity entity : client.level.entitiesForRendering()) {
            entity.setGlowingTag(false);
        }
    }
}
