package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;

public class FakePlayer {
    public static RemotePlayer fake = null;

    public static boolean active() {
        return fake != null;
    }

    public static void toggle(Minecraft client) {
        if (fake != null) {
            despawn(client);
        } else {
            spawn(client);
        }
    }

    public static void spawn(Minecraft client) {
        if (client.level == null || client.player == null || !(client.level instanceof ClientLevel cl)) {
            return;
        }
        if (fake != null) {
            despawn(client);
        }
        fake = new RemotePlayer(cl, client.getGameProfile());
        fake.setId(getNextId(cl));
        fake.setPos(client.player.getX(), client.player.getY(), client.player.getZ());
        fake.setYRot(client.player.getYRot());
        fake.setXRot(client.player.getXRot());
        fake.setNoGravity(false);
        client.level.addEntity(fake);
    }

    public static void despawn(Minecraft client) {
        if (fake != null) {
            if (client.level != null) {
                client.level.removeEntity(fake.getId(), Entity.RemovalReason.DISCARDED);
            }
            fake = null;
        }
    }

    private static int getNextId(ClientLevel cl) {
        int id = 1000;
        while (cl.getEntity(id) != null) {
            id++;
        }
        return id;
    }
}