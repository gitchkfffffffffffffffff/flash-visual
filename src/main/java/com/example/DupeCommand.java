package com.example;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DupeCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, registrationEnvironment) -> {
            var command = Commands.literal("dupe")
                .executes(context -> execute(context.getSource()));
            dispatcher.register(command);
            dispatcher.register(Commands.literal(".dupe").executes(context -> execute(context.getSource())));
        });
    }

    private static int execute(CommandSourceStack source) {
        Player player;
        try {
            player = source.getPlayerOrException();
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
            source.sendFailure(Component.literal("This command can only be run by a player!"));
            return 0;
        }

        ItemStack stack = player.getOffhandItem();
        if (stack.isEmpty()) {
            stack = player.getMainHandItem();
        }

        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("You must hold an item in your hand or offhand!"));
            return 0;
        }

        ItemStack duplicatedStack = stack.copy();
        ItemEntity itemEntity = new ItemEntity(
            player.level(),
            player.getX(),
            player.getY(),
            player.getZ(),
            duplicatedStack
        );

        player.level().addFreshEntity(itemEntity);
        source.sendSuccess(() -> Component.literal("Item duplicated!"), false);

        return 1;
    }
}
