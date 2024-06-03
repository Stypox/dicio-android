package org.dicio.skill.standard.construct

import org.dicio.skill.standard.StandardMatchResult
import org.dicio.skill.standard.helper.MatchHelper

interface Construct {
    fun matchToEnd(memToEnd: Array<StandardMatchResult>, helper: MatchHelper)
}
