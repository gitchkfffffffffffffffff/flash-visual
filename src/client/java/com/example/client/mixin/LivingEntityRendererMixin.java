package com.example.client.mixin;

import com.example.client.Features;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Inject(method = "isBodyVisible(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)Z",
        at = @At("HEAD"), cancellable = true)
    private void flashVisual$showInvis(LivingEntityRenderState state, CallbackInfoReturnable<Boolean> cir) {
        if (Features.showInvis) {
            state.isInvisibleToPlayer = false;
            cir.setReturnValue(true);
        }
    }
}
