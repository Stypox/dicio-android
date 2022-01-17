package org.dicio.skill.standard.word;

import java.util.regex.Pattern;

public class DiacriticsSensitiveRegexWord extends StringWord {

    private final Pattern regexPattern;

    /**
     * A word in a sentence with the indices of all possible subsequent words. When matching, the
     * word value will be interpreted as a regex, and it will be matched against the input word
     * without removing diacritics and accents from it (see e.g. CTRL+F -> Match Diacritics in
     * Firefox). For diacritics insensitive matching see {@link DiacriticsInsensitiveRegexWord}.
     *
     * @param regex a valid regex to compile and use to check if an input word matches
     * @param minimumSkippedWordsToEnd the minimum number of subsequent words that have to be
     *                                 skipped to reach the end of the sentence. Used in case the
     *                                 end of input is reached on this word. Capturing groups count
     *                                 as if two words were skipped.
     * @param nextIndices the indices of all possible subsequent words in the owning sentence; it
     *                    must always contain a value; use the length of the word array to represent
     */
    public DiacriticsSensitiveRegexWord(final String regex,
                                        final int minimumSkippedWordsToEnd,
                                        final int... nextIndices) {
        super(minimumSkippedWordsToEnd, nextIndices);
        this.regexPattern = Pattern.compile(regex);
    }

    @Override
    public boolean matches(final String inputWord, final String normalizedInputWord) {
        // match against the original input word
        return regexPattern.matcher(inputWord).matches();
    }
}
