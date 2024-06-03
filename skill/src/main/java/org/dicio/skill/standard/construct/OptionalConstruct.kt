package org.dicio.skill.standard.construct

import org.dicio.skill.standard.StandardMatchResult
import org.dicio.skill.standard.helper.MatchHelper

class OptionalConstruct : Construct {
    override fun matchToEnd(memToEnd: Array<StandardMatchResult>, helper: MatchHelper) {
    }

    override fun toString(): String {
        return "(?)"
    }
}
