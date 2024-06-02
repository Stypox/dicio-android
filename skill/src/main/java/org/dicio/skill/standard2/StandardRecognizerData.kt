package org.dicio.skill.standard2

import org.dicio.skill.skill.Specificity
import org.dicio.skill.standard2.construct.Construct
import org.dicio.skill.standard2.helper.MatchHelper
import org.dicio.skill.standard2.helper.initialMemToEnd

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
            val memToEnd = initialMemToEnd(cumulativeWeight)
            construct.matchToEnd(memToEnd, helper)

            if (bestRes == null || memToEnd[0].score() > bestRes.second.score()) {
                bestRes = Pair(sentenceId, memToEnd[0])
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
