package com.example.client.mixin;

import com.example.client.Ui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.Shadow;
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
        int prog = Math.max(1, Math.min(1_000_000, (int) (currentProgress * 1_000_000)));
        float f = prog / 1_000_000f;
        int barW = (int) (w * 0.55f);
        int bx = (w - barW) / 2;
        Ui.roundRect(gui, bx, py, barW, 3, 2, 0x66C0C0C0);
        Ui.roundRect(gui, bx, py, Math.max(2, (int) (barW * f)), 3, 2, 0xFFFFFFFF);
    }
}