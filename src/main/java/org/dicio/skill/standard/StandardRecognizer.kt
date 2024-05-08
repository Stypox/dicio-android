package org.dicio.skill.standard

import org.dicio.skill.chain.InputRecognizer
import kotlin.math.abs

class StandardRecognizer(private val data: StandardRecognizerData) :
    InputRecognizer<StandardResult>() {
    private var input: String? = null
    private var inputWords: List<String>
    private var normalizedInputWords: List<String>

    private var bestResultSoFar: PartialScoreResult? = null
    private var bestSentenceIdSoFar: String? = null


    /////////////////
    // Constructor //
    /////////////////
    init {
        this.inputWords = listOf()
        this.normalizedInputWords = listOf()
    }

    constructor(
        specificity: Specificity,
        sentences: Array<Sentence>
    ) : this(StandardRecognizerData(specificity, *sentences))


    ///////////////////////////////
    // InputRecognizer overrides //
    ///////////////////////////////
    override fun specificity(): Specificity {
        return data.specificity
    }

    override fun setInput(
        input: String,
        inputWords: List<String>,
        normalizedInputWords: List<String>
    ) {
        this.input = input
        this.inputWords = inputWords
        this.normalizedInputWords = normalizedInputWords
    }

    override fun score(): Float {
        bestResultSoFar = data.sentences[0].score(inputWords, normalizedInputWords)
        var bestValueSoFar = bestResultSoFar!!.value(inputWords.size)
        bestSentenceIdSoFar = data.sentences[0].sentenceId

        for (i in 1 until data.sentences.size) {
            val result = data.sentences[i].score(inputWords, normalizedInputWords)
            val value = result.value(inputWords.size)

            val valuesAlmostEqual = abs((value - bestValueSoFar).toDouble()) < 0.01f
            val lessWordsInCapturingGroups = (result.wordsInCapturingGroups
                    < bestResultSoFar!!.wordsInCapturingGroups)

            if ((valuesAlmostEqual && lessWordsInCapturingGroups) || value > bestValueSoFar) {
                // update the best result so far also if new result evaluates approximately equal
                // but has less words in capturing groups
                bestResultSoFar = result
                bestValueSoFar = value
                bestSentenceIdSoFar = data.sentences[i].sentenceId
            }
        }

        return bestResultSoFar!!.value(inputWords.size)
    }

    override val result: StandardResult
        get() = bestResultSoFar!!.toStandardResult(bestSentenceIdSoFar!!, input!!)

    override fun cleanup() {
        input = null
        inputWords = listOf()
        normalizedInputWords = listOf()
        bestResultSoFar = null
        bestSentenceIdSoFar = null
    }
}
