package org.dicio.skill.standard2.construct

import org.dicio.skill.standard2.StandardMatchResult
import org.dicio.skill.standard2.helper.MatchHelper
import org.dicio.skill.standard2.helper.normalizeMemToEnd

data class CompositeConstruct(
    private val constructs: List<Construct>
) : Construct {
    override fun matchToEnd(memToEnd: Array<StandardMatchResult>, helper: MatchHelper) {
        for (i in constructs.indices.reversed()) {
            constructs[i].matchToEnd(memToEnd, helper)
        }

        normalizeMemToEnd(memToEnd, helper.cumulativeWeight)
    }
}
