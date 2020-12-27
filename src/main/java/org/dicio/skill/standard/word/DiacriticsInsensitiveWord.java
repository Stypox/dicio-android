package org.dicio.skill.standard.word;

public final class DiacriticsInsensitiveWord extends StringWord {

    private final String normalizedValue;

    /**
     * A word in a sentence with the indices of all possible subsequent words. When matching,
     * diacritics and accents will not be checked (see e.g. CTRL+F -> Match Diacritics in Firefox).
     * For diacritics-sensitive matching see {@link DiacriticsSensitiveWord}.
     *
     * @param normalizedValue the unicode NFKD normalized value for this word. Use
     *      *                 {@link org.dicio.skill.util.WordExtractor#nfkdNormalizeWord(String)}
     *      *                 to NFKD normalize a word.
     * @param minimumSkippedWordsToEnd the minimum number of subsequent words that have to be
     *                                 skipped to reach the end of the sentence. Used in case the
     *                                 end of input is reached on this word. Capturing groups count
     *                                 as if two words were skipped.
     * @param nextIndices the indices of all possible subsequent words in the owning sentence; it
     *                    must always contain a value; use the length of the word array to represent
     */
    public DiacriticsInsensitiveWord(final String normalizedValue,
                                     final int minimumSkippedWordsToEnd,
                                     final int... nextIndices) {
        super(minimumSkippedWordsToEnd, nextIndices);
        this.normalizedValue = normalizedValue;
    }

    @Override
    public boolean matches(final String inputWord, final String normalizedInputWord) {
        return normalizedValue.equals(normalizedInputWord);
    }
}
