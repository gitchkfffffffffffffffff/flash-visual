package com.example.client.mixin;

import com.example.client.FreeLook;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {
    @Shadow
    protected void setRotation(float yaw, float pitch) {
    }

    @Shadow
    public float yaw() {
        return 0;
    }

    @Shadow
    public float xRot() {
        return 0;
    }

    @Inject(method = "setup(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;ZZF)V",
        at = @At("TAIL"))
    private void flashVisual$freeLook(Level level, Entity entity, boolean b1, boolean b2, float partialTick, CallbackInfo ci) {
        if (!FreeLook.isActive()) {
            return;
        }
        this.setRotation(this.yaw() + FreeLook.yawOffset(), this.xRot() + FreeLook.pitchOffset());
    }
}