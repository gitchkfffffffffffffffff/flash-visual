package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class CheatModules {

    public static void updateAll(Minecraft client) {
        Fly.update(client);
        Speed.update(client);
        Spider.update(client);
        AirJump.update(client);
        AutoSprint.update(client);
        NoFall.update(client);
        ChestStealer.update(client);
        Noclip.update(client);
    }

    public static void disableAll(Minecraft client) {
        Fly.enabled = false;
        Speed.enabled = false;
        Spider.enabled = false;
        AirJump.enabled = false;
        AutoSprint.enabled = false;
        NoFall.enabled = false;
        ChestStealer.enabled = false;
        Noclip.enabled = false;
        Fly.disable(client);
        Noclip.disable(client);
    }

    public static class Noclip {
        public static boolean enabled = false;
        public static double speed = 0.42;

        public static void update(Minecraft client) {
            LocalPlayer p = client.player;
            if (p == null || !enabled) {
                return;
            }
            p.noPhysics = true;
            p.setNoGravity(true);
            Vec2 mv = p.input.getMoveVector();
            double fwd = mv.y;
            double strafe = mv.x;
            boolean jump = p.input.keyPresses.jump();
            boolean sneak = p.input.keyPresses.shift();
            double m = speed;
            double yaw = Math.toRadians(p.getYRot());
            double x = (-Math.sin(yaw) * fwd + Math.cos(yaw) * strafe) * m;
            double z = (Math.cos(yaw) * fwd + Math.sin(yaw) * strafe) * m;
            double y = 0;
            if (jump) {
                y = m * 0.75;
            } else if (sneak) {
                y = -m * 0.75;
            }
            p.setDeltaMovement(x, y, z);
        }

        public static void disable(Minecraft client) {
            if (client.player != null) {
                client.player.setNoGravity(false);
            }
        }
    }

    public static class Fly {
        public static boolean enabled = false;
        public static double speed = 1.2;

        public static void update(Minecraft client) {
            LocalPlayer p = client.player;
            if (p == null || !enabled) {
                return;
            }
            p.setNoGravity(true);
            if (p.isFallFlying()) {
                return;
            }
            Vec2 mv = p.input.getMoveVector();
            double fwd = mv.y;
            double strafe = mv.x;
            boolean jump = p.input.keyPresses.jump();
            boolean sneak = p.input.keyPresses.shift();
            double yaw = Math.toRadians(p.getYRot());
            double m = speed;
            double x = (-Math.sin(yaw) * fwd + Math.cos(yaw) * strafe) * m;
            double z = (Math.cos(yaw) * fwd + Math.sin(yaw) * strafe) * m;
            double y = 0;
            if (jump) {
                y = m * 0.8;
            } else if (sneak) {
                y = -m * 0.8;
            }
            p.setDeltaMovement(x, y, z);
        }

        public static void disable(Minecraft client) {
            if (client.player != null) {
                client.player.setNoGravity(false);
            }
        }
    }

    public static class Speed {
        public static boolean enabled = false;
        public static double boost = 0.55;

        public static void update(Minecraft client) {
            LocalPlayer p = client.player;
            if (p == null || !enabled) {
                return;
            }
            if (p.onGround() && p.input.getMoveVector().y > 0.1) {
                double yaw = Math.toRadians(p.getYRot());
                p.setDeltaMovement(-Math.sin(yaw) * boost, p.getDeltaMovement().y, Math.cos(yaw) * boost);
            }
        }
    }

    public static class Spider {
        public static boolean enabled = false;

        public static void update(Minecraft client) {
            LocalPlayer p = client.player;
            if (p == null || !enabled) {
                return;
            }
            if (p.horizontalCollision && !p.isSpectator()) {
                Vec3 v = p.getDeltaMovement();
                if (v.y < 0.25) {
                    p.setDeltaMovement(v.x, 0.25, v.z);
                }
            }
        }
    }

    public static class AirJump {
        public static boolean enabled = false;
        private static boolean wasJump = false;

        public static void update(Minecraft client) {
            LocalPlayer p = client.player;
            if (p == null || !enabled) {
                return;
            }
            boolean jump = p.input.keyPresses.jump();
            if (jump && !wasJump && !p.onGround() && !p.getAbilities().flying) {
                Vec3 v = p.getDeltaMovement();
                p.setDeltaMovement(v.x, 0.42, v.z);
            }
            wasJump = jump;
        }
    }

    public static class AutoSprint {
        public static boolean enabled = false;

        public static void update(Minecraft client) {
            LocalPlayer p = client.player;
            if (p == null || !enabled) {
                return;
            }
            if (p.input.getMoveVector().y > 0.1 && !p.isSprinting()) {
                p.setSprinting(true);
            }
        }
    }

    public static class NoFall {
        public static boolean enabled = false;

        public static void update(Minecraft client) {
            LocalPlayer p = client.player;
            if (p == null || !enabled) {
                return;
            }
            if (p.fallDistance > 3.0f) {
                p.fallDistance = 0.0f;
            }
        }
    }

    public static class ChestStealer {
        public static boolean enabled = false;
        private static int tick = 0;

        public static void update(Minecraft client) {
            if (!enabled || client.player == null) {
                return;
            }
            tick++;
            if (tick < 4) {
                return;
            }
            tick = 0;
            if (!(client.screen instanceof AbstractContainerScreen<?> acs)) {
                return;
            }
            AbstractContainerMenu menu = acs.getMenu();
            if (menu == null) {
                return;
            }
            int slots = menu.slots.size() - 40;
            if (slots <= 0) {
                return;
            }
            for (int i = 0; i < slots; i++) {
                var slot = menu.slots.get(i);
                if (slot != null && slot.hasItem() && !slot.getItem().isEmpty()) {
                    menu.clicked(i, 0, ClickType.QUICK_MOVE, client.player);
                }
            }
        }
    }
}
