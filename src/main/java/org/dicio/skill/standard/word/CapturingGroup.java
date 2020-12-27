package org.dicio.skill.standard.word;

public final class CapturingGroup extends BaseWord {

    final String name;

    /**
     * A capturing group in a sentence with the indices of all possible subsequent base words
     *
     * @param name the capturing group name, used for identification purposes
     * @param minimumSkippedWordsToEnd the minimum number of subsequent words that have to be
     *                                 skipped to reach the end of the sentence. Used in case the
     *                                 end of input is reached on this word. Capturing groups count
     *                                 as if two words were skipped.
     * @param nextIndices the indices of all possible subsequent words in the owning sentence; it
     *                    must always contain a value; use the length of the word array to represent
     */
    public CapturingGroup(final String name,
                          final int minimumSkippedWordsToEnd,
                          final int... nextIndices) {
        super(minimumSkippedWordsToEnd, nextIndices);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
