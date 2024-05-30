package org.dicio.skill.standard2

import org.dicio.skill.skill.Specificity
import org.dicio.skill.standard2.construct.Construct
import org.dicio.skill.standard2.helper.MatchHelper
import org.dicio.skill.standard2.helper.cumulativeWeight

open class StandardRecognizerData<out T>(
    val specificity: Specificity,
    private val converter: (input: String, sentenceId: String, matchResult: StandardMatchResult) -> T,
    private val sentencesWithId: List<Pair<String, Construct>>,
) {
    fun score(input: String): Pair<Float, T> {
        val helper = MatchHelper(input)
        val cumulativeWeight = helper.cumulativeWeight

        var bestRes: Pair<String, StandardMatchResult>? = null
        for ((sentenceId, construct) in sentencesWithId) {
            construct.setupCache(helper)
            val bestSentenceRes = (0..input.length)
                .map { start ->
                    val res = construct.match(0, input.length, helper)
                    return@map res.copy(
                        userWeight = res.userWeight +
                                (cumulativeWeight[start] - cumulativeWeight[0]) +
                                (cumulativeWeight[input.length] - cumulativeWeight[res.end]),
                        end = input.length,
                    )
                }
                // it is impossible for the result to be null because the (0..input.length) range
                // is always non-empty (even if input.length == 0), hence the !!
                .fold(null, StandardMatchResult::keepBest)!!
            construct.destroyCache()

            if (bestRes == null || bestSentenceRes.score() > bestRes.second.score()) {
                bestRes = Pair(sentenceId, bestSentenceRes)
            }
        }

        // it is impossible for the result to be null because sentencesWithId is non-empty
        bestRes!!
        return Pair(
            bestRes.second.scoreIn01Range(),
            converter(
                input,
                bestRes.first,
                bestRes.second,
            )
        )
    }
}
