package com.example.client.mixin;

import com.example.client.TimeChanger;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class LevelMixin {
    @Inject(method = "getDayTime", at = @At("HEAD"), cancellable = true)
    private void flashVisual$time(CallbackInfoReturnable<Long> cir) {
        if (TimeChanger.mode != TimeChanger.OFF) {
            cir.setReturnValue(TimeChanger.fixedTime());
        }
    }
}
