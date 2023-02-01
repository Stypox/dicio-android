package org.stypox.dicio.util;

import org.dicio.skill.util.WordExtractor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public final class StringUtils {
    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile("\\p{Punct}");
    private static final Pattern WORD_DELIMITERS_PATTERN = Pattern.compile("[^\\p{L}\\d]");

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
     * Removes the punctuation in a string
     * @param string a string to remove punctuation from
     * @return e.g. for "hello, how are you? " returns "hello how are you "
     */
    public static String removePunctuation(final String string) {
        return RegexUtils.replaceAll(PUNCTUATION_PATTERN, string, "");
    }

    public static boolean isNullOrEmpty(final String string) {
        return string == null || string.isEmpty();
    }

    private static String cleanStringForDistance(final String s) {
        return WORD_DELIMITERS_PATTERN.matcher(WordExtractor.nfkdNormalizeWord(s.toLowerCase()))
                .replaceAll("");
    }

    /**
     * Returns the dynamic programming memory obtained when calculating the Levenshtein distance.
     * The solution lies at {@code memory[a.length()][b.length()]}. This memory can be used to find
     * the set of actions (insertion, deletion or substitution) to be done on the two strings to
     * turn one into the other. TODO this can be optimized to work with O(n) memory.
     * @param a the first string, maybe cleaned with {@link #cleanStringForDistance(String)}
     * @param b the second string, maybe cleaned with {@link #cleanStringForDistance(String)}
     * @return the memory of size {@code (a.length()+1) x (b.length()+1)}
     */
    private static int[][] levenshteinDistanceMemory(final String a, final String b) {
        // memory already filled with zeros, as it's the default value for int
        final int[][] memory = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); ++i) {
            memory[i][0] = i;
        }
        for (int j = 0; j <= b.length(); ++j) {
            memory[0][j] = j;
        }

        for (int i = 0; i < a.length(); ++i) {
            for (int j = 0; j < b.length(); ++j) {
                final int substitutionCost = Character.toLowerCase(a.codePointAt(i))
                        == Character.toLowerCase(b.codePointAt(j)) ? 0 : 1;
                memory[i + 1][j + 1] = Math.min(Math.min(
                        memory[i][j + 1] + 1,
                        memory[i + 1][j] + 1),
                        memory[i][j] + substitutionCost);
            }
        }

        return memory;
    }

    private static class LevenshteinMemoryPos {
        final int i;
        final int j;
        final boolean match;
        LevenshteinMemoryPos(final int i, final int j, final boolean match) {
            this.i = i;
            this.j = j;
            this.match = match;
        }
    }

    private static List<LevenshteinMemoryPos> pathInLevenshteinMemory(
            final String a, final String b, final int[][] memory) {
        // follow the path from bottom right (score==distance) to top left (where score==0)
        final List<LevenshteinMemoryPos> positions = new ArrayList<>();
        int i = a.length() - 1;
        int j = b.length() - 1;
        while (i >= 0 && j >= 0) {
            final int iOld = i;
            final int jOld = j;
            boolean match = false;

            if (memory[i + 1][j + 1] == memory[i][j + 1] + 1) {
                // the path goes up
                --i;
            } else if (memory[i + 1][j + 1] == memory[i + 1][j] + 1) {
                // the path goes left
                --j;
            } else  {
                // the path goes up-left diagonally (surely either
                // memory[i+1][j+1] == memory[i][j] or memory[i+1][j+1] == memory[i][j] + 1)
                match = memory[i + 1][j + 1] == memory[i][j];
                --i;
                --j;
            }

            positions.add(new LevenshteinMemoryPos(iOld, jOld, match));
        }
        return positions;
    }

    /**
     * Finds the
     * <a href="https://en.wikipedia.org/wiki/Levenshtein_distance">Levenshtein distance</a>
     * between two strings, that is the number of characters that need to be changed to turn one
     * string into the other. The two strings will be cleaned with {@link
     * #cleanStringForDistance(String)} before calculating the distance. Use {@link
     * #customStringDistance(String, String)} for better results when e.g. comparing app names.
     * @see #customStringDistance(String, String)
     * @param aNotCleaned the first string
     * @param bNotCleaned the second string
     * @return the Levenshtein distance between the two cleaned strings, lower is better, values are
     *         always greater than or equal to 0
     */
    public static int levenshteinDistance(final String aNotCleaned, final String bNotCleaned) {
        final String a = cleanStringForDistance(aNotCleaned);
        final String b = cleanStringForDistance(bNotCleaned);
        return levenshteinDistanceMemory(a, b)[a.length()][b.length()];
    }


    private static final class StringDistanceStats {
        private final int levenshteinDistance;
        private final int maxSubsequentChars;
        private final int matchingCharCount;

        private StringDistanceStats(final int levenshteinDistance,
                                    final int maxSubsequentChars,
                                    final int matchingCharCount) {
            this.levenshteinDistance = levenshteinDistance;
            this.maxSubsequentChars = maxSubsequentChars;
            this.matchingCharCount = matchingCharCount;
        }
    }

    /**
     * Calculates some statistics about the
     * <a href="https://en.wikipedia.org/wiki/Levenshtein_distance">Levenshtein distance</a> between
     * the two strings. Follows the path chosen by the dynamic programming algorithm to obtain the
     * total number of matched characters and the maximum number of roughly subsequent characters
     * matched.
     *
     * @param a the first string, maybe cleaned with {@link #cleanStringForDistance(String)}
     * @param b the second string, maybe cleaned with {@link #cleanStringForDistance(String)}
     * @return a triple of Levenshtein distance, max subsequent chars and matching char count
     */
    private static StringDistanceStats stringDistanceStats(final String a, final String b) {
        final int[][] memory = levenshteinDistanceMemory(a, b);

        int matchingCharCount = 0;
        int subsequentChars = 0;
        int maxSubsequentChars = 0;
        for (final LevenshteinMemoryPos pos : pathInLevenshteinMemory(a, b, memory)) {
            if (pos.match) {
                ++matchingCharCount;
                ++subsequentChars;
                maxSubsequentChars = Math.max(maxSubsequentChars, subsequentChars);
            } else {
                subsequentChars = Math.max(0, subsequentChars - 1);
            }
        }

        return new StringDistanceStats(memory[a.length()][b.length()], maxSubsequentChars,
                matchingCharCount);
    }

    /**
     * Calculates a custom string distance between the two provided strings, based on the statistics
     * drawn by {@link #stringDistanceStats(String, String)}. Seems to work well when matching
     * names of objects, where the difference in length between the two strings counts by some
     * factor, but max subsequent chars and matching char count also play a big role.
     *
     * @param aNotCleaned the first string
     * @param bNotCleaned the second string
     * @return the custom string distance between the two cleaned strings, lower is better, values
     *         can be lower than 0, values are always less than or equal to the {@link
     *         #levenshteinDistance(String, String)} between the two strings
     */
    public static int customStringDistance(final String aNotCleaned, final String bNotCleaned) {
        final String a = cleanStringForDistance(aNotCleaned);
        final String b = cleanStringForDistance(bNotCleaned);
        final StringDistanceStats stats = stringDistanceStats(a, b);
        return stats.levenshteinDistance - stats.maxSubsequentChars - stats.matchingCharCount;
    }

    /**
     * Calculates a custom string distance between the two provided strings, based on the statistics
     * drawn by {@link #stringDistanceStats(String, String)}. Seems to work well when matching
     * contact names, where the difference in length between the two strings is mostly irrelevant,
     * and what mostly counts are max subsequent chars and matching char count.
     *
     * @param aNotCleaned the first string
     * @param bNotCleaned the second string
     * @return the custom string distance between the two cleaned strings, lower is better, values
     *         will always be lower than or equal to 0
     */
    public static int contactStringDistance(final String aNotCleaned, final String bNotCleaned) {
        final String a = cleanStringForDistance(aNotCleaned);
        final String b = cleanStringForDistance(bNotCleaned);
        final StringDistanceStats stats = stringDistanceStats(a, b);
        return -stats.maxSubsequentChars - stats.matchingCharCount;
    }
}
