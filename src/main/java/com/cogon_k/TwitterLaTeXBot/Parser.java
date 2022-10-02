package com.cogon_k.TwitterLaTeXBot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    public static String parse(String text) {
        String result = "";
        Matcher m = Pattern.compile("\\$[\\s\\S]*?\\$").matcher(text);
        while (m.find()) {
            String s = m.group();
            s = s.substring(1, s.length() - 1);
            result += s + "\\\\\n";
        }

        return result.substring(0, result.length() - 3);
    }
}
