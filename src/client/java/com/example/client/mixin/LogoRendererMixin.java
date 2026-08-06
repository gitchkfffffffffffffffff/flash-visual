package com.example.client.mixin;

import com.example.client.Features;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LogoRenderer.class)
public class LogoRendererMixin {
    @Inject(method = "renderLogo(Lnet/minecraft/client/gui/GuiGraphics;IF)V",
        at = @At("HEAD"), cancellable = true)
    private void flashVisual$hide(GuiGraphics gui, int heightOffset, float alpha, CallbackInfo ci) {
        if (Minecraft.getInstance().screen instanceof TitleScreen) {
            ci.cancel();
        }
    }
}