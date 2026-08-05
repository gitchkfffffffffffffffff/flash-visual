package com.example.client;

import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Config {
    private static final Map<String, String> values = new LinkedHashMap<>();

    private static Path configFile(Minecraft client) {
        return client.gameDirectory.toPath()
            .resolve("config").resolve("flash-visual").resolve("config.txt");
    }

    public static void save() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.gameDirectory == null) {
            return;
        }
        values.clear();
        put("zoom.enabled", Zoom.enabled);
        put("zoom.factor", Zoom.factor);
        put("fullbright.enabled", Fullbright.enabled);
        put("killaura.enabled", KillAura.enabled);
        put("killaura.hitPlayers", KillAura.hitPlayers);
        put("killaura.range", KillAura.range);
        put("killaura.delay", KillAura.delay);
        put("scaffold.enabled", Scaffold.enabled);
        put("scaffold.delay", Scaffold.delay);
        put("autototem.enabled", AutoTotem.enabled);
        put("autototem.health", AutoTotem.healthThreshold);
        put("autototem.delay", AutoTotem.delay);
        put("house.enabled", HouseBuilder.enabled);
        put("house.delay", HouseBuilder.delay);
        put("time.mode", TimeChanger.mode);

        put("features.customFog", Features.customFog);
        put("features.fogColor", Features.fogColorIndex);
        put("features.fogDistance", Features.fogDistanceIndex);
        put("features.soundBoost", Features.soundBoost);
        put("features.quietWarden", Features.quietWarden);
        put("features.noRender", Features.noRender);
        put("features.showInvis", Features.showInvis);
        put("features.darkMenu", Features.darkMenu);
        put("features.beautifulMenu", Features.beautifulMenu);
        put("features.scoreboardTheme", Features.scoreboardTheme);
        put("features.darkHud", Features.darkHud);
        put("features.itemPhysics", Features.itemPhysics);

        put("hud.music", HudRenderer.musicEnabled);
        put("hud.target", HudRenderer.targetEnabled);
        put("hud.watermark", HudRenderer.watermarkEnabled);
        put("hud.fps", HudRenderer.fpsEnabled);
        put("hud.inventory", HudRenderer.inventoryEnabled);
        put("hud.potion", HudRenderer.potionEnabled);
        put("hud.enemies", HudRenderer.enemiesEnabled);
        put("hud.coords", CoordinatesHud.enabled);
        put("hud.staff", StaffHud.enabled);
        put("hud.scoreboard", ScoreboardHud.enabled);
        put("hud.minimap", Minimap.enabled);
        put("hud.jojo", JojoHud.enabled);
        put("hud.cursor", CursorOverlay.enabled);

        put("esp.player", EspRenderer.playerEsp);
        put("esp.mob", EspRenderer.mobEsp);
        put("esp.item", EspRenderer.itemEsp);

        put("visual.chinaHat", WorldVisuals.chinaHat);
        put("visual.jumpCircle", WorldVisuals.jumpCircle);
        put("visual.tracers", WorldVisuals.tracers);
        put("visual.tracersMobs", WorldVisuals.tracersMobs);
        put("visual.nameTag", WorldVisuals.nameTag);
        put("visual.enemyLabels", WorldVisuals.enemyLabels);
        put("visual.zhiguli", WorldVisuals.zhiguli);
        put("visual.zhiguliView", WorldVisuals.zhiguliView);
        put("visual.majorSuit", WorldVisuals.majorSuit);
        put("visual.skinOverride", WorldVisuals.skinOverride);
        put("visual.wings", WorldVisuals.wings);
        put("visual.near", WorldVisuals.near);
        put("visual.nearRange", WorldVisuals.nearRange);
        put("visual.hatScale", WorldVisuals.hatScale);
        put("visual.circleRadius", WorldVisuals.circleRadius);

        put("viewmodel.enabled", ViewModel.enabled);
        put("viewmodel.posX", ViewModel.posX);
        put("viewmodel.posY", ViewModel.posY);
        put("viewmodel.posZ", ViewModel.posZ);
        put("viewmodel.scale", ViewModel.scale);
        put("viewmodel.rotX", ViewModel.rotX);
        put("viewmodel.rotY", ViewModel.rotY);
        put("viewmodel.rotZ", ViewModel.rotZ);

        put("streamer.enabled", StreamerMode.enabled);
        put("search.enabled", PlayerSearch.enabled);
        put("search.minLength", PlayerSearch.minLength);
        put("exploit.break", ItemExploit.breakEnabled);

        put("discord.enabled", DiscordRpc.enabled);
        put("discord.showDetails", DiscordRpc.showDetails);

        put("theme.index", Theme.indexForSave());
        put("theme.anime", Theme.isAnime());

        for (String action : Binds.actions()) {
            put("bind." + action, Binds.get(action));
        }
        for (Map.Entry<String, int[]> e : HudPos.all().entrySet()) {
            int[] p = e.getValue();
            put("hudpos." + e.getKey(), p[0] + "," + p[1]);
        }
        put("friends", String.join(";", Friends.all()));

        Path dir = configFile(client).getParent();
        try {
            Files.createDirectories(dir);
            List<String> lines = new ArrayList<>();
            for (Map.Entry<String, String> e : values.entrySet()) {
                lines.add(e.getKey() + "=" + e.getValue());
            }
            try (OutputStream os = Files.newOutputStream(configFile(client))) {
                for (int i = 0; i < lines.size(); i++) {
                    if (i > 0) {
                        os.write('\n');
                    }
                    os.write(lines.get(i).getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (IOException ignored) {
        }
    }

    public static void load() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.gameDirectory == null) {
            return;
        }
        values.clear();
        Path f = configFile(client);
        if (!Files.exists(f)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(f, StandardCharsets.UTF_8)) {
                String t = line.trim();
                if (t.isEmpty() || t.startsWith("#")) {
                    continue;
                }
                int eq = t.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                values.put(t.substring(0, eq).trim(), t.substring(eq + 1).trim());
            }
        } catch (IOException ignored) {
        }

        Zoom.enabled = getBool("zoom.enabled", Zoom.enabled);
        Zoom.factor = getDouble("zoom.factor", Zoom.factor);
        Fullbright.enabled = getBool("fullbright.enabled", Fullbright.enabled);
        KillAura.enabled = getBool("killaura.enabled", KillAura.enabled);
        KillAura.hitPlayers = getBool("killaura.hitPlayers", KillAura.hitPlayers);
        KillAura.range = getDouble("killaura.range", KillAura.range);
        KillAura.delay = getInt("killaura.delay", KillAura.delay);
        Scaffold.enabled = getBool("scaffold.enabled", Scaffold.enabled);
        Scaffold.delay = getInt("scaffold.delay", Scaffold.delay);
        AutoTotem.enabled = getBool("autototem.enabled", AutoTotem.enabled);
        AutoTotem.healthThreshold = getFloat("autototem.health", AutoTotem.healthThreshold);
        AutoTotem.delay = getInt("autototem.delay", AutoTotem.delay);
        HouseBuilder.enabled = getBool("house.enabled", HouseBuilder.enabled);
        HouseBuilder.delay = getInt("house.delay", HouseBuilder.delay);
        TimeChanger.mode = getInt("time.mode", TimeChanger.mode);

        Features.customFog = getBool("features.customFog", Features.customFog);
        Features.fogColorIndex = getInt("features.fogColor", Features.fogColorIndex);
        Features.fogDistanceIndex = getInt("features.fogDistance", Features.fogDistanceIndex);
        Features.soundBoost = getBool("features.soundBoost", Features.soundBoost);
        Features.quietWarden = getBool("features.quietWarden", Features.quietWarden);
        Features.noRender = getBool("features.noRender", Features.noRender);
        Features.showInvis = getBool("features.showInvis", Features.showInvis);
        Features.darkMenu = getBool("features.darkMenu", Features.darkMenu);
        Features.beautifulMenu = getBool("features.beautifulMenu", Features.beautifulMenu);
        Features.scoreboardTheme = getBool("features.scoreboardTheme", Features.scoreboardTheme);
        Features.darkHud = getBool("features.darkHud", Features.darkHud);
        Features.itemPhysics = getBool("features.itemPhysics", Features.itemPhysics);

        HudRenderer.musicEnabled = getBool("hud.music", HudRenderer.musicEnabled);
        HudRenderer.targetEnabled = getBool("hud.target", HudRenderer.targetEnabled);
        HudRenderer.watermarkEnabled = getBool("hud.watermark", HudRenderer.watermarkEnabled);
        HudRenderer.fpsEnabled = getBool("hud.fps", HudRenderer.fpsEnabled);
        HudRenderer.inventoryEnabled = getBool("hud.inventory", HudRenderer.inventoryEnabled);
        HudRenderer.potionEnabled = getBool("hud.potion", HudRenderer.potionEnabled);
        HudRenderer.enemiesEnabled = getBool("hud.enemies", HudRenderer.enemiesEnabled);
        CoordinatesHud.enabled = getBool("hud.coords", CoordinatesHud.enabled);
        StaffHud.enabled = getBool("hud.staff", StaffHud.enabled);
        ScoreboardHud.enabled = getBool("hud.scoreboard", ScoreboardHud.enabled);
        Minimap.enabled = getBool("hud.minimap", Minimap.enabled);
        JojoHud.enabled = getBool("hud.jojo", JojoHud.enabled);
        CursorOverlay.enabled = getBool("hud.cursor", CursorOverlay.enabled);

        EspRenderer.playerEsp = getBool("esp.player", EspRenderer.playerEsp);
        EspRenderer.mobEsp = getBool("esp.mob", EspRenderer.mobEsp);
        EspRenderer.itemEsp = getBool("esp.item", EspRenderer.itemEsp);

        WorldVisuals.chinaHat = getBool("visual.chinaHat", WorldVisuals.chinaHat);
        WorldVisuals.jumpCircle = getBool("visual.jumpCircle", WorldVisuals.jumpCircle);
        WorldVisuals.tracers = getBool("visual.tracers", WorldVisuals.tracers);
        WorldVisuals.tracersMobs = getBool("visual.tracersMobs", WorldVisuals.tracersMobs);
        WorldVisuals.nameTag = getBool("visual.nameTag", WorldVisuals.nameTag);
        WorldVisuals.enemyLabels = getBool("visual.enemyLabels", WorldVisuals.enemyLabels);
        WorldVisuals.zhiguli = getBool("visual.zhiguli", WorldVisuals.zhiguli);
        WorldVisuals.zhiguliView = getBool("visual.zhiguliView", WorldVisuals.zhiguliView);
        WorldVisuals.majorSuit = getBool("visual.majorSuit", WorldVisuals.majorSuit);
        WorldVisuals.skinOverride = getBool("visual.skinOverride", WorldVisuals.skinOverride);
        WorldVisuals.wings = getBool("visual.wings", WorldVisuals.wings);
        WorldVisuals.near = getBool("visual.near", WorldVisuals.near);
        WorldVisuals.nearRange = getDouble("visual.nearRange", WorldVisuals.nearRange);
        WorldVisuals.hatScale = getFloat("visual.hatScale", WorldVisuals.hatScale);
        WorldVisuals.circleRadius = getFloat("visual.circleRadius", WorldVisuals.circleRadius);

        ViewModel.enabled = getBool("viewmodel.enabled", ViewModel.enabled);
        ViewModel.posX = getFloat("viewmodel.posX", ViewModel.posX);
        ViewModel.posY = getFloat("viewmodel.posY", ViewModel.posY);
        ViewModel.posZ = getFloat("viewmodel.posZ", ViewModel.posZ);
        ViewModel.scale = getFloat("viewmodel.scale", ViewModel.scale);
        ViewModel.rotX = getFloat("viewmodel.rotX", ViewModel.rotX);
        ViewModel.rotY = getFloat("viewmodel.rotY", ViewModel.rotY);
        ViewModel.rotZ = getFloat("viewmodel.rotZ", ViewModel.rotZ);

        StreamerMode.enabled = getBool("streamer.enabled", StreamerMode.enabled);
        PlayerSearch.enabled = getBool("search.enabled", PlayerSearch.enabled);
        PlayerSearch.minLength = getInt("search.minLength", PlayerSearch.minLength);
        ItemExploit.breakEnabled = getBool("exploit.break", ItemExploit.breakEnabled);

        DiscordRpc.enabled = getBool("discord.enabled", DiscordRpc.enabled);
        DiscordRpc.showDetails = getBool("discord.showDetails", DiscordRpc.showDetails);

        Theme.loadFromSave(getInt("theme.index", Theme.indexForSave()), getBool("theme.anime", Theme.isAnime()));

        for (String action : Binds.actions()) {
            int v = getInt("bind." + action, Binds.get(action));
            if (v != 0) {
                Binds.set(action, v);
            }
        }
        for (Map.Entry<String, int[]> e : HudPos.all().entrySet()) {
            String raw = get("hudpos." + e.getKey(), null);
            if (raw != null) {
                String[] p = raw.split(",");
                if (p.length == 2) {
                    try {
                        int[] pos = {Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim())};
                        HudPos.set(e.getKey(), pos);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        String fr = get("friends", null);
        if (fr != null) {
            Friends.clear();
            for (String n : fr.split(";")) {
                if (!n.isEmpty()) {
                    Friends.add(n);
                }
            }
        }

        Theme.apply();
    }

    private static void put(String key, boolean v) {
        values.put(key, v ? "1" : "0");
    }

    private static void put(String key, int v) {
        values.put(key, String.valueOf(v));
    }

    private static void put(String key, float v) {
        values.put(key, String.valueOf(v));
    }

    private static void put(String key, double v) {
        values.put(key, String.valueOf(v));
    }

    private static void put(String key, String v) {
        values.put(key, v);
    }

    private static String get(String key, String def) {
        return values.getOrDefault(key, def);
    }

    private static boolean getBool(String key, boolean def) {
        String v = values.get(key);
        if (v == null) {
            return def;
        }
        return v.equals("1") || v.equalsIgnoreCase("true");
    }

    private static int getInt(String key, int def) {
        try {
            return Integer.parseInt(values.getOrDefault(key, String.valueOf(def)));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static float getFloat(String key, float def) {
        try {
            return Float.parseFloat(values.getOrDefault(key, String.valueOf(def)));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static double getDouble(String key, double def) {
        try {
            return Double.parseDouble(values.getOrDefault(key, String.valueOf(def)));
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
