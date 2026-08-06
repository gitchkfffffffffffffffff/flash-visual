package com.example.client.mixin;

import com.example.client.SkinChanger;
import com.example.client.WorldVisuals;
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
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
        at = @At("TAIL"))
    private void flashVisual$swapSkin(Avatar entity, AvatarRenderState state, float partialTick, CallbackInfo ci) {
        if (!WorldVisuals.skinOverride || !(entity instanceof Player p)) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || p == mc.player) {
            return;
        }
        if (!WorldVisuals.skinTargetName.isEmpty() && !p.getName().getString().equalsIgnoreCase(WorldVisuals.skinTargetName)) {
            return;
        }
        PlayerSkin custom = SkinChanger.getCustomSkin();
        if (custom != null) {
            state.skin = custom;
            return;
        }
        PlayerSkin local = mc.getSkinManager().createLookup(mc.player.getGameProfile(), false).get();
        if (local != null) {
            state.skin = local;
        }
    }
}
