package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.phys.Vec3;

public class ZhiguliCam {
    private static boolean active = false;
    private static Marker camera = null;

    public static boolean isActive() {
        return active;
    }

    private static void enable(Minecraft client) {
        LocalPlayer player = client.player;
        active = true;
        camera = new Marker(EntityType.MARKER, client.level);
        syncToPlayer(camera, player);
        client.setCameraEntity(camera);
    }

    private static void disable(Minecraft client) {
        active = false;
        camera = null;
        if (client.player != null && client.getCameraEntity() instanceof Marker) {
            client.setCameraEntity(client.player);
        }
    }

    private static void syncToPlayer(Marker cam, LocalPlayer player) {
        double yaw = Math.toRadians(player.getYRot());
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);
        double seatForward = 0.25;
        double seatHeight = 1.05;
        cam.setPos(
            player.getX() + fx * seatForward,
            player.getY() + seatHeight,
            player.getZ() + fz * seatForward
        );
        cam.setYRot(player.getYRot());
        cam.setXRot(player.getXRot());
        cam.xo = cam.getX();
        cam.yo = cam.getY();
        cam.zo = cam.getZ();
        cam.xOld = cam.xo;
        cam.yOld = cam.yo;
        cam.zOld = cam.zo;
    }

    public static void tick(Minecraft client) {
        if (!WorldVisuals.zhiguliView) {
            if (active) {
                disable(client);
            }
            return;
        }
        LocalPlayer player = client.player;
        if (player == null || client.level == null) {
            disable(client);
            return;
        }
        if (!active || camera == null) {
            enable(client);
            return;
        }
        if (client.getCameraEntity() != camera) {
            client.setCameraEntity(camera);
        }
        syncToPlayer(camera, player);
    }
}