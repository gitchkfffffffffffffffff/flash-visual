package com.example.client.mixin;

import com.example.client.Features;
import com.example.client.Ui;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiHudMixin {
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

        Ui.panel(gui, w / 2 - 104, h - 58, 208, 26, 0xA6000000, 0xFF22242A);
        Ui.panel(gui, w / 2 - 100, h - 31, 200, 29, 0xB3000000, 0xFF22242A);
        gui.fill(w / 2 - 104, h - 58, w / 2 + 104, h - 57, accent);
        gui.fill(w / 2 - 100, h - 31, w / 2 + 100, h - 30, accent);
    }
}