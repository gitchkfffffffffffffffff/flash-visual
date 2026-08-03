package com.example.client.mixin;

import com.example.client.Features;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(FogRenderer.class)
public class FogRendererMixin {
    private static final String UBO =
        "Lnet/minecraft/client/renderer/fog/FogRenderer;updateBuffer(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V";

    @ModifyArg(method = "setupFog", at = @At(value = "INVOKE", target = UBO), index = 2)
    private Vector4f flashVisual$fogColor(Vector4f color) {
        if (Features.customFog) {
            color.set(Features.fogR(), Features.fogG(), Features.fogB(), 1.0f);
        }
        return color;
    }

    @ModifyArg(method = "setupFog", at = @At(value = "INVOKE", target = UBO), index = 3)
    private float flashVisual$envStart(float value) {
        return fogArg(value);
    }

    @ModifyArg(method = "setupFog", at = @At(value = "INVOKE", target = UBO), index = 4)
    private float flashVisual$envEnd(float value) {
        return fogArg(value);
    }

    @ModifyArg(method = "setupFog", at = @At(value = "INVOKE", target = UBO), index = 5)
    private float flashVisual$rdStart(float value) {
        return fogArg(value);
    }

    @ModifyArg(method = "setupFog", at = @At(value = "INVOKE", target = UBO), index = 6)
    private float flashVisual$rdEnd(float value) {
        return fogArg(value);
    }

    @ModifyArg(method = "setupFog", at = @At(value = "INVOKE", target = UBO), index = 7)
    private float flashVisual$skyEnd(float value) {
        return fogArg(value);
    }

    @ModifyArg(method = "setupFog", at = @At(value = "INVOKE", target = UBO), index = 8)
    private float flashVisual$cloudEnd(float value) {
        return fogArg(value);
    }

    private float fogArg(float value) {
        if (!Features.customFog) {
            return value;
        }
        float mult = Features.fogDistance();
        if (mult <= 0.0f) {
            return 10000.0f;
        }
        return Math.max(0.5f, value * mult);
    }
}
