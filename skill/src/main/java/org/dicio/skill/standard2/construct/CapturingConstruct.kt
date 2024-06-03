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
        val originalMemToEnd = memToEnd.clone()

        var lastCapturingGroupEnd: Int = helper.userInput.length
        for (start in memToEnd.indices.reversed()) {
            val userWeight = cumulativeWeight[lastCapturingGroupEnd] - cumulativeWeight[start]
            val whitespace = cumulativeWhitespace[lastCapturingGroupEnd] - cumulativeWhitespace[start]
            // using originalMemToEnd because originalMemToEnd[lastCapturingGroupEnd] will already
            // have been changed
            val ifContinuingCapturingGroup = originalMemToEnd[lastCapturingGroupEnd].plus(
                userMatched = userWeight,
                userWeight = userWeight,
                refMatched = if (whitespace == lastCapturingGroupEnd - start) 0.0f else weight,
                refWeight = weight,
                capturingGroup = StringRangeCapture(name, start, lastCapturingGroupEnd),
            )

            val ifSkippingCapturingGroup = memToEnd[start].plus(refWeight = weight)
            if (ifContinuingCapturingGroup.score() > ifSkippingCapturingGroup.score()) {
                memToEnd[start] = ifContinuingCapturingGroup

                val ifCreatingNewCapturingGroup = originalMemToEnd[start].plus(
                    refMatched = weight,
                    refWeight = weight,
                )
                if (ifCreatingNewCapturingGroup.score() > ifContinuingCapturingGroup.score()) {
                    lastCapturingGroupEnd = start
                }
            } else {
                lastCapturingGroupEnd = start
                memToEnd[start] = ifSkippingCapturingGroup
            }
        }

        normalizeMemToEnd(memToEnd, helper.cumulativeWeight)
    }

    override fun toString(): String {
        return ".$name."
    }
}
