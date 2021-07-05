package tech.tyman.texter;

import android.util.Pair;

import java.util.Arrays;
import java.util.List;

public class Maps {
    public static String getMappedChar(List<Pair<String, String>> map, String character) {
        return map
                .stream()
                .filter(pair -> pair.first.equals(character))
                .findFirst()
                .orElse(new Pair<>(character, character))
                .second;
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
}
