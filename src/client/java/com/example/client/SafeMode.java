package com.example.client;

import net.minecraft.client.Minecraft;

public class SafeMode {
    public static boolean enabled = false;

    public static void apply(Minecraft client) {
        KillAura.enabled = false;
        Scaffold.enabled = false;
        Features.showInvis = false;
        Features.noRender = false;
        TimeChanger.mode = TimeChanger.OFF;
        EspRenderer.playerEsp = false;
        EspRenderer.mobEsp = false;
        EspRenderer.itemEsp = false;
        EspRenderer.clear(client);
        if (Fullbright.enabled) {
            Fullbright.toggle(client);
        }
        if (FreeCam.isActive()) {
            FreeCam.disable(client);
        }
        DupeModClient.disableGhostBlocks(client);
    }
}
