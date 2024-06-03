package org.dicio.skill.standard.construct

import org.dicio.skill.standard.StandardMatchResult
import org.dicio.skill.standard.helper.MatchHelper
import org.dicio.skill.standard.helper.normalizeMemToEnd

data class CompositeConstruct(
    private val constructs: List<Construct>
) : Construct {
    override fun matchToEnd(memToEnd: Array<StandardMatchResult>, helper: MatchHelper) {
        for (i in constructs.indices.reversed()) {
            constructs[i].matchToEnd(memToEnd, helper)
        }

        normalizeMemToEnd(memToEnd, helper.cumulativeWeight)
    }

    override fun toString(): String {
        return "(" + constructs.joinToString(" ") { it.toString() } + ")"
    }
}
