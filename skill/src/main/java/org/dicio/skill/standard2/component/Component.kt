package org.dicio.skill.standard2.component

import org.dicio.skill.standard2.StandardMatchResult
import org.dicio.skill.standard2.helper.MatchHelper

interface Component {
    fun match(start: Int, end: Int, ctx: MatchHelper): StandardMatchResult
}
