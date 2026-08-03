package com.example.client;

import net.minecraft.client.User;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AltManager {
    private static final List<String> alts = new ArrayList<>();
    private static String active = null;

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
    }

    public static void remove(String name) {
        alts.remove(name);
        if (name.equals(active)) {
            active = null;
        }
    }

    public static void select(String name) {
        if (alts.contains(name)) {
            active = name;
        }
    }

    public static void deselect() {
        active = null;
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
}
