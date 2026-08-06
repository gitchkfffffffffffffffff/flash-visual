package com.example.client.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelLoadingScreen.class)
public class LevelLoadingScreenMixin {
    @Shadow
    private float smoothedProgress;

    @Inject(method = "render", at = @At("TAIL"))
    private void flashVisual$progressBar(GuiGraphics gui, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        int w = gui.guiWidth();
        int h = gui.guiHeight();
        // затемнённый фон вместо серого
        gui.fillGradient(0, 0, w, h, 0xC0080B14, 0xC00B0F1A);
        // приятная полоса загрузки
        int pw = Math.min(420, w - 60);
        int px = (w - pw) / 2;
        int py = h - 24;
        float f = Math.max(0f, Math.min(1f, smoothedProgress));
        gui.fill(px, py, px + pw, py + 2, 0x33FFFFFF);
        gui.fill(px, py, px + (int) (pw * f), py + 2, 0xFFFFB300);
        gui.fill(px - 2, py - 2, px, py + 4, 0x22FFFFFF);
        gui.fill(px + pw, py - 2, px + pw + 2, py + 4, 0x22FFFFFF);
        net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
        if (font != null) {
            gui.drawCenteredString(font, "Загрузка мира", px + pw / 2, py + 6, 0xFF9A9A9A);
        }
    }
}