package org.dicio.skill.standard.construct

import org.dicio.skill.standard.StandardScore
import org.dicio.skill.standard.helper.MatchHelper

interface Construct {
    fun matchToEnd(memToEnd: Array<StandardScore>, helper: MatchHelper)
}
