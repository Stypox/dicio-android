package org.dicio.skill.standard.construct

import org.dicio.skill.standard.StandardScore
import org.dicio.skill.standard.capture.StringRangeCapture
import org.dicio.skill.standard.util.MatchHelper
import org.dicio.skill.standard.util.normalizeMemToEnd

data class LanguageNameCapturingConstruct(
    private val name: String,
    private val weight: Float
) : Construct {
    override fun matchToEnd(memToEnd: Array<StandardScore>, helper: MatchHelper) {
        helper.languageNames
    }

    override fun toString(): String {
        return ".$name."
    }
}
