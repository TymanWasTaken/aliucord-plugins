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
    public static String emojiLetters = "\uD83C\uDDF6\uD83C\uDDFC\uD83C\uDDEA\uD83C\uDDF7\uD83C\uDDF9\uD83C\uDDFE\uD83C\uDDFA\uD83C\uDDEE\uD83C\uDDF4\uD83C\uDDF5\uD83C\uDDE6\uD83C\uDDF8\uD83C\uDDE9\uD83C\uDDEB\uD83C\uDDEC\uD83C\uDDED\uD83C\uDDEF\uD83C\uDDF0\uD83C\uDDF1\uD83C\uDDFF\uD83C\uDDFD\uD83C\uDDE8\uD83C\uDDFB\uD83C\uDDE7\uD83C\uDDF3\uD83C\uDDF2\uD83C\uDDF6\uD83C\uDDFC\uD83C\uDDEA\uD83C\uDDF7\uD83C\uDDF9\uD83C\uDDFE\uD83C\uDDFA\uD83C\uDDEE\uD83C\uDDF4\uD83C\uDDF5\uD83C\uDDE6\uD83C\uDDF8\uD83C\uDDE9\uD83C\uDDEB\uD83C\uDDEC\uD83C\uDDED\uD83C\uDDEF\uD83C\uDDF0\uD83C\uDDF1\uD83C\uDDFF\uD83C\uDDFD\uD83C\uDDE8\uD83C\uDDFB\uD83C\uDDE7\uD83C\uDDF3\uD83C\uDDF21️⃣2️⃣3️⃣4️⃣5️⃣6️⃣7️⃣8️⃣9️⃣0️⃣";
}
