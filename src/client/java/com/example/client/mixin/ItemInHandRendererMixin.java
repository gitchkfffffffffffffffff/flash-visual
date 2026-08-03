package com.example.client.mixin;

import com.example.client.ViewModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Inject(method = "applyItemArmTransform", at = @At("HEAD"))
    private void flashVisual$viewModel(PoseStack poseStack, HumanoidArm arm, float equipProgress, CallbackInfo ci) {
        ViewModel.apply(poseStack);
    }
}
