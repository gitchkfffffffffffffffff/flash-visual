package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class KillAura {
    public static boolean enabled = false;
    public static boolean hitPlayers = true;
    public static double range = 4.0;
    public static int delay = 4;

    private static int counter = 0;

    public static void tick(Minecraft client) {
        if (!enabled) {
            return;
        }
        LocalPlayer player = client.player;
        if (player == null || client.level == null || client.gameMode == null) {
            return;
        }

        counter++;
        if (counter % delay != 0) {
            return;
        }

        LivingEntity target = findTarget(client);
        if (target == null) {
            return;
        }

        player.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
        client.gameMode.attack(player, target);
        player.swing(InteractionHand.MAIN_HAND);
    }

    private static LivingEntity findTarget(Minecraft client) {
        LocalPlayer player = client.player;
        LivingEntity best = null;
        double bestDist = range * range;
        List<Entity> entities = client.level.getEntities(
            player,
            player.getBoundingBox().inflate(range),
            e -> e instanceof LivingEntity
        );
        for (Entity e : entities) {
            if (!(e instanceof LivingEntity living)) {
                continue;
            }
            if (living == player || !living.isAlive()) {
                continue;
            }
            if (living instanceof ArmorStand) {
                continue;
            }
            if (living instanceof Player && !hitPlayers) {
                continue;
            }
            double d = player.distanceToSqr(living);
            if (d < bestDist) {
                bestDist = d;
                best = living;
            }
        }
        return best;
    }
}
