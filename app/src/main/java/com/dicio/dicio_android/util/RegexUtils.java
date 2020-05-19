package com.dicio.dicio_android.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {
    // taken from NewPipeExtractor, file utils/Parser.java, created by @theScrabi
    public static String matchGroup1(String pattern, String input) {
        return matchGroup(pattern, input, 1);
    }

    // taken from NewPipeExtractor, file utils/Parser.java, created by @theScrabi
    public static String matchGroup1(Pattern pattern, String input) {
        return matchGroup(pattern, input, 1);
    }

    // taken from NewPipeExtractor, file utils/Parser.java, created by @theScrabi
    public static String matchGroup(String pattern, String input, int group) {
        Pattern pat = Pattern.compile(pattern);
        return matchGroup(pat, input, group);
    }

    // taken from NewPipeExtractor, file utils/Parser.java, created by @theScrabi
    public static String matchGroup(Pattern pat, String input, int group) {
        Matcher mat = pat.matcher(input);
        boolean foundMatch = mat.find();
        if (foundMatch) {
            return mat.group(group);
        } else {
            // only pass input to exception message when it is not too long
            if (input.length() > 1024) {
                throw new IllegalArgumentException("failed to find pattern \"" + pat.pattern());
            } else {
                throw new IllegalArgumentException("failed to find pattern \"" + pat.pattern()
                        + " inside of " + input + "\"");
            }
        }
    }
}
