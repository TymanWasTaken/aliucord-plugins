package com.aliucord.plugins;

import android.util.Pair;

import java.util.Arrays;
import java.util.List;

public class Maps {
    public static String getMappedString(List<Pair<String, String>> map, String string) {
        char[] split = string.toCharArray();
        StringBuilder newString = new StringBuilder();
        for (char character : split) {
            String stringChar = String.valueOf(character);
            newString.append(
                map
                    .stream()
                    .filter(pair -> pair.first.equals(stringChar))
                    .findFirst()
                    .orElse(new Pair(stringChar, stringChar))
                    .second
            );
        }
        return newString.toString();
    }

    public static List<Pair<String, String>> smallLetters = Arrays.asList(
            new Pair<>("w", "ᴡ"),
            new Pair<>("e", "ᴇ"),
            new Pair<>("q", "ǫ"),
            new Pair<>("r", "ʀ"),
            new Pair<>("t", "ᴛ"),
            new Pair<>("y", "ʏ"),
            new Pair<>("u", "ᴜ"),
            new Pair<>("i", "ɪ"),
            new Pair<>("o", "ᴏ"),
            new Pair<>("p", "ᴘ"),
            new Pair<>("a", "ᴀ"),
            new Pair<>("s", "s"),
            new Pair<>("d", "ᴅ"),
            new Pair<>("f", "ꜰ"),
            new Pair<>("g", "ɢ"),
            new Pair<>("h", "ʜ"),
            new Pair<>("j", "ᴊ"),
            new Pair<>("k", "ᴋ"),
            new Pair<>("l", "ʟ"),
            new Pair<>("z", "ᴢ"),
            new Pair<>("x", "x"),
            new Pair<>("c", "ᴄ"),
            new Pair<>("v", "ᴠ"),
            new Pair<>("b", "ʙ"),
            new Pair<>("n", "ɴ"),
            new Pair<>("m", "ᴍ")
    );

    public static List<Pair<String, String>> smallerLetters = Arrays.asList(
        new Pair<>("q", "ᑫ"),
        new Pair<>("w", "ʷ"),
        new Pair<>("e", "ᵉ"),
        new Pair<>("r", "ʳ"),
        new Pair<>("t", "ᵗ"),
        new Pair<>("y", "ʸ"),
        new Pair<>("u", "ᵘ"),
        new Pair<>("i", "ᶦ"),
        new Pair<>("o", "ᵒ"),
        new Pair<>("p", "ᵖ"),
        new Pair<>("a", "ᵃ"),
        new Pair<>("s", "ˢ"),
        new Pair<>("d", "ᵈ"),
        new Pair<>("f", "ᶠ"),
        new Pair<>("g", "ᵍ"),
        new Pair<>("h", "ʰ"),
        new Pair<>("j", "ʲ"),
        new Pair<>("k", "ᵏ"),
        new Pair<>("l", "ˡ"),
        new Pair<>("z", "ᶻ"),
        new Pair<>("x", "ˣ"),
        new Pair<>("c", "ᶜ"),
        new Pair<>("v", "ᵛ"),
        new Pair<>("b", "ᵇ"),
        new Pair<>("n", "ⁿ"),
        new Pair<>("m", "ᵐ")
    );
}
