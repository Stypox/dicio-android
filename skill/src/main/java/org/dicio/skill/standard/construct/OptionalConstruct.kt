package org.dicio.skill.standard.construct

import org.dicio.skill.standard.StandardScore
import org.dicio.skill.standard.helper.MatchHelper

class OptionalConstruct : Construct {
    override fun matchToEnd(memToEnd: Array<StandardScore>, helper: MatchHelper) {
    }

    override fun toString(): String {
        return "(?)"
    }
}
