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

        var lastCapturingGroupEnd: Int = helper.userInput.length
        for (start in memToEnd.indices.reversed()) {
            val userWeight = cumulativeWeight[lastCapturingGroupEnd] - cumulativeWeight[start]
            val whitespace = cumulativeWhitespace[lastCapturingGroupEnd] - cumulativeWhitespace[start]
            val ifContinuingCapturingGroup = memToEnd[lastCapturingGroupEnd].plus(
                userMatched = userWeight,
                userWeight = userWeight,
                refMatched = if (whitespace == lastCapturingGroupEnd - start) 0.0f else weight,
                refWeight = weight,
                capturingGroup = StringRangeCapture(name, start, lastCapturingGroupEnd),
            )

            val ifSkippingCapturingGroup = memToEnd[start].plus(refWeight = weight)
            if (ifContinuingCapturingGroup.score() > ifSkippingCapturingGroup.score()) {
                memToEnd[start] = ifContinuingCapturingGroup
            } else {
                lastCapturingGroupEnd = start
                memToEnd[start] = ifSkippingCapturingGroup
            }
        }

        normalizeMemToEnd(memToEnd, helper.cumulativeWeight)
    }
}
