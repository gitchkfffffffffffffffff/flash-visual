package com.example.client;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class NickNotes {
    private static final Map<String, String> NOTES = new LinkedHashMap<>();

    public static Map<String, String> all() {
        return Collections.unmodifiableMap(NOTES);
    }

    public static String get(String name) {
        if (name == null) {
            return null;
        }
        return NOTES.get(name.toLowerCase());
    }

    public static boolean has(String name) {
        return name != null && NOTES.containsKey(name.toLowerCase());
    }

    public static void set(String nick, String note) {
        if (nick == null) {
            return;
        }
        String key = nick.trim().toLowerCase();
        if (key.isEmpty()) {
            return;
        }
        String n = note == null ? "" : note.trim();
        if (n.isEmpty()) {
            NOTES.remove(key);
        } else {
            NOTES.put(key, n);
        }
    }

    public static boolean remove(String nick) {
        return nick != null && NOTES.remove(nick.toLowerCase()) != null;
    }

    public static void clear() {
        NOTES.clear();
    }
}