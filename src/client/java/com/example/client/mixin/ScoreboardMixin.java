package com.example.client.mixin;

import com.example.client.Features;
import com.example.client.ScoreboardHud;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class ScoreboardMixin {
    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
        at = @At("HEAD"), cancellable = true)
    private void flashVisual$scoreboard(GuiGraphics gui, DeltaTracker delta, CallbackInfo ci) {
        if (!Features.scoreboardTheme) {
            return;
        }
        ci.cancel();
        ScoreboardHud.render(gui, Minecraft.getInstance());
    }
}