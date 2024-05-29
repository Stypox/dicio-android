package org.dicio.skill.standard2.component

import org.dicio.skill.standard2.StandardMatchResult
import org.dicio.skill.standard2.helper.MatchHelper

object OptionalComponent : Component {
    override fun match(start: Int, end: Int, ctx: MatchHelper): StandardMatchResult {
        return StandardMatchResult.empty(start, false)
    }
}
