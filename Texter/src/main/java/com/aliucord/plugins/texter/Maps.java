package com.aliucord.plugins.texter;

import com.aliucord.Logger;

import java.util.Map;

public class Maps {
    public static String getMappedString(Map<String, String> map, String string) {
        new Logger().debug(map.keySet().toString());
        String[] splitText = string.split("(?!^)");
        StringBuilder newString = new StringBuilder();
        for (String character : splitText) {
            newString.append(
                    map.getOrDefault(character, character)
            );
        }
        return newString.toString();
    }
    public static final Map<String, String> smallLetters = Utils.createMap(
            "q", "ǫ",
            "w", "ᴡ",
            "e", "ᴇ",
            "r", "ʀ",
            "t", "ᴛ",
            "y", "ʏ",
            "u", "ᴜ",
            "i", "ɪ",
            "o", "ᴏ",
            "p", "ᴘ",
            "a", "ᴀ",
            "s", "s",
            "d", "ᴅ",
            "f", "ғ",
            "g", "ɢ",
            "h", "ʜ",
            "j", "ᴊ",
            "k", "ᴋ",
            "l", "ʟ",
            "z", "ᴢ",
            "x", "x",
            "c", "ᴄ",
            "v", "ᴠ",
            "b", "ʙ",
            "n", "ɴ",
            "m", "ᴍ",
            "Q", "ǫ",
            "W", "ᴡ",
            "E", "ᴇ",
            "R", "ʀ",
            "T", "ᴛ",
            "Y", "ʏ",
            "U", "ᴜ",
            "I", "ɪ",
            "O", "ᴏ",
            "P", "ᴘ",
            "A", "ᴀ",
            "S", "s",
            "D", "ᴅ",
            "F", "ғ",
            "G", "ɢ",
            "H", "ʜ",
            "J", "ᴊ",
            "K", "ᴋ",
            "L", "ʟ",
            "Z", "ᴢ",
            "X", "x",
            "C", "ᴄ",
            "V", "ᴠ",
            "B", "ʙ",
            "N", "ɴ",
            "M", "ᴍ"
    );

