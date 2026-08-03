package com.example.client;

public class TimeChanger {
    public static final int OFF = 0;
    public static final int DAY = 1;
    public static final int NIGHT = 2;

    public static final String[] MODE_NAMES = {"Выкл", "День", "Ночь"};

    public static int mode = OFF;

    public static long fixedTime() {
        return mode == NIGHT ? 13000L : 1000L;
    }
}
