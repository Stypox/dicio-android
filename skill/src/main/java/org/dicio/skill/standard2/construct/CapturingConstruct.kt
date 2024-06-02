package org.dicio.skill.standard2.construct

import org.dicio.skill.standard2.StandardMatchResult
import org.dicio.skill.standard2.capture.StringRangeCapture
import org.dicio.skill.standard2.helper.MatchHelper
import org.dicio.skill.standard2.helper.normalizeMemToEnd

data class CapturingConstruct(
    private val name: String,
    private val weight: Float
) : Construct {
    override fun matchToEnd(memToEnd: Array<StandardMatchResult>, helper: MatchHelper) {
        val cumulativeWeight = helper.cumulativeWeight
        val cumulativeWhitespace = helper.cumulativeWhitespace

        for (start in memToEnd.indices) {
            val cumulativeWeightStart = cumulativeWeight[start]
            val cumulativeWhitespaceStart = cumulativeWhitespace[start]

            memToEnd[start] = (start..helper.userInput.length)
                .map { end ->
                    val userWeight = cumulativeWeight[end] - cumulativeWeightStart
                    val whitespace = cumulativeWhitespace[end] - cumulativeWhitespaceStart

                    memToEnd[end].plus(
                        userMatched = userWeight,
                        userWeight = userWeight,
                        refMatched = if (whitespace == end - start) 0.0f else weight,
                        refWeight = weight,
                        capturingGroup = StringRangeCapture(name, start, end),
                    )
                }
                // it is impossible for the result to be null because the (start..userInput.length)
                // range is always non-empty (even if start == userInput.length), hence the !!
                .fold(null, StandardMatchResult::keepBest)!!
        }

        normalizeMemToEnd(memToEnd, helper.cumulativeWeight)
    }
}
