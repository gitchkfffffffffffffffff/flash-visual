package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.User;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class AltManager {
    private static final List<String> alts = new ArrayList<>();
    private static String active = null;

    private static Path configFile(Minecraft client) {
        return client.gameDirectory.toPath()
            .resolve("config").resolve("flash-visual").resolve("alts.txt");
    }

    public static void load(Minecraft client) {
        if (client == null) {
            return;
        }
        alts.clear();
        active = null;
        Path f = configFile(client);
        if (!Files.exists(f)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(f, StandardCharsets.UTF_8)) {
                String t = line.trim();
                if (t.isEmpty()) {
                    continue;
                }
                if (t.startsWith("#active=")) {
                    String a = t.substring("#active=".length()).trim();
                    if (!a.isEmpty()) {
                        active = a;
                    }
                    continue;
                }
                if (t.startsWith("#")) {
                    continue;
                }
                if (!alts.contains(t)) {
                    alts.add(t);
                }
            }
        } catch (IOException ignored) {
        }
    }

    public static void save() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.gameDirectory == null) {
            return;
        }
        Path dir = configFile(client).getParent();
        try {
            Files.createDirectories(dir);
            List<String> out = new ArrayList<>();
            if (active != null) {
                out.add("#active=" + active);
            }
            out.addAll(alts);
            try (OutputStream os = Files.newOutputStream(configFile(client))) {
                for (int i = 0; i < out.size(); i++) {
                    if (i > 0) {
                        os.write('\n');
                    }
                    os.write(out.get(i).getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (IOException ignored) {
        }
    }

    public static List<String> getAlts() {
        return alts;
    }

    public static String getActive() {
        return active;
    }

    public static void add(String name) {
        String n = name.trim();
        if (n.isEmpty() || alts.contains(n)) {
            return;
        }
        alts.add(n);
        save();
    }

    public static void remove(String name) {
        alts.remove(name);
        if (name.equals(active)) {
            active = null;
        }
        save();
    }

    public static void select(String name) {
        if (alts.contains(name)) {
            active = name;
            save();
        }
    }

    public static void deselect() {
        active = null;
        save();
    }

    public static boolean isActive() {
        return active != null;
    }

    public static User currentUser() {
        if (active == null) {
            return null;
        }
        UUID offline = UUID.nameUUIDFromBytes(("OfflinePlayer:" + active).getBytes(StandardCharsets.UTF_8));
        return new User(active, offline, "0", Optional.empty(), Optional.empty());
    }

    private static final Random RNG = new Random();
    private static final String[] PREFIXES = {
        "xX_", "No_", "Mr_", "Pro", "Dark", "Ghost", "Neo", "Zero", "Mega", "Ultra",
        "Shadow", "Crystal", "Golden", "Night", "Star", "Red", "Blue", "Iron", "Alpha", "Omega"
    };
    private static final String[] NAMES = {
        "Alex", "Steve", "Zombie", "Blaze", "Creeper", "Ninja", "Gamer", "Killer", "Hunter", "Wizard",
        "Dragon", "Panda", "Wolf", "Fox", "Tiger", "Storm", "Blade", "Fury", "Void", "Prime"
    };
    private static final String[] SUFFIXES = {
        "_xD", "YT", "Pro", "_2010", "2007", "MC", "GOD", "LP", "BR", "RU"
    };

    public static String randomNick() {
        int r = RNG.nextInt(3);
        if (r == 0) {
            return PREFIXES[RNG.nextInt(PREFIXES.length)] + NAMES[RNG.nextInt(NAMES.length)]
                + SUFFIXES[RNG.nextInt(SUFFIXES.length)];
        }
        if (r == 1) {
            return NAMES[RNG.nextInt(NAMES.length)] + RNG.nextInt(100, 10000);
        }
        return "_" + NAMES[RNG.nextInt(NAMES.length)].toLowerCase() + "_" + RNG.nextInt(10, 999);
    }
}
