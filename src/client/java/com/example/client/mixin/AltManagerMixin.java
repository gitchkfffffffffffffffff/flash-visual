package com.example.client.mixin;

import com.example.client.AltManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class AltManagerMixin {
    @Inject(method = "getUser", at = @At("HEAD"), cancellable = true)
    private void flashVisual$altUser(CallbackInfoReturnable<User> cir) {
        User alt = AltManager.currentUser();
        if (alt != null) {
            cir.setReturnValue(alt);
        }
    }
}
