package com.aliucord.plugins;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Utils {
    public static <T> Map<T, T> createMap(T... entries) {
        Map<T, T> map = new HashMap<>();
        T curKey = null;
        for (int i = 1; i <= entries.length; i++) {
            if (i % 2 == 1) {
                curKey = entries[i - 1];
            } else {
                map.put(curKey, entries[i - 1]);
            }
        }
        return map;
    }
    public static String clapify(String text) {
        return ":clap:" + text.replace(" ", ":clap:") + ":clap:";
    }
    public static String reverse(String text) {
        return new StringBuilder(text).reverse().toString();
    }
}
