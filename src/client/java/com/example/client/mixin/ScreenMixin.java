package com.example.client.mixin;

import com.example.client.CursorOverlay;
import com.example.client.Features;
import com.example.client.ScreenAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Renderable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin implements ScreenAccessor {
    @Shadow
    public int width;
    @Shadow
    public int height;
    @Shadow
    private java.util.List<GuiEventListener> children;
    @Shadow
    private java.util.List<NarratableEntry> narratables;
    @Shadow
    private java.util.List<Renderable> renderables;

    @Override
    public void flashVisual$addRenderableWidget(GuiEventListener widget) {
        this.children.add(widget);
        if (widget instanceof Renderable r) {
            this.renderables.add(r);
        }
        if (widget instanceof NarratableEntry n) {
            this.narratables.add(n);
        }
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("RETURN"))
    private void flashVisual$darkMenu(GuiGraphics gui, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (Features.darkMenu) {
            gui.fill(0, 0, this.width, this.height, 0x59000000);
        }
        CursorOverlay.render(gui);
    }
}
