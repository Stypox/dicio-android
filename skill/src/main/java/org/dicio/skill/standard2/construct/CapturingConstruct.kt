package org.dicio.skill.standard2.construct

import org.dicio.skill.standard2.StandardMatchResult
import org.dicio.skill.standard2.helper.MatchHelper
import org.dicio.skill.standard2.helper.cumulativeWeight
import org.dicio.skill.standard2.helper.cumulativeWhitespace

data class CapturingConstruct(
    private val name: String,
    private val weight: Float
) : Construct {
    override fun match(start: Int, end: Int, ctx: MatchHelper): StandardMatchResult {
        val cumulativeWeight = ctx.getOrTokenize("cumulativeWeight", ::cumulativeWeight)
        val cumulativeWhitespace = ctx.getOrTokenize("cumulativeWhitespace", ::cumulativeWhitespace)
        val userWeight = cumulativeWeight[end] - cumulativeWeight[start]
        val whitespace = cumulativeWhitespace[end] - cumulativeWhitespace[start]

        return StandardMatchResult(
            userMatched = userWeight,
            userWeight = userWeight,
            refMatched = if (whitespace == end-start) 0.0f else weight,
            refWeight = weight,
            end = end,
            canGrow = end != ctx.userInput.length,
            capturingGroups = mapOf(name to Pair(start, end))
        )
    }
}
