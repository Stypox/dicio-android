package org.dicio.skill.standard2.construct

import org.dicio.skill.standard2.StandardMatchResult
import org.dicio.skill.standard2.helper.MatchHelper

interface Construct {
    fun matchToEnd(memToEnd: Array<StandardMatchResult>, helper: MatchHelper)
}
