package com.example.client.mixin;

import com.example.client.Features;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void flashVisual$hideWarden(Entity entity, Frustum frustum, double x, double y, double z,
                                        CallbackInfoReturnable<Boolean> cir) {
        if (Features.noRender && entity != null && entity.getType() == EntityType.WARDEN) {
            cir.setReturnValue(false);
        }
    }
}