    public static final Map<String, String> smallerLetters = Utils.createMap(
            "q", "ᑫ",
            "w", "ʷ",
            "e", "ᵉ",
            "r", "ʳ",
            "t", "ᵗ",
            "y", "ʸ",
            "u", "ᵘ",
            "i", "ᶦ",
            "o", "ᵒ",
            "p", "ᵖ",
            "a", "ᵃ",
            "s", "ˢ",
            "d", "ᵈ",
            "f", "ᶠ",
            "g", "ᵍ",
            "h", "ʰ",
            "j", "ʲ",
            "k", "ᵏ",
            "l", "ˡ",
            "z", "ᶻ",
            "x", "ˣ",
            "c", "ᶜ",
            "v", "ᵛ",
            "b", "ᵇ",
            "n", "ⁿ",
            "m", "ᵐ",
            "Q", "ᑫ",
            "W", "ʷ",
            "E", "ᵉ",
            "R", "ʳ",
            "T", "ᵗ",
            "Y", "ʸ",
            "U", "ᵘ",
            "I", "ᶦ",
            "O", "ᵒ",
            "P", "ᵖ",
            "A", "ᵃ",
            "S", "ˢ",
            "D", "ᵈ",
            "F", "ᶠ",
            "G", "ᵍ",
            "H", "ʰ",
            "J", "ʲ",
            "K", "ᵏ",
            "L", "ˡ",
            "Z", "ᶻ",
            "X", "ˣ",
            "C", "ᶜ",
            "V", "ᵛ",
            "B", "ᵇ",
            "N", "ⁿ",
            "M", "ᵐ",
            "1", "¹",
            "2", "²",
            "3", "³",
            "4", "⁴",
            "5", "⁵",
            "6", "⁶",
            "7", "⁷",
            "8", "⁸",
            "9", "⁹",
            "0", "⁰"
    );
    public static final Map<String, String> fullWidthLetters = Utils.createMap(
            "q", "ｑ",
            "w", "ｗ",
            "e", "ｅ",
            "r", "ｒ",
            "t", "ｔ",
            "y", "ｙ",
            "u", "ｕ",
            "i", "ｉ",
            "o", "ｏ",
            "p", "ｐ",
            "a", "ａ",
            "s", "ｓ",
            "d", "ｄ",
            "f", "ｆ",
            "g", "ｇ",
            "h", "ｈ",
            "j", "ｊ",
            "k", "ｋ",
            "l", "ｌ",
            "z", "ｚ",
            "x", "ｘ",
            "c", "ｃ",
            "v", "ｖ",
            "b", "ｂ",
            "n", "ｎ",
            "m", "ｍ",
            "Q", "Ｑ",
            "W", "Ｗ",
            "E", "Ｅ",
            "R", "Ｒ",
            "T", "Ｔ",
            "Y", "Ｙ",
            "U", "Ｕ",
            "I", "Ｉ",
            "O", "Ｏ",
            "P", "Ｐ",
            "A", "Ａ",
            "S", "Ｓ",
            "D", "Ｄ",
            "F", "Ｆ",
            "G", "Ｇ",
            "H", "Ｈ",
            "J", "Ｊ",
            "K", "Ｋ",
            "L", "Ｌ",
            "Z", "Ｚ",
            "X", "Ｘ",
            "C", "Ｃ",
            "V", "Ｖ",
            "B", "Ｂ",
            "N", "Ｎ",
            "M", "Ｍ",
            "1", "１",
            "2", "２",
            "3", "３",
            "4", "４",
            "5", "５",
            "6", "６",
            "7", "７",
            "8", "８",
            "9", "９",
            "0", "０"
    );
    public static final Map<String, String> emojiLetters = Utils.createMap(
            "q", ":regional_indicator_q:",
            "w", ":regional_indicator_w:",
            "e", ":regional_indicator_e:",
            "r", ":regional_indicator_r:",
            "t", ":regional_indicator_t:",
            "y", ":regional_indicator_y:",
            "u", ":regional_indicator_u:",
            "i", ":regional_indicator_i:",
            "o", ":regional_indicator_o:",
            "p", ":regional_indicator_p:",
            "a", ":regional_indicator_a:",
            "s", ":regional_indicator_s:",
            "d", ":regional_indicator_d:",
            "f", ":regional_indicator_f:",
            "g", ":regional_indicator_g:",
            "h", ":regional_indicator_h:",
            "j", ":regional_indicator_j:",
            "k", ":regional_indicator_k:",
            "l", ":regional_indicator_l:",
            "z", ":regional_indicator_z:",
            "x", ":regional_indicator_x:",
            "c", ":regional_indicator_c:",
            "v", ":regional_indicator_v:",
            "b", ":regional_indicator_b:",
            "n", ":regional_indicator_n:",
            "m", ":regional_indicator_m:",
            "Q", ":regional_indicator_q:",
            "W", ":regional_indicator_w:",
            "E", ":regional_indicator_e:",
            "R", ":regional_indicator_r:",
            "T", ":regional_indicator_t:",
            "Y", ":regional_indicator_y:",
            "U", ":regional_indicator_u:",
            "I", ":regional_indicator_i:",
            "O", ":regional_indicator_o:",
            "P", ":regional_indicator_p:",
            "A", ":regional_indicator_a:",
            "S", ":regional_indicator_s:",
            "D", ":regional_indicator_d:",
            "F", ":regional_indicator_f:",
            "G", ":regional_indicator_g:",
            "H", ":regional_indicator_h:",
            "J", ":regional_indicator_j:",
            "K", ":regional_indicator_k:",
            "L", ":regional_indicator_l:",
            "Z", ":regional_indicator_z:",
            "X", ":regional_indicator_x:",
            "C", ":regional_indicator_c:",
            "V", ":regional_indicator_v:",
            "B", ":regional_indicator_b:",
            "N", ":regional_indicator_n:",
            "M", ":regional_indicator_m:",
            "1", ":one:",
            "2", ":two:",
            "3", ":three:",
            "4", ":four:",
            "5", ":five:",
            "6", ":six:",
            "7", ":seven:",
            "8", ":eight:",
            "9", ":nine:",
            "0", ":zero:"
    );
    public static final Map<String, String> flippedLetters = Utils.createMap(
            "q", "b",
            "w", "ʍ",
            "e", "ǝ",
            "r", "ɹ",
            "t", "ʇ",
            "y", "ʎ",
            "u", "n",
            "i", "ᴉ",
            "p", "d",
            "a", "ɐ",
            "s", "s",
            "d", "p",
            "f", "ɟ",
            "g", "ɓ",
            "h", "ɥ",
            "j", "ſ",
            "k", "ʞ",
            "l", "ๅ",
            "z", "z",
            "x", "x",
            "c", "ɔ",
            "v", "ʌ",
            "b", "q",
            "n", "u",
            "m", "ɯ",
            "Q", "Ὸ",
            "W", "M",
            "E", "Ǝ",
            "R", "ꓤ",
            "T", "ꓕ",
            "Y", "⅄",
            "U", "ꓵ",
            "I", "I",
            "O", "O",
            "P", "ꓒ",
            "A", "Ɐ",
            "S", "S",
            "D", "ꓷ",
            "F", "ꓞ",
            "G", "ꓨ",
            "H", "H",
            "J", "ſ",
            "K", "ꓘ",
            "L", "ꓶ",
            "Z", "Z",
            "X", "X",
            "C", "ꓛ",
            "V", "ꓥ",
            "B", "ꓭ",
            "N", "N",
            "M", "W",
            "1", "Ɩ",
            "2", "Շ",
            "3", "Ɛ",
            "4", "h",
            "5", "૬",
            "6", "9",
            "7", "L",
            "8", "8",
            "9", "6",
            "0", "0"
    );
    public static final Map<String, String> leetLetters = Utils.createMap(
            "e", "3",
            "r", "Я",
            "t", "7",
            "o", "0",
            "a", "4",
            "s", "5",
            "h", "#",
            "l", "1",
            "z", "2",
            "b", "8",
            "E", "3",
            "R", "Я",
            "T", "7",
            "O", "0",
            "A", "4",
            "S", "5",
            "H", "#",
            "L", "1",
            "Z", "2",
            "B", "8"
    );
}