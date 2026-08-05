package com.example.client.mixin;

import com.example.client.FreeLook;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Shadow
    private double accumulatedDX;

    @Shadow
    private double accumulatedDY;

    @Inject(method = "turnPlayer(D)V", at = @At("HEAD"), cancellable = true)
    private void flashVisual$freeLook(double indicator, CallbackInfo ci) {
        if (!FreeLook.isActive()) {
            return;
        }
        FreeLook.addDelta(this.accumulatedDX, this.accumulatedDY);
        ci.cancel();
    }
}