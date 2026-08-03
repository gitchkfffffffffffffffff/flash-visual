package com.example.client;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class Friends {
    private static final Set<String> SET = new LinkedHashSet<>();

    public static Set<String> all() {
        return SET;
    }

    public static boolean contains(String name) {
        if (name == null) {
            return false;
        }
        return SET.contains(name.toLowerCase(Locale.ROOT));
    }

    public static boolean add(String name) {
        if (name == null) {
            return false;
        }
        String key = name.toLowerCase(Locale.ROOT);
        if (key.isEmpty()) {
            return false;
        }
        return SET.add(key);
    }

    public static boolean remove(String name) {
        if (name == null) {
            return false;
        }
        return SET.remove(name.toLowerCase(Locale.ROOT));
    }

    public static void clear() {
        SET.clear();
    }
}
