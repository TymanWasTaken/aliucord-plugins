package com.aliucord.plugins.texter;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Utils {
    public static Map<String, String> createMap(String... entries) {
        Map<String, String> map = new HashMap<>();
        String curKey = null;
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
    public static String mock(String text) {
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for (String character : text.split("(?!^)")) {
            builder.append(random.nextBoolean() ? character.toLowerCase() : character.toUpperCase());
        }
        return builder.toString();
    }
}
