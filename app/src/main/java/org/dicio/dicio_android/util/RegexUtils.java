package org.dicio.dicio_android.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RegexUtils {

    private RegexUtils() {
    }

    // taken from NewPipeExtractor, file utils/Parser.java, created by @theScrabi
    public static String matchGroup1(final Pattern pattern, final String string) {
        return matchGroup(pattern, string, 1);
    }

    // taken from NewPipeExtractor, file utils/Parser.java, created by @theScrabi
    public static String matchGroup(final Pattern pattern, final String string, final int group) {
        final Matcher matcher = pattern.matcher(string);
        final boolean foundMatch = matcher.find();
        if (foundMatch) {
            return matcher.group(group);
        } else {
            // only pass input to exception message when it is not too long
            if (string.length() > 1024) {
                throw new IllegalArgumentException("failed to find pattern \"" + pattern.pattern());
            } else {
                throw new IllegalArgumentException("failed to find pattern \"" + pattern.pattern()
                        + " inside of " + string + "\"");
            }
        }
    }

    public static String replaceAll(final Pattern pattern,
                                    final String string,
                                    final String replacement) {
        return pattern.matcher(string).replaceAll(replacement);
    }
}
