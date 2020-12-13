package org.dicio.dicio_android.util;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    private static final Pattern punctationPattern = Pattern.compile("\\p{Punct}");

    private StringUtils() {
    }

    /**
     * Joins strings using delimiter
     * @param delimiter what to put in between strings
     * @param strings a list of strings to join
     * @return {@code string1 + delimiter + string2 + delimiter + ...
     *                + delimiter + stringN-1 + delimiter + stringN}
     */
    public static String join(final String delimiter, final List<String> strings) {
        final StringBuilder builder = new StringBuilder();
        final Iterator<String> iterator = strings.iterator();

        if (iterator.hasNext()) {
            builder.append(iterator.next());
        }
        while (iterator.hasNext()) {
            builder.append(delimiter);
            builder.append(iterator.next());
        }

        return builder.toString();
    }

    /**
     * Joins the strings by putting a space in between them.
     * @see #join(String, List)
     */
    public static String join(final List<String> strings) {
        return join(" ", strings);
    }

    /**
     * Finds the
     * <a href="https://en.wikipedia.org/wiki/Levenshtein_distance">Levenshtein distance</a>
     * between two strings, that is the number of characters that need to be changed to turn one
     * string into the other. Letter case is ignored.
     * @param a the first string
     * @param b the second string
     * @return the Levenshtein distance between the two strings
     */
    public static int levenshteinDistance(final String a, final String b) {
        // memory already filled with zeros, as it's the default value for int
        int[][] memory = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); ++i) {
            memory[i][0] = i;
        }
        for (int j = 0; j <= b.length(); ++j) {
            memory[0][j] = j;
        }

        for (int i = 0; i < a.length(); ++i) {
            for (int j = 0; j < b.length(); ++j) {
                int substitutionCost = Character.toLowerCase(a.codePointAt(i))
                        == Character.toLowerCase(b.codePointAt(j)) ? 0 : 1;
                memory[i+1][j+1] = Math.min(Math.min(memory[i][j+1] + 1, memory[i+1][j] + 1),
                        memory[i][j] + substitutionCost);
            }
        }

        return memory[a.length()][b.length()];
    }

    /**
     * Removes the punctuation in a string
     * @param s a string to remove punctuation from
     * @return e.g. for "hello, how are you? " returns "hello how are you "
     */
    public static String removePunctuation(final String s) {
        final Matcher matcher = punctationPattern.matcher(s);
        return matcher.replaceAll("");
    }
}
