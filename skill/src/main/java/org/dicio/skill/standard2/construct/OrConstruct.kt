package org.dicio.skill.standard2.construct

import org.dicio.skill.standard2.StandardMatchResult
import org.dicio.skill.standard2.helper.MatchHelper

data class OrConstruct(
    private val constructs: List<Construct>
) : Construct {
    override fun match(start: Int, end: Int, ctx: MatchHelper): StandardMatchResult {
        return constructs
            .map { it.match(start, end, ctx) }
            .fold(null, StandardMatchResult::keepBest)
            // edge case when `components` is empty (should never happen, but this handles it well)
            ?: StandardMatchResult.empty(start, false)
    }
}
