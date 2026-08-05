package com.example.client.mixin;

import com.example.client.Features;
import com.example.client.ViewModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Inject(method = "applyItemArmTransform", at = @At("RETURN"))
    private void flashVisual$viewModel(PoseStack poseStack, HumanoidArm arm, float equipProgress, CallbackInfo ci) {
        ViewModel.apply(poseStack);
    }

    @Inject(method = "applyItemArmTransform", at = @At("HEAD"))
    private void flashVisual$itemPhysics(PoseStack poseStack, HumanoidArm arm, float equipProgress, CallbackInfo ci) {
        if (!Features.itemPhysics) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        net.minecraft.world.phys.Vec3 mv = client.player.getDeltaMovement();
        double sp = Math.sqrt(mv.x * mv.x + mv.z * mv.z);
        if (sp < 0.02) {
            return;
        }
        long t = System.currentTimeMillis();
        float sway = (float) (Math.sin(t / 130.0) * Math.min(1.0, sp * 6.0) * 14.0);
        poseStack.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(sway)));
        poseStack.mulPose(new Quaternionf().rotationX((float) Math.toRadians(sway * 0.5)));
        poseStack.translate(0, (float) (Math.sin(t / 170.0) * Math.min(1.0, sp * 6.0) * 0.006), 0);
    }
}