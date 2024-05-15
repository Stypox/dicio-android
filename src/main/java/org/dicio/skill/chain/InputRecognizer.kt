package org.dicio.skill.chain

import org.dicio.skill.SkillComponent
import org.dicio.skill.util.CleanableUp

/**
 * Recognizes input by giving a score to it, and is able to extract data from the provided input.
 * Even though everything computation step could be done here, it is better to keep things separate,
 * so that [InputRecognizer]'s only purpose is to collect information from user input. Use
 * [IntermediateProcessor] for input-unrelated intermediate steps. Methods in this class do
 * not allow throwing exceptions.
 * @param ResultType the type of the data extracted from the input
*/
abstract class InputRecognizer<ResultType> constructor(
    /**
     * The specificity of this input recognizer
     * @return [Specificity.HIGH] for specific things (e.g. weather);<br></br>
     * [Specificity.MEDIUM] for not-too-specific things (e.g. calculator that parses
     * numbers);<br></br>
     * [Specificity.LOW] for broad things (e.g. omniscient API);<br></br>
     */
    val specificity: Specificity
) : SkillComponent(), CleanableUp {
    enum class Specificity {
        HIGH,
        MEDIUM,
        LOW,
    }

    /**
     * Sets the current input for the recognizer,
     * to be used when [score()][.score] is called
     * @param input raw input from the user
     * @param inputWords normalized input split into words
     * @param normalizedInputWords the collation keys for all of the input words (in the same
     * order), needed for diacritics-insensitive matching, built by
     * passing inputWords to
     * [org.dicio.skill.util.WordExtractor.normalizeWords]
     */
    abstract fun setInput(
        input: String,
        inputWords: List<String>,
        normalizedInputWords: List<String>
    )

    /**
     * The score of the input previously set with [setInput()][.setInput]
     * for this input recognizer
     * @return a number in range [0.0, 1.0]
     */
    abstract fun score(): Float

    /**
     * If this input recognizer has the highest score, this function is called to generate a result
     * based on the input previously set with [setInput()][.setInput]
     * @return a result useful for the next step of the computation
     */
    abstract val result: ResultType

    /**
     * To prevent excessive memory usage, release all temporary resources and set to `null`
     * all temporary variables used while calculating the score and getting the result.
     */
    abstract override fun cleanup()
}
