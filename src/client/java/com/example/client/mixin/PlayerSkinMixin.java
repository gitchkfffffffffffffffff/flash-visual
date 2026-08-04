package com.example.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public class PlayerSkinMixin {
    public static boolean enabled = true;
    public static String targetName = "iamknow";

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
        at = @At("TAIL"))
    private void flashVisual$swapSkin(Avatar entity, AvatarRenderState state, float partialTick, CallbackInfo ci) {
        if (!enabled || !(entity instanceof Player p)) {
            return;
        }
        String name = p.getName().getString();
        if (name == null || !name.equalsIgnoreCase(targetName)) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            PlayerSkin local = mc.getSkinManager().createLookup(mc.player.getGameProfile(), false).get();
            if (local != null) {
                state.skin = local;
            }
        }
    }
}
