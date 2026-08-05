package com.example.client;

import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.List;

public class Binds {
    public static final String MENU = "menu";
    public static final String GHOST = "ghost";
    public static final String FREECAM = "freecam";
    public static final String MUSIC_HUD = "musicHud";
    public static final String TARGET_HUD = "targetHud";
    public static final String ESP_PLAYER = "espPlayer";
    public static final String ESP_MOB = "espMob";
    public static final String ESP_ITEM = "espItem";
    public static final String PREV = "prev";
    public static final String PLAY_PAUSE = "playPause";
    public static final String NEXT = "next";
    public static final String KILLAURA = "killAura";
    public static final String SCAFFOLD = "scaffold";
    public static final String WITHDRAW = "withdraw";
    public static final String DEPOSIT = "deposit";
    public static final String AUTO_TOTEM = "autoTotem";
    public static final String ZOOM = "zoom";
    public static final String FAKE = "fake";
    public static final String SAFE = "safe";

    private static final LinkedHashMap<String, String> LABELS = new LinkedHashMap<>();
    private static final LinkedHashMap<String, Integer> BINDS = new LinkedHashMap<>();

    static {
        defaults();
    }

    private static void defaults() {
        register(MENU, "Открыть меню", GLFW.GLFW_KEY_RIGHT_SHIFT);
        register(GHOST, "Ghost Blocks", GLFW.GLFW_KEY_H);
        register(FREECAM, "FreeCam", GLFW.GLFW_KEY_F6);
        register(MUSIC_HUD, "Music HUD", GLFW.GLFW_KEY_F7);
        register(TARGET_HUD, "Target HUD", GLFW.GLFW_KEY_F8);
        register(ESP_PLAYER, "Player ESP", GLFW.GLFW_KEY_F9);
        register(ESP_MOB, "Mob ESP", GLFW.GLFW_KEY_F10);
        register(ESP_ITEM, "Item ESP", GLFW.GLFW_KEY_F11);
        register(PREV, "Медиа · назад", GLFW.GLFW_KEY_F4);
        register(PLAY_PAUSE, "Медиа · плей", GLFW.GLFW_KEY_F5);
        register(NEXT, "Медиа · вперёд", GLFW.GLFW_KEY_F12);
        register(KILLAURA, "KillAura", GLFW.GLFW_KEY_K);
        register(SCAFFOLD, "Scaffold", GLFW.GLFW_KEY_R);
        register(WITHDRAW, "Забрать всё", GLFW.GLFW_KEY_Z);
        register(DEPOSIT, "Положить всё", GLFW.GLFW_KEY_X);
        register(AUTO_TOTEM, "Auto Totem", GLFW.GLFW_KEY_V);
        register(ZOOM, "Zoom", GLFW.GLFW_KEY_C);
        register(FAKE, "Fake Player", GLFW.GLFW_KEY_P);
        register(SAFE, "Safe Mode", GLFW.GLFW_KEY_U);
    }

    private static void register(String id, String label, int key) {
        LABELS.put(id, label);
        BINDS.put(id, key);
    }

    public static void resetAll() {
        defaults();
    }

    public static List<String> actions() {
        return List.copyOf(BINDS.keySet());
    }

    public static String label(String action) {
        return LABELS.getOrDefault(action, action);
    }

    public static int get(String action) {
        return BINDS.getOrDefault(action, -1);
    }

    public static void set(String action, int key) {
        if (BINDS.containsKey(action) && key >= 0) {
            BINDS.put(action, key);
        }
    }

    public static String keyName(String action) {
        return keyName(get(action));
    }

    public static String keyName(int key) {
        switch (key) {
            case GLFW.GLFW_KEY_LEFT_SHIFT:
            case GLFW.GLFW_KEY_RIGHT_SHIFT:
                return "SHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL:
            case GLFW.GLFW_KEY_RIGHT_CONTROL:
                return "CTRL";
            case GLFW.GLFW_KEY_LEFT_ALT:
            case GLFW.GLFW_KEY_RIGHT_ALT:
                return "ALT";
            case GLFW.GLFW_KEY_SPACE:
                return "SPACE";
            case GLFW.GLFW_KEY_ENTER:
                return "ENTER";
            case GLFW.GLFW_KEY_TAB:
                return "TAB";
            case GLFW.GLFW_KEY_ESCAPE:
                return "ESC";
            case GLFW.GLFW_KEY_BACKSPACE:
                return "BACKSPACE";
            case GLFW.GLFW_KEY_UP:
                return "UP";
            case GLFW.GLFW_KEY_DOWN:
                return "DOWN";
            case GLFW.GLFW_KEY_LEFT:
                return "LEFT";
            case GLFW.GLFW_KEY_RIGHT:
                return "RIGHT";
            case GLFW.GLFW_KEY_F1:
                return "F1";
            case GLFW.GLFW_KEY_F2:
                return "F2";
            case GLFW.GLFW_KEY_F3:
                return "F3";
            case GLFW.GLFW_KEY_F4:
                return "F4";
            case GLFW.GLFW_KEY_F5:
                return "F5";
            case GLFW.GLFW_KEY_F6:
                return "F6";
            case GLFW.GLFW_KEY_F7:
                return "F7";
            case GLFW.GLFW_KEY_F8:
                return "F8";
            case GLFW.GLFW_KEY_F9:
                return "F9";
            case GLFW.GLFW_KEY_F10:
                return "F10";
            case GLFW.GLFW_KEY_F11:
                return "F11";
            case GLFW.GLFW_KEY_F12:
                return "F12";
            default:
                if (key < 0) {
                    return "НЕТ";
                }
                String glfwName = GLFW.glfwGetKeyName(key, 0);
                return glfwName != null ? glfwName.toUpperCase() : "KEY" + key;
        }
    }
}
