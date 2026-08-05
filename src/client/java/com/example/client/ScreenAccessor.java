package com.example.client;

import net.minecraft.client.gui.components.events.GuiEventListener;

public interface ScreenAccessor {
    void flashVisual$addRenderableWidget(GuiEventListener widget);
}
