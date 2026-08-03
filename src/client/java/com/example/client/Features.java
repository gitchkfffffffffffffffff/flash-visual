package com.example.client;

public class Features {
    public static boolean customFog = false;
    public static int fogColorIndex = 0;
    public static int fogDistanceIndex = 3;

    public static boolean soundBoost = true;
    public static boolean quietWarden = true;

    public static boolean noRender = false;

    public static boolean showInvis = false;
    public static boolean darkMenu = false;
    public static boolean beautifulMenu = true;

    public static final float[][] FOG_COLORS = {
        {0.42f, 0.20f, 0.85f},
        {0.00f, 0.81f, 1.00f},
        {1.00f, 0.55f, 0.10f},
        {0.25f, 1.00f, 0.55f},
        {1.00f, 0.25f, 0.25f},
        {0.90f, 0.90f, 0.90f}
    };
    public static final String[] FOG_COLOR_NAMES = {"Фиолет", "Циан", "Оранж", "Зелёный", "Красный", "Белый"};

    public static final float[] FOG_DISTANCES = {0f, 0.25f, 0.5f, 1f, 2f, 4f};
    public static final String[] FOG_DISTANCE_NAMES = {"Нет", "25%", "50%", "100%", "200%", "400%"};

    public static float fogR() {
        return FOG_COLORS[fogColorIndex][0];
    }

    public static float fogG() {
        return FOG_COLORS[fogColorIndex][1];
    }

    public static float fogB() {
        return FOG_COLORS[fogColorIndex][2];
    }

    public static float fogDistance() {
        return FOG_DISTANCES[fogDistanceIndex];
    }
}
