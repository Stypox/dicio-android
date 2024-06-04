package org.dicio.skill.old_standard_impl

import org.dicio.skill.skill.FloatScore
import org.dicio.skill.skill.Specificity
import kotlin.math.abs

open class StandardRecognizerData(val specificity: Specificity, vararg val sentences: Sentence) {
    fun score(
        input: String,
        inputWords: List<String>,
        normalizedWordKeys: List<String>
    ): Pair<FloatScore, StandardResult> {
        var bestResultSoFar = sentences[0].score(inputWords, normalizedWordKeys)
        var bestValueSoFar = bestResultSoFar.value(inputWords.size)
        var bestSentenceIdSoFar = sentences[0].sentenceId

        for (i in 1 until sentences.size) {
            val result = sentences[i].score(inputWords, normalizedWordKeys)
            val value = result.value(inputWords.size)

            val valuesAlmostEqual = abs((value - bestValueSoFar).toDouble()) < 0.01f
            val lessWordsInCapturingGroups = (result.wordsInCapturingGroups
                    < bestResultSoFar.wordsInCapturingGroups)

            if ((valuesAlmostEqual && lessWordsInCapturingGroups) || value > bestValueSoFar) {
                // update the best result so far also if new result evaluates approximately equal
                // but has less words in capturing groups
                bestResultSoFar = result
                bestValueSoFar = value
                bestSentenceIdSoFar = sentences[i].sentenceId
            }
        }

        return Pair(
            FloatScore(bestResultSoFar.value(inputWords.size)),
            bestResultSoFar.toStandardResult(bestSentenceIdSoFar, input),
        )
    }
}
