package com.example.client;

public class Theme {
    private static final int[] ACCENTS = {
        0xFF00CFFF, 0xFFFFAA00, 0xFFB44AFF, 0xFF2ECC40, 0xFFFF4136, 0xFF3A86FF, 0xFFFF5FA2
    };
    private static final String[] NAMES = {
        "Циан", "Оранж", "Пурпур", "Зелёный", "Красный", "Синий", "Розовый"
    };

    private static int index = 0;
    private static boolean anime = false;

    private static final int ANIME_ACCENT = 0xFFFF8AC8;
    private static final int ANIME_BG = 0xFFF7E8F4;
    private static final int ANIME_PANEL = 0xE6FFE3F2;
    private static final int ANIME_LINE = 0xFFE0C8DE;

    public static int current() {
        return anime ? ANIME_ACCENT : ACCENTS[index];
    }

    public static String name() {
        return anime ? "Аниме" : NAMES[index];
    }

    public static boolean isAnime() {
        return anime;
    }

    public static void next() {
        if (anime) {
            anime = false;
        }
        index = (index + 1) % ACCENTS.length;
        apply();
    }

    public static void toggleAnime() {
        anime = !anime;
        apply();
    }

    public static void apply() {
        if (anime) {
            Ui.applyAccent(ANIME_ACCENT);
            Ui.applyPalette(ANIME_BG, ANIME_PANEL, ANIME_LINE);
        } else {
            Ui.applyAccent(ACCENTS[index]);
            Ui.applyPalette(0xFF0A0E18, 0xC00B0F1A, 0xFF1E2A3E);
        }
    }
}
