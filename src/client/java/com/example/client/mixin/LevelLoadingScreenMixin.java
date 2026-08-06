package com.example.client.mixin;

import com.example.client.Ui;
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
        // светлый серо-белый фон
        gui.fillGradient(0, 0, w, h, 0xD8E0E0E0, 0xD8F2F2F2);
        // скруглённая панель с полосой загрузки
        int pw = Math.min(420, w - 80);
        int px = (w - pw) / 2;
        int py = h - 32;
        Ui.roundRect(gui, px - 10, py - 12, pw + 20, 40, 12, 0xEFFFFFFF);
        float f = Math.max(0f, Math.min(1f, smoothedProgress));
        Ui.roundRect(gui, px, py, pw, 5, 3, 0x66A0A0A0);
        int fill = (int) (Math.max(1, (pw - 2) * f));
        Ui.roundRect(gui, px, py, Math.max(2, fill), 5, 3, 0xFF3A3A3A);
        net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
        if (font != null) {
            gui.drawCenteredString(font, "Загрузка мира", px + pw / 2, py - 12, 0xFF4A4A4A);
        }
    }
}