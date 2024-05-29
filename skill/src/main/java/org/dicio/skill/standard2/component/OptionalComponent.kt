package org.dicio.skill.standard2.component

import org.dicio.skill.standard2.StandardMatchResult
import org.dicio.skill.standard2.helper.MatchHelper

data class OptionalComponent(
    val component: Component
) : Component {
    override fun match(start: Int, end: Int, ctx: MatchHelper): StandardMatchResult {
        return StandardMatchResult.keepBest(
            StandardMatchResult.empty(start, false),
            component.match(start, end, ctx)
        )
    }
}
