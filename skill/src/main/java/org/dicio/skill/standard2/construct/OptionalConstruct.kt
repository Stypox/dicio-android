package org.dicio.skill.standard2.construct

import org.dicio.skill.standard2.StandardMatchResult
import org.dicio.skill.standard2.helper.MatchHelper

class OptionalConstruct : Construct {
    override fun match(start: Int, end: Int, helper: MatchHelper): StandardMatchResult {
        return StandardMatchResult.empty(start, false)
    }
}
