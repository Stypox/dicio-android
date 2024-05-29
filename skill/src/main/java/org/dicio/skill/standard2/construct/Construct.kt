package org.dicio.skill.standard2.construct

import org.dicio.skill.standard2.StandardMatchResult
import org.dicio.skill.standard2.helper.MatchHelper

interface Construct {
    fun match(start: Int, end: Int, ctx: MatchHelper): StandardMatchResult
}
