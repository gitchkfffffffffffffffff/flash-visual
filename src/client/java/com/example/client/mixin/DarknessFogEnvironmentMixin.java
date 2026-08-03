package com.example.client.mixin;

import com.example.client.Features;
import net.minecraft.client.renderer.fog.environment.DarknessFogEnvironment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DarknessFogEnvironment.class)
public class DarknessFogEnvironmentMixin {

    @Inject(method = "isApplicable", at = @At("HEAD"), cancellable = true)
    private void flashVisual$noDarkness(FogType fogType, Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (Features.noRender) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getModifiedDarkness", at = @At("HEAD"), cancellable = true)
    private void flashVisual$noDarknessValue(LivingEntity entity, float a, float b,
                                             CallbackInfoReturnable<Float> cir) {
        if (Features.noRender) {
            cir.setReturnValue(0.0f);
        }
    }
}
