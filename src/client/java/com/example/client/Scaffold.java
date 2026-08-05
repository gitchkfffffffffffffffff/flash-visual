package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class Scaffold {
    public static boolean enabled = false;
    public static int delay = 2;

    private static int counter = 0;

    public static void tick(Minecraft client) {
        if (!enabled) {
            return;
        }
        LocalPlayer player = client.player;
        if (player == null || client.level == null || client.gameMode == null || client.screen != null) {
            return;
        }

        counter++;
        if (counter % delay != 0) {
            return;
        }

        BlockPos target = findTargetPos(player);
        if (target == null) {
            return;
        }

        int slot = findBlockSlot(player);
        if (slot < 0) {
            return;
        }
        if (player.getInventory().getSelectedSlot() != slot) {
            player.getInventory().setSelectedSlot(slot);
        }

        Level level = player.level();
        BlockHitResult hit = findPlaceHit(level, target, player);
        if (hit == null) {
            return;
        }
        player.lookAt(EntityAnchorArgument.Anchor.EYES, hit.getLocation());
        client.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
        player.swing(InteractionHand.MAIN_HAND);
    }

    private static BlockHitResult findPlaceHit(Level level, BlockPos target, LocalPlayer player) {
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (Direction d : Direction.values()) {
            BlockPos neighbor = target.relative(d);
            BlockState state = level.getBlockState(neighbor);
            if (level.isLoaded(neighbor) && !state.isAir() && state.getFluidState().isEmpty()) {
                double dist = neighbor.distToCenterSqr(player.getX(), player.getY(), player.getZ());
                if (dist < bestDist) {
                    bestDist = dist;
                    best = neighbor;
                }
            }
        }
        if (best == null) {
            return null;
        }
        Direction face = null;
        int dx = best.getX() - target.getX();
        int dy = best.getY() - target.getY();
        int dz = best.getZ() - target.getZ();
        for (Direction d : Direction.values()) {
            if (d.getStepX() == dx && d.getStepY() == dy && d.getStepZ() == dz) {
                face = d;
                break;
            }
        }
        if (face == null) {
            face = Direction.UP;
        }
        return new BlockHitResult(Vec3.atCenterOf(best), face.getOpposite(), best, false);
    }

    private static BlockPos findTargetPos(LocalPlayer player) {
        Level level = player.level();
        BlockPos feet = player.blockPosition();
        BlockPos under = feet.below();
        if (level.getBlockState(under).isAir()) {
            return under;
        }
        boolean jumping = player.input.keyPresses.jump() && !player.onGround();
        if (jumping) {
            return feet;
        }
        Vec3 look = player.getLookAngle();
        int dx = (int) Math.round(look.x);
        int dz = (int) Math.round(look.z);
        if (dx == 0 && dz == 0) {
            return null;
        }
        BlockPos ahead = feet.offset(dx, -1, dz);
        if (!ahead.equals(under) && level.getBlockState(ahead).isAir()) {
            return ahead;
        }
        return null;
    }

    private static int findBlockSlot(LocalPlayer player) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getItem(i).getItem() instanceof BlockItem) {
                return i;
            }
        }
        return -1;
    }
}
