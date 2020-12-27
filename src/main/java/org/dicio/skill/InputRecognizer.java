package org.dicio.skill;

import java.util.List;

/**
 * Recognizes input by giving a score to it, and is able to extract data from the provided input.
 * Even though everything computation step could be done here, it is better to keep things separate,
 * so that {@link InputRecognizer}'s only purpose is to collect information from user input. Use
 * {@link IntermediateProcessor} for input-unrelated intermediate steps. Methods in this class do
 * not allow throwing exceptions.
 * @param <ResultType> the type of the data extracted from the input
 */
public interface InputRecognizer<ResultType> {
    enum Specificity {
        high,
        medium,
        low,
    }

    /**
     * The specificity of this input recognizer
     * @return {@link Specificity#high high} for specific things (e.g. weather);<br>
     * {@link Specificity#medium medium} for not-too-specific things (e.g. calculator that parses numbers);<br>
     * {@link Specificity#low low} for broad things (e.g. omniscient API);<br>
     */
    Specificity specificity();

    /**
     * Sets the current input for the recognizer,
     * to be used when {@link #score() score()} is called
     * @param input raw input from the user
     * @param inputWords normalized input split into words
     * @param normalizedInputWords the collation keys for all of the input words (in the same
     *                               order), needed for diacritics-insensitive matching, built by
     *                               passing inputWords to
     *                               {@link org.dicio.skill.util.WordExtractor#normalizeWords(List)}
     */
    void setInput(String input, List<String> inputWords, List<String> normalizedInputWords);

    /**
     * The score of the input previously set with {@link #setInput(String, List, List) setInput()}
     * for this input recognizer
     * @return a number in range [0.0, 1.0]
     */
    float score();

    /**
     * If this input recognizer has the highest score, this function is called to generate a result
     * based on the input previously set with {@link #setInput(String, List, List) setInput()}
     * @return a result useful for the next step of the computation
     */
    ResultType getResult();

    /**
     * To prevent excessive memory usage, release all temporary resources and set to {@code null}
     * all temporary variables used while calculating the score and getting the result.
     */
    void cleanup();
}
