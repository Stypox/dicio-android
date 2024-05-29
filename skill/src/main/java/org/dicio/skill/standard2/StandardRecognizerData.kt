package org.dicio.skill.standard2

import org.dicio.skill.skill.Specificity
import org.dicio.skill.standard2.construct.Construct
import org.dicio.skill.standard2.helper.MatchHelper
import org.dicio.skill.standard2.helper.cumulativeWeight

open class StandardRecognizerData<out T>(
    val specificity: Specificity,
    private val converter: (StandardMatchResult) -> T,
    private val construct: Construct,
) {
    fun score(input: String): Pair<Float, T> {
        val helper = MatchHelper(input)
        val cumulativeWeight = helper.getOrTokenize("cumulativeWeight", ::cumulativeWeight)

        val bestRes = (0..input.length)
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

        return Pair(bestRes.scoreIn01Range(), converter(bestRes))
    }
}
