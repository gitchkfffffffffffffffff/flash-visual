package com.example.client.mixin;

import com.example.client.CursorOverlay;
import com.example.client.Features;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {
    @Shadow
    public int width;
    @Shadow
    public int height;

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("RETURN"))
    private void flashVisual$darkMenu(GuiGraphics gui, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (Features.darkMenu) {
            gui.fill(0, 0, this.width, this.height, 0x59000000);
        }
        CursorOverlay.render(gui);
    }
}
