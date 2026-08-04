package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class HouseBuilder {
    public static boolean enabled = false;
    public static int delay = 2;

    private static int counter = 0;
    private static int index = 0;
    private static List<BlockPos> targets = new ArrayList<>();

    public static void toggle(Minecraft client) {
        if (!enabled) {
            start(client);
        } else {
            enabled = false;
            targets.clear();
            if (client.player != null) {
                client.player.displayClientMessage(Component.literal("Стройка дома OFF"), false);
            }
        }
    }

    private static void start(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.level == null) {
            return;
        }
        Vec3 look = player.getLookAngle();
        BlockPos feet = player.blockPosition();
        BlockPos anchor = feet.offset((int) Math.round(look.x) * 3, 0, (int) Math.round(look.z) * 3);
        targets = buildHouse(anchor);
        if (targets.isEmpty()) {
            return;
        }
        index = 0;
        counter = 0;
        enabled = true;
        player.displayClientMessage(Component.literal("Стройка дома: " + targets.size() + " блоков"), false);
    }

    public static void tick(Minecraft client) {
        if (!enabled) {
            return;
        }
        LocalPlayer player = client.player;
        if (player == null || client.level == null || client.gameMode == null || client.screen != null) {
            return;
        }
        counter++;
        if (counter % Math.max(1, delay) != 0) {
            return;
        }
        int slot = findBlockSlot(player);
        if (slot < 0) {
            return;
        }
        player.getInventory().setSelectedSlot(slot);

        Level level = player.level();
        while (index < targets.size()) {
            BlockPos target = targets.get(index);
            if (level.getBlockState(target).isAir() && placeAt(client, player, level, target)) {
                index++;
                return;
            }
            index++;
        }
        enabled = false;
        targets.clear();
        player.displayClientMessage(Component.literal("Стройка дома завершена"), false);
    }

    private static boolean placeAt(Minecraft client, LocalPlayer player, Level level, BlockPos target) {
        for (Direction d : Direction.values()) {
            BlockPos neighbor = target.relative(d);
            BlockState state = level.getBlockState(neighbor);
            if (level.isLoaded(neighbor) && !state.isAir() && state.getFluidState().isEmpty()) {
                player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(neighbor));
                BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(neighbor), d.getOpposite(), neighbor, false);
                client.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
                player.swing(InteractionHand.MAIN_HAND);
                return true;
            }
        }
        return false;
    }

    private static int findBlockSlot(LocalPlayer player) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getItem(i).getItem() instanceof BlockItem) {
                return i;
            }
        }
        return -1;
    }

    private static List<BlockPos> buildHouse(BlockPos a) {
        int w = 5;
        int d = 5;
        int h = 4;
        List<BlockPos> list = new ArrayList<>();
        for (int y = 0; y < h; y++) {
            for (int ix = 0; ix < w; ix++) {
                for (int iz = 0; iz < d; iz++) {
                    boolean floor = y == 0;
                    boolean ceil = y == h - 1;
                    boolean wall = ix == 0 || ix == w - 1 || iz == 0 || iz == d - 1;
                    boolean door = iz == 0 && ix == w / 2 && (y == 1 || y == 2);
                    boolean window = iz == d - 1 && (ix == 1 || ix == 3) && y == 2;
                    if (floor || ceil) {
                        list.add(a.offset(ix, y, iz));
                    } else if (wall && !door && !window) {
                        list.add(a.offset(ix, y, iz));
                    }
                }
            }
        }
        return list;
    }
}