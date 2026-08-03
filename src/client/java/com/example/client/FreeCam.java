package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class FreeCam {
    private static final double SPEED = 8.0;

    private static boolean active = false;
    private static Marker camera = null;
    private static ClientInput originalInput = null;

    private static class FrozenInput extends ClientInput {
        private boolean frozen = false;

        void setFrozen(boolean frozen) {
            this.frozen = frozen;
        }

        @Override
        public Vec2 getMoveVector() {
            return frozen ? Vec2.ZERO : super.getMoveVector();
        }

        @Override
        public void tick() {
            if (frozen) {
                this.keyPresses = Input.EMPTY;
            } else {
                super.tick();
            }
        }

        @Override
        public boolean hasForwardImpulse() {
            return !frozen && super.hasForwardImpulse();
        }

        @Override
        public void makeJump() {
            if (!frozen) {
                super.makeJump();
            }
        }
    }

    private static final FrozenInput frozenInput = new FrozenInput();

    public static boolean isActive() {
        return active;
    }

    public static void toggle(Minecraft client) {
        if (client.level == null || client.player == null) {
            active = false;
            return;
        }
        if (active) {
            disable(client);
        } else {
            enable(client);
        }
    }

    private static void enable(Minecraft client) {
        LocalPlayer player = client.player;
        active = true;
        camera = new Marker(EntityType.MARKER, client.level);
        camera.setPos(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
        camera.setYRot(player.getYRot());
        camera.setXRot(player.getXRot());
        syncInterpolation(camera);
        client.setCameraEntity(camera);

        originalInput = player.input;
        player.input = frozenInput;
        frozenInput.setFrozen(true);
        player.displayClientMessage(Component.literal("FreeCam ON (F6)"), false);
    }

    public static void disable(Minecraft client) {
        active = false;
        camera = null;
        frozenInput.setFrozen(false);
        if (client.player != null) {
            client.player.input = originalInput != null ? originalInput : client.player.input;
            originalInput = null;
            client.setCameraEntity(client.player);
        }
    }

    public static void tick(Minecraft client) {
        if (!active) {
            return;
        }
        LocalPlayer player = client.player;
        if (player == null || client.level == null || camera == null) {
            disable(client);
            return;
        }
        if (client.getCameraEntity() != camera) {
            client.setCameraEntity(camera);
        }
    }

    public static void update(Minecraft client, float realtimeDeltaTicks) {
        if (!active) {
            return;
        }
        LocalPlayer player = client.player;
        if (player == null || client.level == null || camera == null) {
            disable(client);
            return;
        }
        double seconds = realtimeDeltaTicks / 20.0;
        double maxStep = SPEED * seconds;

        double forward = 0;
        double strafe = 0;
        double up = 0;
        if (client.options.keyUp.isDown()) forward += 1;
        if (client.options.keyDown.isDown()) forward -= 1;
        if (client.options.keyRight.isDown()) strafe += 1;
        if (client.options.keyLeft.isDown()) strafe -= 1;
        if (client.options.keyJump.isDown()) up += 1;
        if (client.options.keyShift.isDown()) up -= 1;

        double speed = client.options.keySprint.isDown() ? SPEED * 2.2 : SPEED;
        Vec3 look = player.getLookAngle();
        Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();
        double fx = look.x * forward + right.x * strafe;
        double fz = look.z * forward + right.z * strafe;
        double fl = Math.sqrt(fx * fx + fz * fz);
        double dx = 0;
        double dz = 0;
        if (fl > 0) {
            double sc = speed * seconds / fl;
            dx = fx * sc;
            dz = fz * sc;
        }
        Vec3 move = new Vec3(dx, up * maxStep, dz);

        camera.setPos(camera.getX() + move.x, camera.getY() + move.y, camera.getZ() + move.z);
        camera.setYRot(player.getYRot());
        camera.setXRot(player.getXRot());
        syncInterpolation(camera);
    }

    private static void syncInterpolation(Marker cam) {
        cam.xo = cam.getX();
        cam.yo = cam.getY();
        cam.zo = cam.getZ();
        cam.xOld = cam.xo;
        cam.yOld = cam.yo;
        cam.zOld = cam.zo;
    }
}