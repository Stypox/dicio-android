package org.dicio.skill.standard.word;

public final class DiacriticsSensitiveWord extends StringWord {

    private final String value;

    /**
     * A word in a sentence with the indices of all possible subsequent words. When matching,
     * diacritics and accents will be checked (see e.g. CTRL+F -> Match Diacritics in Firefox). For
     * diacritics insensitive matching see {@link DiacriticsInsensitiveWord}.
     *
     * @param value the value of the word, made of only unicode letters
     * @param minimumSkippedWordsToEnd the minimum number of subsequent words that have to be
     *                                 skipped to reach the end of the sentence. Used in case the
     *                                 end of input is reached on this word. Capturing groups count
     *                                 as if two words were skipped.
     * @param nextIndices the indices of all possible subsequent words in the owning sentence; it
     *                    must always contain a value; use the length of the word array to represent
     */
    public DiacriticsSensitiveWord(final String value,
                                   final int minimumSkippedWordsToEnd,
                                   final int... nextIndices) {
        super(minimumSkippedWordsToEnd, nextIndices);
        this.value = value;
    }

    @Override
    public boolean matches(final String inputWord, final String normalizedInputWord) {
        return value.equals(inputWord);
    }
}
