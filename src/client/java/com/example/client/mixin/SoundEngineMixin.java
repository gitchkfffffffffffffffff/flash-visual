package com.example.client.mixin;

import com.example.client.Features;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {
    private static final String CALC =
        "Lnet/minecraft/client/sounds/SoundEngine;calculateVolume(Lnet/minecraft/client/resources/sounds/SoundInstance;)F";

    @Shadow
    private float calculateVolume(SoundInstance sound) {
        throw new AssertionError();
    }

    @Redirect(method = "method_76567", at = @At(value = "INVOKE", target = CALC))
    private float flashVisual$volume(SoundEngine instance, SoundInstance sound) {
        return modifiedVolume(sound);
    }

    @Redirect(method = "tickInGameSound", at = @At(value = "INVOKE", target = CALC))
    private float flashVisual$volumeTick(SoundEngine instance, SoundInstance sound) {
        return modifiedVolume(sound);
    }

    private float modifiedVolume(SoundInstance sound) {
        float v = this.calculateVolume(sound);
        if (Features.soundBoost) {
            v = Math.min(3.0f, v * 1.6f);
        }
        if (Features.quietWarden && sound != null && sound.getIdentifier() != null) {
            String path = sound.getIdentifier().getPath();
            if (path != null && path.contains("warden")) {
                v *= 0.1f;
            }
        }
        return v;
    }
}
