package com.example.client.mixin;

import com.example.client.Features;
import com.example.client.Ui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractButton.class)
public class ButtonsMixin {
    @Inject(method = "renderWidget(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
        at = @At("HEAD"), cancellable = true)
    private void flashVisual$render(GuiGraphics gui, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!Features.beautifulMenu) {
            return;
        }
        ci.cancel();
        net.minecraft.client.gui.components.AbstractWidget wd = (net.minecraft.client.gui.components.AbstractWidget) (Object) this;
        int x = wd.getX();
        int y = wd.getY();
        int w = wd.getWidth();
        int h = wd.getHeight();
        boolean hover = wd.isHovered();

        int bg = hover ? 0xFF161616 : 0xFF000000;
        Ui.panel(gui, x - 1, y - 1, w + 2, h + 2, bg, hover ? 0xFF1E1E1E : 0xFF101010);

        Font font = Minecraft.getInstance().font;
        Component message = wd.getMessage();
        int c = x + w / 2;
        if (hover) {
            gui.drawString(font, message, c - font.width(message) / 2, y + (h - 8) / 2, 0xFFDDDDDD);
        } else {
            gui.drawString(font, message, c - font.width(message) / 2, y + (h - 8) / 2, 0xFFAAAAAA);
        }
    }

    private static int[] blend(int c1, int c2, float t) {
        return new int[]{
            (int) (((c1 >> 16) & 0xFF) + (((c2 >> 16) & 0xFF) - ((c1 >> 16) & 0xFF)) * t),
            (int) (((c1 >> 8) & 0xFF) + (((c2 >> 8) & 0xFF) - ((c1 >> 8) & 0xFF)) * t),
            (int) ((c1 & 0xFF) + ((c2 & 0xFF) - (c1 & 0xFF)) * t)
        };
    }

    private static int[] blend3(float p, int a, int b, int c) {
        if (p < 0.5f) {
            return blend(a, b, p * 2f);
        }
        return blend(b, c, (p - 0.5f) * 2f);
    }

    private static int rgb(int r, int g, int b) {
        return 0xFF000000 | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }
}