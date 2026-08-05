package com.example.client;

public class Theme {
    private static final int[] ACCENTS = {
        0xFF9A9A9A, 0xFFB0B0B0, 0xFFC8C8C8, 0xFF7A7A7A, 0xFF8C8C8C, 0xFFA8A8A8, 0xFFBFBFBF
    };
    private static final String[] NAMES = {
        "Серый", "Светлый", "Белый", "Тёмный", "Пепел", "Серебро", "Сталь"
    };

    private static int index = 0;
    private static boolean anime = false;

    private static final int ANIME_ACCENT = 0xFFC9C9C9;
    private static final int ANIME_BG = 0xFF111111;
    private static final int ANIME_PANEL = 0xE6242424;
    private static final int ANIME_LINE = 0xFF3C3C3C;

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

    public static int indexForSave() {
        return index;
    }

    public static void loadFromSave(int idx, boolean an) {
        index = Math.max(0, idx % ACCENTS.length);
        anime = an;
    }

    public static void apply() {
        if (anime) {
            Ui.applyAccent(ANIME_ACCENT);
            Ui.applyPalette(ANIME_BG, ANIME_PANEL, ANIME_LINE);
        } else {
            Ui.applyAccent(ACCENTS[index]);
            Ui.applyPalette(0xFF000000, 0xF00B0B0B, 0xFF3A3A3A);
        }
    }
}
