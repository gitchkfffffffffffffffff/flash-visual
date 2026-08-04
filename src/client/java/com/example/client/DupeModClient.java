package com.example.client;

import com.example.DupeHelloPayload;
import com.example.DupeHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class DupeModClient implements ClientModInitializer {
    private static boolean wasShiftDown = false;
    private static boolean wasHKeyDown = false;
    private static boolean wasF6Down = false;
    private static boolean wasF7Down = false;
    private static boolean wasF8Down = false;
    private static boolean wasF9Down = false;
    private static boolean wasF10Down = false;
    private static boolean wasF11Down = false;
    private static boolean wasPDown = false;
    private static boolean wasLDown = false;
    private static boolean wasSemicolonDown = false;    private static boolean wasF4Down = false;
    private static boolean wasF5Down = false;
    private static boolean wasF12Down = false;
    private static boolean wasKDown = false;
    private static boolean wasRDown = false;
    private static boolean wasZDown = false;
    private static boolean wasXDown = false;
    private static boolean wasVDown = false;
    private static boolean ghostBlockActive = false;
    private static BlockPos lastGhostBlockPos = null;
    private static boolean pendingChestOpen = false;
    private static int pendingChestTicks = 0;
    private static int rpcTick = 0;
    public static volatile boolean serverHasMod = false;

    @Override
    public void onInitializeClient() {
        DiscordRpc.init();
        HudRenderCallback.EVENT.register((gui, delta) ->
            HudRenderer.render(gui, delta, Minecraft.getInstance())
        );

        ClientPlayNetworking.registerGlobalReceiver(DupeHelloPayload.TYPE, (payload, context) -> {
            serverHasMod = true;
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            serverHasMod = false;
            pendingChestOpen = false;
            DupeChestScreen.clearBound();
            clearGhostBlock(client);
            FreeCam.disable(client);
            EspRenderer.clear(client);
            KillAura.enabled = false;
            Scaffold.enabled = false;
            AutoTotem.enabled = false;
        });

        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            if (message != null && message.trim().equalsIgnoreCase(".dupe")) {
                performDupe();
                return false;
            }
            return true;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getWindow() == null) {
                return;
            }
            long handle = client.getWindow().handle();
            boolean isShiftDown = GLFW.glfwGetKey(handle, Binds.get(Binds.MENU)) == GLFW.GLFW_PRESS;

            if (isShiftDown && !wasShiftDown) {
                if (client.screen == null) {
                    client.setScreen(new DupeGuiScreen());
                } else if (client.player != null) {
                    List<ItemStack> openItems = DupeHelper.collectContainerItems(client.player);
                    if (!openItems.isEmpty()) {
                        client.setScreen(new DupeChestScreen(openItems));
                    } else {
                        client.setScreen(null);
                    }
                }
            }
            wasShiftDown = isShiftDown;

            boolean inGame = client.screen == null;

            boolean isHKeyDown = GLFW.glfwGetKey(handle, Binds.get(Binds.GHOST)) == GLFW.GLFW_PRESS;
            if (inGame && isHKeyDown && !wasHKeyDown) {
                toggleGhostBlocks(client);
            }
            wasHKeyDown = isHKeyDown;

            HudRenderer.tick(client);
            HudRenderer.updateInteraction(client);
            FreeCam.tick(client);
            EspRenderer.tick(client);
            KillAura.tick(client);
            Scaffold.tick(client);
            AutoTotem.tick(client);
            updateDiscordPresence(client);

            boolean isF6Down = GLFW.glfwGetKey(handle, Binds.get(Binds.FREECAM)) == GLFW.GLFW_PRESS;
            if (inGame && isF6Down && !wasF6Down) {
                FreeCam.toggle(client);
            }
            wasF6Down = isF6Down;

            boolean isF7Down = GLFW.glfwGetKey(handle, Binds.get(Binds.MUSIC_HUD)) == GLFW.GLFW_PRESS;
            if (inGame && isF7Down && !wasF7Down) {
                HudRenderer.musicEnabled = !HudRenderer.musicEnabled;
                message(client, "Music HUD " + (HudRenderer.musicEnabled ? "ON" : "OFF"));
            }
            wasF7Down = isF7Down;

            boolean isF8Down = GLFW.glfwGetKey(handle, Binds.get(Binds.TARGET_HUD)) == GLFW.GLFW_PRESS;
            if (inGame && isF8Down && !wasF8Down) {
                HudRenderer.targetEnabled = !HudRenderer.targetEnabled;
                message(client, "Target HUD " + (HudRenderer.targetEnabled ? "ON" : "OFF"));
            }
            wasF8Down = isF8Down;

            boolean isF9Down = GLFW.glfwGetKey(handle, Binds.get(Binds.ESP_PLAYER)) == GLFW.GLFW_PRESS;
            if (inGame && isF9Down && !wasF9Down) {
                EspRenderer.playerEsp = !EspRenderer.playerEsp;
                message(client, "Player ESP " + (EspRenderer.playerEsp ? "ON" : "OFF"));
            }
            wasF9Down = isF9Down;

            boolean isF10Down = GLFW.glfwGetKey(handle, Binds.get(Binds.ESP_MOB)) == GLFW.GLFW_PRESS;
            if (inGame && isF10Down && !wasF10Down) {
                EspRenderer.mobEsp = !EspRenderer.mobEsp;
                message(client, "Mob ESP " + (EspRenderer.mobEsp ? "ON" : "OFF"));
            }
            wasF10Down = isF10Down;

            boolean isF11Down = GLFW.glfwGetKey(handle, Binds.get(Binds.ESP_ITEM)) == GLFW.GLFW_PRESS;
            if (inGame && isF11Down && !wasF11Down) {
                EspRenderer.itemEsp = !EspRenderer.itemEsp;
                message(client, "Item ESP " + (EspRenderer.itemEsp ? "ON" : "OFF"));
            }
            wasF11Down = isF11Down;

            boolean isF4Down = GLFW.glfwGetKey(handle, Binds.get(Binds.PREV)) == GLFW.GLFW_PRESS;
            if (inGame && isF4Down && !wasF4Down) {
                WinMusicReader.prev();
            }
            wasF4Down = isF4Down;

            boolean isF5Down = GLFW.glfwGetKey(handle, Binds.get(Binds.PLAY_PAUSE)) == GLFW.GLFW_PRESS;
            if (inGame && isF5Down && !wasF5Down) {
                WinMusicReader.playPause();
            }
            wasF5Down = isF5Down;

            boolean isF12Down = GLFW.glfwGetKey(handle, Binds.get(Binds.NEXT)) == GLFW.GLFW_PRESS;
            if (inGame && isF12Down && !wasF12Down) {
                WinMusicReader.next();
            }
            wasF12Down = isF12Down;

            boolean isPDown = GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_P) == GLFW.GLFW_PRESS;
            if (inGame && isPDown && !wasPDown) {
                togglePump(client);
            }
            wasPDown = isPDown;

            boolean isLDown = GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_L) == GLFW.GLFW_PRESS;
            if (inGame && isLDown && !wasLDown) {
                targetPump(client, false);
            }
            wasLDown = isLDown;

            boolean isSemicolonDown = GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_SEMICOLON) == GLFW.GLFW_PRESS;
            if (inGame && isSemicolonDown && !wasSemicolonDown) {
                targetPump(client, true);
            }
            wasSemicolonDown = isSemicolonDown;

            boolean isKDown = GLFW.glfwGetKey(handle, Binds.get(Binds.KILLAURA)) == GLFW.GLFW_PRESS;
            if (inGame && isKDown && !wasKDown) {
                KillAura.enabled = !KillAura.enabled;
                message(client, "KillAura " + (KillAura.enabled ? "ON" : "OFF"));
            }
            wasKDown = isKDown;

            boolean isRDown = GLFW.glfwGetKey(handle, Binds.get(Binds.SCAFFOLD)) == GLFW.GLFW_PRESS;
            if (inGame && isRDown && !wasRDown) {
                Scaffold.enabled = !Scaffold.enabled;
                message(client, "Scaffold " + (Scaffold.enabled ? "ON" : "OFF"));
            }
            wasRDown = isRDown;

            boolean isZDown = GLFW.glfwGetKey(handle, Binds.get(Binds.WITHDRAW)) == GLFW.GLFW_PRESS;
            if (isZDown && !wasZDown) {
                if (transferAll(client, true)) {
                    message(client, "Забрали всё");
                }
            }
            wasZDown = isZDown;

            boolean isXDown = GLFW.glfwGetKey(handle, Binds.get(Binds.DEPOSIT)) == GLFW.GLFW_PRESS;
            if (isXDown && !wasXDown) {
                if (transferAll(client, false)) {
                    message(client, "Положили всё");
                }
            }
            wasXDown = isXDown;

            boolean isVDown = GLFW.glfwGetKey(handle, Binds.get(Binds.AUTO_TOTEM)) == GLFW.GLFW_PRESS;
            if (inGame && isVDown && !wasVDown) {
                AutoTotem.enabled = !AutoTotem.enabled;
                message(client, "Auto Totem " + (AutoTotem.enabled ? "ON" : "OFF"));
            }
            wasVDown = isVDown;

            if (ghostBlockActive && client.player != null && client.level != null) {
                BlockPos below = client.player.blockPosition().below();
                if (client.level.getBlockState(below).isAir()) {
                    placeGhostBlock(client, below);
                }
            }

            if (pendingChestOpen && client.player != null) {
                pendingChestTicks++;
                List<ItemStack> items = DupeHelper.collectContainerItems(client.player);
                if (!items.isEmpty() || pendingChestTicks > 40) {
                    pendingChestOpen = false;
                    pendingChestTicks = 0;
                    if (!items.isEmpty()) {
                        client.setScreen(new DupeChestScreen(items));
                    }
                }
            }
        });
    }

    private static void updateDiscordPresence(Minecraft client) {
        if (!DiscordRpc.enabled) {
            return;
        }
        rpcTick++;
        if (rpcTick < 40) {
            return;
        }
        rpcTick = 0;
        String details;
        String state;
        String music = HudRenderer.getCurrentMusic();
        if (client.player == null) {
            details = "Flash Visual";
            state = "В меню";
        } else if (music != null && !music.isEmpty()) {
            details = "Слушает музыку";
            state = music.length() > 128 ? music.substring(0, 128) : music;
        } else {
            details = "Flash Visual";
            state = dimensionName(client);
        }
        if (client.player != null) {
            if (KillAura.enabled) {
                state += " · KillAura";
            }
            if (Scaffold.enabled) {
                state += " · Scaffold";
            }
            if (AutoTotem.enabled) {
                state += " · AutoTotem";
            }
            if (Features.customFog) {
                state += " · Fog";
            }
            if (Fullbright.enabled) {
                state += " · Fullbright";
            }
        }
        DiscordRpc.update(details, state);
    }

    private static String dimensionName(Minecraft client) {
        try {
            String p = client.player.level().dimension().identifier().getPath();
            if (p.contains("nether")) {
                return "Нижний мир";
            }
            if (p.contains("end")) {
                return "Энд";
            }
            return "Оверворлд";
        } catch (Throwable t) {
            return "В игре";
        }
    }

    public static void performDupe() {        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (player == null) {
            return;
        }

        if (client.isSingleplayer() || serverHasMod) {
            if (!player.getOffhandItem().isEmpty() || !player.getMainHandItem().isEmpty()) {
                client.getConnection().sendCommand("dupe");
                message(client, "Дюп: предмет продублирован на сервере");
                return;
            }
        }

        List<ItemStack> items = DupeHelper.collectContainerItems(player);
        if (items.isEmpty()) {
            items = collectTargetContainerItems(client);
        }
        if (!items.isEmpty()) {
            client.setScreen(new DupeChestScreen(items));
            return;
        }

        if (tryOpenTargetAndShowMenu(client)) {
            return;
        }

        if (client.isSingleplayer() || serverHasMod) {
            client.getConnection().sendCommand("dupe");
        } else {
            DupeHelper.attemptContainerSyncDupe(player);
            client.setScreen(null);
        }
    }

    public static boolean isGhostBlocksEnabled() {
        return ghostBlockActive;
    }

    private static void message(Minecraft client, String text) {
        if (client.player != null) {
            client.player.displayClientMessage(Component.literal(text), false);
        }
    }

    private static void togglePump(Minecraft client) {
        if (WorldVisuals.pumping) {
            WorldVisuals.pumping = false;
            WorldVisuals.cumStartMs = System.currentTimeMillis();
            message(client, "Кончил 🤍");
        } else {
            WorldVisuals.pumping = true;
            WorldVisuals.cumStartMs = -1;
            message(client, "Подрочи...");
        }
    }

    private static void targetPump(Minecraft client, boolean blowjobMode) {
        if (client.player == null || client.level == null) {
            return;
        }
        if (WorldVisuals.pumping && WorldVisuals.fuckTargetUuid != null && WorldVisuals.blowjob == blowjobMode) {
            WorldVisuals.fuckTargetUuid = null;
            WorldVisuals.pumping = false;
            WorldVisuals.blowjob = false;
            WorldVisuals.cumStartMs = -1;
            message(client, "Отпустил");
            return;
        }
        net.minecraft.world.phys.Vec3 eye = client.player.getEyePosition();
        net.minecraft.world.phys.Vec3 look = client.player.getLookAngle();
        double bestDot = 0.5;
        net.minecraft.world.entity.Entity best = null;
        for (net.minecraft.world.entity.Entity e : client.level.entitiesForRendering()) {
            if (e == client.player || !(e instanceof net.minecraft.world.entity.player.Player)) {
                continue;
            }
            net.minecraft.world.phys.Vec3 center = e.getBoundingBox().getCenter();
            double dist = eye.distanceTo(center);
            if (dist > 16.0) {
                continue;
            }
            double dot = center.subtract(eye).normalize().dot(look);
            if (dot > bestDot) {
                bestDot = dot;
                best = e;
            }
        }
        if (best == null) {
            message(client, "Никого нет перед камерой");
            return;
        }
        WorldVisuals.fuckTargetUuid = best.getUUID();
        WorldVisuals.pumping = true;
        WorldVisuals.blowjob = blowjobMode;
        WorldVisuals.cumStartMs = -1;
        WorldVisuals.autoCumDone = false;
        WorldVisuals.actStartMs = System.currentTimeMillis();
        message(client, blowjobMode ? "Минет " + best.getName().getString() : "Трахнул " + best.getName().getString());
    }

    public static void toggleGhostBlocks(Minecraft client) {
        ghostBlockActive = !ghostBlockActive;
        if (ghostBlockActive) {
            message(client, "Ghost blocks ON");
        } else {
            clearGhostBlock(client);
            message(client, "Ghost blocks OFF");
        }
    }

    public static void disableGhostBlocks(Minecraft client) {
        ghostBlockActive = false;
        clearGhostBlock(client);
    }

    private static void placeGhostBlock(Minecraft client, BlockPos pos) {
        if (lastGhostBlockPos != null && !lastGhostBlockPos.equals(pos)) {
            client.level.setBlock(lastGhostBlockPos, Blocks.AIR.defaultBlockState(), 0);
        }
        client.level.setBlock(pos, Blocks.BEDROCK.defaultBlockState(), 3);
        lastGhostBlockPos = pos;
    }

    private static void clearGhostBlock(Minecraft client) {
        if (lastGhostBlockPos != null) {
            client.level.setBlock(lastGhostBlockPos, Blocks.AIR.defaultBlockState(), 0);
            lastGhostBlockPos = null;
        }
    }

    private static boolean tryOpenTargetAndShowMenu(Minecraft client) {
        if (client.level == null || client.hitResult == null) {
            return false;
        }
        if (client.hitResult.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        BlockPos pos = ((BlockHitResult) client.hitResult).getBlockPos();
        BlockEntity blockEntity = client.level.getBlockEntity(pos);
        if (!(blockEntity instanceof Container)) {
            return false;
        }
        LocalPlayer player = client.player;
        if (player == null) {
            return false;
        }
        pendingChestOpen = true;
        pendingChestTicks = 0;
        try {
            client.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, (BlockHitResult) client.hitResult);
        } catch (Exception e) {
            pendingChestOpen = false;
        }
        return true;
    }

    private static boolean transferAll(Minecraft client, boolean withdraw) {
        if (client.player == null) {
            return false;
        }
        if (!(client.screen instanceof AbstractContainerScreen)) {
            return false;
        }
        AbstractContainerMenu menu = client.player.containerMenu;
        if (menu == null || menu instanceof InventoryMenu) {
            return false;
        }
        boolean moved = false;
        java.util.List<Slot> slots = menu.slots;
        int start = withdraw ? 0 : slots.size() - 1;
        int step = withdraw ? 1 : -1;
        for (int i = start; withdraw ? i < slots.size() : i >= 0; i += step) {
            Slot slot = slots.get(i);
            ItemStack before = slot.getItem();
            if (before.isEmpty()) {
                continue;
            }
            boolean isPlayerSlot = slot.container instanceof Inventory;
            if (isPlayerSlot == withdraw) {
                continue;
            }
            menu.quickMoveStack(client.player, i);
            ItemStack after = slot.getItem();
            if (after.isEmpty() || after.getCount() < before.getCount()) {
                moved = true;
            }
        }
        return moved;
    }

    private static List<ItemStack> collectTargetContainerItems(Minecraft client) {        List<ItemStack> result = new ArrayList<>();
        if (client.level == null || client.hitResult == null) {
            return result;
        }
        if (client.hitResult.getType() != HitResult.Type.BLOCK) {
            return result;
        }
        BlockPos pos = ((BlockHitResult) client.hitResult).getBlockPos();
        BlockEntity blockEntity = client.level.getBlockEntity(pos);
        if (blockEntity instanceof Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                if (!stack.isEmpty()) {
                    result.add(stack.copy());
                }
            }
        }
        return result;
    }
}
