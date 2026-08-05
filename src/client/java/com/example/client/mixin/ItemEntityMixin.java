package com.example.client.mixin;

import com.example.client.Features;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Inject(method = "getSpin(FF)F", at = @At("RETURN"), cancellable = true)
    private static void flashVisual$spin(float bob, float spin, CallbackInfoReturnable<Float> cir) {
        if (!Features.itemPhysics) {
            return;
        }
        long t = System.currentTimeMillis();
        cir.setReturnValue(cir.getReturnValue() + (float) (Math.sin(t / 120.0) * 1.3) + (float) (Math.cos(t / 200.0) * 0.8));
    }
}