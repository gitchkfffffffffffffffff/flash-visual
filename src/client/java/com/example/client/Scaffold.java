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
        player.getInventory().setSelectedSlot(slot);

        Level level = player.level();
        for (Direction d : Direction.values()) {
            BlockPos neighbor = target.relative(d);
            BlockState state = level.getBlockState(neighbor);
            if (level.isLoaded(neighbor) && !state.isAir() && state.getFluidState().isEmpty()) {
                player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(neighbor));
                BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(neighbor), d.getOpposite(), neighbor, false);
                client.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
                player.swing(InteractionHand.MAIN_HAND);
                return;
            }
        }
    }

    private static BlockPos findTargetPos(LocalPlayer player) {
        Level level = player.level();
        BlockPos feet = player.blockPosition();
        BlockPos under = feet.below();
        if (level.getBlockState(under).isAir()) {
            return under;
        }
        Vec3 look = player.getLookAngle();
        BlockPos ahead = feet.offset((int) Math.round(look.x), -1, (int) Math.round(look.z));
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
