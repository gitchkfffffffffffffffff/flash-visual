package com.example.client.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoadingOverlay.class)
public class LoadingOverlayMixin {
    @Shadow
    private float currentProgress;

    @Inject(method = "render", at = @At("TAIL"))
    private void flashVisual$progressBar(GuiGraphics gui, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        int w = gui.guiWidth();
        int h = gui.guiHeight();
        // нижняя полоса красивой загрузки
        int py = h - 18;
        gui.fill(0, py - 1, w, py, 0x22FFFFFF);
        gui.fill(0, py + 1, w, py + 3, 0x14FFFFFF);
        int prog = Math.max(1, Math.min(1_000_000, (int) (currentProgress * 1_000_000)));
        float f = prog / 1_000_000f;
        int barW = (int) (w * 0.55f);
        int bx = (w - barW) / 2;
        gui.fill(bx, py, bx + barW, py + 1, 0x66C0C0C0);
        gui.fill(bx, py, bx + (int) (barW * f), py + 1, 0xFFFFFFFF);
        gui.fill(bx, py - 1, bx + 2, py + 2, 0x99C0C0C0);
        gui.fill(bx + barW - 2, py - 1, bx + barW, py + 2, 0x99C0C0C0);
    }
}