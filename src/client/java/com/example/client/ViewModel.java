package com.example.client;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;

public class ViewModel {
    public static boolean enabled = false;
    public static float posX = 0.0f;
    public static float posY = 0.0f;
    public static float posZ = 0.0f;
    public static float scale = 1.0f;
    public static float rotX = 0.0f;
    public static float rotY = 0.0f;
    public static float rotZ = 0.0f;

    public static void apply(PoseStack poseStack) {
        if (!enabled) {
            return;
        }
        poseStack.translate(posX, posY, posZ);
        poseStack.mulPose(new Quaternionf().rotationX((float) Math.toRadians(rotX)));
        poseStack.mulPose(new Quaternionf().rotationY((float) Math.toRadians(rotY)));
        poseStack.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(rotZ)));
        poseStack.scale(scale, scale, scale);
    }
}