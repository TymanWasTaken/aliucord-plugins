package com.aliucord.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Maps {
    public static String getMappedString(String map, String string) {
        char[] splitText = string.toCharArray();
        char[] splitReference = reference.toCharArray();
        char[] splitMap = map.toCharArray();
        Map<Character, Character> parsedMap = new HashMap<>();
        for (int i = 0; i < splitReference.length; i++) {
            parsedMap.put(splitReference[i], splitMap[i]);
        }
        StringBuilder newString = new StringBuilder();
        for (char character : splitText) {
            newString.append(
                    parsedMap.getOrDefault(character, character)
            );
        }
        return newString.toString();
    }

    public static String reference = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890";

    public static String smallLetters = "ǫᴡᴇʀᴛʏᴜɪᴏᴘᴀsᴅғɢʜᴊᴋʟᴢxᴄᴠʙɴᴍǫᴡᴇʀᴛʏᴜɪᴏᴘᴀsᴅғɢʜᴊᴋʟᴢxᴄᴠʙɴᴍ1234567890";
    public static String smallerLetters = "ᑫʷᵉʳᵗʸᵘᶦᵒᵖᵃˢᵈᶠᵍʰʲᵏˡᶻˣᶜᵛᵇⁿᵐᑫʷᵉʳᵗʸᵘᶦᵒᵖᵃˢᵈᶠᵍʰʲᵏˡᶻˣᶜᵛᵇⁿᵐ¹²³⁴⁵⁶⁷⁸⁹⁰";
    public static String fullWidthLetters = "ｑｗｅｒｔｙｕｉｏｐａｓｄｆｇｈｊｋｌｚｘｃｖｂｎｍＱＷＥＲＴＹＵＩＯＰＡＳＤＦＧＨＪＫＬＺＸＣＶＢＮＭ１２３４５６７８９０";
    public static String emojiLetters = "🇶🇼🇪🇷🇹🇾🇺🇮🇴🇵🇦🇸🇩🇫🇬🇭🇯🇰🇱🇿🇽🇨🇻🇧🇳🇲🇶🇼🇪🇷🇹🇾🇺🇮🇴🇵🇦🇸🇩🇫🇬🇭🇯🇰🇱🇿🇽🇨🇻🇧🇳🇲123456789";
}
