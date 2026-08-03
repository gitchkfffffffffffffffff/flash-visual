package com.example.client;

import java.util.LinkedHashMap;
import java.util.Map;

public class HudPos {
    private static final Map<String, int[]> POS = new LinkedHashMap<>();

    public static int[] get(String name, int defX, int defY) {
        int[] p = POS.get(name);
        if (p == null) {
            p = new int[]{defX, defY};
            POS.put(name, p);
        }
        return p;
    }

    public static void reset() {
        POS.clear();
    }

    public static Map<String, int[]> all() {
        return POS;
    }
}