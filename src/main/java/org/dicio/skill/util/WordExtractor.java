package org.dicio.skill.util;

import org.dicio.skill.standard.InputWordRange;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordExtractor {

    private static final Pattern wordSplitter = Pattern.compile("[^\\p{L}]+");
    private static final Pattern diacriticalMarksRemover =
            Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private WordExtractor() {
    }


    /**
     * Splits the input into words at every non-letter character, and making every word lowercase.
     * <br>
     * For example, "Hello, how Àrè you? " becomes {"hello", "how, "àrè", "you"}
     * @param input the input from which to extract words
     * @return the list of extracted words in order
     */
    public static List<String> extractWords(final String input) {
        // match all non-letter characters
        final String[] splitInput = wordSplitter.split(input);

        final List<String> inputWords = new ArrayList<>();
        for(final String word : splitInput) {
            if (word != null && !word.isEmpty()) {
                // TODO should this be locale-sensitive?
                inputWords.add(word.toLowerCase(Locale.ENGLISH));
            }
        }

        return inputWords;
    }

    /**
     * Builds the list of the unicode NFKD normalized values for the input words using
     * {@link #nfkdNormalizeWord(String)}
     * @param inputWords the lowercase words to normalize
     * @return the normalized words in order
     */
    public static List<String> normalizeWords(final List<String> inputWords) {
        final List<String> normalizedInputWords = new ArrayList<>(inputWords.size());
        for (final String inputWord : inputWords) {
            normalizedInputWords.add(nfkdNormalizeWord(inputWord));
        }
        return normalizedInputWords;
    }

    /**
     * @param word a lowercase string
     * @return the unicode NFKD normalized value for the provided word
     * @implNote the normalization process could be slow
     */
    public static String nfkdNormalizeWord(final String word) {
        final String normalized = Normalizer.normalize(word, Normalizer.Form.NFKD);
        return diacriticalMarksRemover.matcher(normalized).replaceAll("");
    }

    /**
     * Extracts a capturing group from the input containing the provided word range. Special
     * characters before and after the range are kept. The case and diacritics of letters are also
     * preserved.<br>
     * For example, extracting [1,3) from "a b, c; d " yields " b, c; " (note how spaces are also
     * kept).
     * @param input the original raw input from the user
     * @param range the range of words representing those captured in the capturing group
     * @return the content of the capturing group
     */
    public static String extractCapturingGroup(final String input, final InputWordRange range) {
        final Pattern pattern = Pattern.compile("^(?:[^\\p{L}]*\\p{L}+){" + range.from()
                + "}((?:[^\\p{L}]*\\p{L}+){" + (range.to() - range.from()) + "}[^\\p{L}]*)");
        final Matcher matcher = pattern.matcher(input);
        final boolean foundMatch = matcher.find();

        if (foundMatch) {
            return matcher.group(1);
        } else {
            return null; // unreachable, hopefully
        }
    }
}
