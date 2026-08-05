package com.example.client.mixin;

import com.example.client.Features;
import com.example.client.Ui;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiHudMixin {
    private static final int SLOT = 20;
    private static final int GAP = 2;
    private static final int SLOT_R = 6;

    @Inject(method = "renderPlayerHealth", at = @At("HEAD"), cancellable = true)
    private void flashVisual$health(GuiGraphics gui, CallbackInfo ci) {
        if (!Features.darkHud) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        ci.cancel();
        renderBars(gui, client);
    }

    private static void renderBars(GuiGraphics gui, Minecraft client) {
        int w = client.getWindow().getGuiScaledWidth();
        int h = client.getWindow().getGuiScaledHeight();
        net.minecraft.world.entity.player.Player player = client.player;
        int accent = Ui.PULSE_ACCENT;

        int barW = 182;
        int bx = w / 2 - barW / 2;
        int rowH = 8;
        int gap = 2;

        int foodY = h - 42;
        int hpY = foodY - rowH - gap;
        int armorY = hpY - rowH - gap;

        float hp = Math.max(0, player.getHealth());
        float maxHp = Math.max(1, player.getMaxHealth());
        float frac = Math.min(1, hp / maxHp);
        int hc = frac > 0.5f ? 0xFF8CC08C : (frac > 0.25f ? 0xFFC8C8C8 : 0xFFFF6B6B);
        Ui.panel(gui, bx - 3, hpY - 3, barW + 6, rowH + 6, Ui.PULSE_PANEL, Ui.PULSE_LINE);
        gui.fill(bx + 1, hpY + 1, bx + barW - 1, hpY + rowH - 1, 0x44000000);
        if (frac > 0) {
            gui.fill(bx + 1, hpY + 1, bx + 1 + (int) ((barW - 2) * frac), hpY + rowH - 1, hc);
        }
        gui.fill(bx + 1, hpY + 1, bx + 1 + (int) ((barW - 2) * frac), hpY + 2, Ui.mix(hc, 0xFFFFFF, 0.35f));
        String hpText = (int) Math.ceil(hp) + "/" + (int) maxHp;
        gui.drawCenteredString(client.font, hpText, w / 2, hpY - 2, 0xFFE6E6E6);

        int fy = foodY;
        net.minecraft.world.food.FoodData food = player.getFoodData();
        float foodFrac = Math.min(1, food.getFoodLevel() / 20f);
        int fc = foodFrac > 0.5f ? 0xFFB0B0B0 : (foodFrac > 0.25f ? 0xFF8C8C8C : 0xFFFF6B6B);
        Ui.panel(gui, bx - 3, fy - 3, barW + 6, rowH + 6, Ui.PULSE_PANEL, Ui.PULSE_LINE);
        gui.fill(bx + 1, fy + 1, bx + barW - 1, fy + rowH - 1, 0x44000000);
        if (foodFrac > 0) {
            gui.fill(bx + 1, fy + 1, bx + 1 + (int) ((barW - 2) * foodFrac), fy + rowH - 1, fc);
        }
        gui.fill(bx + 1, fy + 1, bx + 1 + (int) ((barW - 2) * foodFrac), fy + 2, Ui.mix(fc, 0xFFFFFF, 0.35f));
        String foodText = food.getFoodLevel() + "/20";
        gui.drawCenteredString(client.font, foodText, w / 2, fy - 2, 0xFFE6E6E6);

        int armor = player.getArmorValue();
        if (armor > 0) {
            int ay = armorY;
            float aFrac = Math.min(1, armor / 20f);
            Ui.panel(gui, bx - 3, ay - 3, barW + 6, rowH + 6, Ui.PULSE_PANEL, Ui.PULSE_LINE);
            gui.fill(bx + 1, ay + 1, bx + barW - 1, ay + rowH - 1, 0x44000000);
            if (aFrac > 0) {
                gui.fill(bx + 1, ay + 1, bx + 1 + (int) ((barW - 2) * aFrac), ay + rowH - 1, 0xFF9A9A9A);
            }
            gui.fill(bx + 1, ay + 1, bx + 1 + (int) ((barW - 2) * aFrac), ay + 2, Ui.mix(0xFF9A9A9A, 0xFFFFFF, 0.4f));
            String armorText = "Броня " + armor + "/20";
            gui.drawCenteredString(client.font, armorText, w / 2, ay - 2, 0xFFC9C9C9);
        }
    }

    @Inject(method = "renderOverlayMessage", at = @At("HEAD"), cancellable = true)
    private void flashVisual$actionbar(GuiGraphics gui, DeltaTracker delta, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
        at = @At("RETURN"))
    private void flashVisual$darkHud(GuiGraphics gui, DeltaTracker delta, CallbackInfo ci) {
        if (!Features.darkHud) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        int w = client.getWindow().getGuiScaledWidth();
        int h = client.getWindow().getGuiScaledHeight();
        int accent = Ui.PULSE_ACCENT;

        int n = 9;
        int barW = n * SLOT + (n - 1) * GAP;
        int bx = w / 2 - barW / 2 - 8;
        int by = h - 34;

        Ui.panel(gui, bx - 4, by - 4, barW + 16, SLOT + 14, Ui.PULSE_PANEL, Ui.PULSE_LINE);
        gui.fillGradient(bx - 4, by - 4, bx + 8, by - 3, accent, (accent & 0x00FFFFFF));

        for (int i = 0; i < n; i++) {
            int sx = bx + i * (SLOT + GAP);
            int sy = by;
            boolean selected = i == client.player.getInventory().getSelectedSlot();
            if (selected) {
                int glow = 0x33 << 24;
                Ui.roundRect(gui, sx - 4, sy - 4, SLOT + 8, SLOT + 8, SLOT_R + 2, glow | (accent & 0xFFFFFF));
                Ui.roundRect(gui, sx - 2, sy - 2, SLOT + 4, SLOT + 4, SLOT_R, accent);
                Ui.roundRect(gui, sx - 2, sy - 2, SLOT + 4, SLOT + 4, SLOT_R, Ui.mix(accent, 0x000000, 0.45f));
            } else {
                Ui.roundRect(gui, sx, sy, SLOT, SLOT, SLOT_R, Ui.PULSE_LINE);
            }
            Ui.roundRect(gui, sx + 1, sy + 1, SLOT - 2, SLOT - 2, SLOT_R, 0x8C000000);
            ItemStack stack = client.player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                Font font = client.font;
                gui.renderItem(stack, sx + 2, sy + 2);
                gui.renderItemDecorations(font, stack, sx + 2, sy + 2);
            }
        }
    }
}
