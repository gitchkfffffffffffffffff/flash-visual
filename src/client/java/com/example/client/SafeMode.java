package com.example.client;

import net.minecraft.client.Minecraft;

public class SafeMode {
    public static boolean enabled = false;

    public static void apply(Minecraft client) {
        enabled = true;
        KillAura.enabled = false;
        Scaffold.enabled = false;
        Features.showInvis = false;
        Features.noRender = false;
        Features.customFog = false;
        Features.darkHud = false;
        Features.scoreboardTheme = false;
        Features.beautifulMenu = false;
        TimeChanger.mode = TimeChanger.OFF;
        EspRenderer.playerEsp = false;
        EspRenderer.mobEsp = false;
        EspRenderer.itemEsp = false;
        EspRenderer.clear(client);
        WorldVisuals.chinaHat = false;
        WorldVisuals.jumpCircle = false;
        WorldVisuals.tracers = false;
        WorldVisuals.tracersMobs = false;
        WorldVisuals.nameTag = false;
        WorldVisuals.enemyLabels = false;
        WorldVisuals.zhiguli = false;
        WorldVisuals.zhiguliView = false;
        WorldVisuals.majorSuit = false;
        WorldVisuals.wings = false;
        WorldVisuals.near = false;
        HudRenderer.musicEnabled = false;
        HudRenderer.targetEnabled = false;
        HudRenderer.watermarkEnabled = false;
        HudRenderer.fpsEnabled = false;
        HudRenderer.inventoryEnabled = false;
        HudRenderer.potionEnabled = false;
        HudRenderer.enemiesEnabled = false;
        Minimap.enabled = false;
        StaffHud.enabled = false;
        CoordinatesHud.enabled = false;
        JojoHud.enabled = false;
        CursorOverlay.enabled = false;
        CursorOverlay.restore();
        ViewModel.enabled = false;
        Zoom.enabled = false;
        if (Fullbright.enabled) {
            Fullbright.toggle(client);
        }
        if (FreeCam.isActive()) {
            FreeCam.disable(client);
        }
        if (FakePlayer.active()) {
            FakePlayer.despawn(client);
        }
        CheatModules.disableAll(client);
        DupeModClient.disableGhostBlocks(client);
    }
}