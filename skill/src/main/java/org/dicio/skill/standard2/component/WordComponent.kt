package org.dicio.skill.standard2.component

import org.dicio.skill.standard2.StandardMatchResult
import org.dicio.skill.standard2.helper.MatchHelper
import org.dicio.skill.standard2.helper.findTokenStartingAt
import org.dicio.skill.standard2.helper.splitWords


data class WordComponent(
    private val text: String,
    private val weight: Float,
) : Component {
    override fun match(start: Int, end: Int, ctx: MatchHelper): StandardMatchResult {
        val token = ctx.getOrTokenize("splitWords", ::splitWords)
            .findTokenStartingAt(start)
        return if (token == null || token.text != text) {
            // canGrow=false since even if end was bigger we wouldn't match anything more
            StandardMatchResult(0.0f, 0.0f, 0.0f, weight, start, false, mapOf())
        } else if (token.end > end) {
            // canGrow=true since if end was bigger we would be able to match the word
            StandardMatchResult(0.0f, 0.0f, 0.0f, weight, start, true, mapOf())
        } else {
            // canGrow=false since WordComponent matches only one word at a time
            StandardMatchResult(1.0f, 1.0f, weight, weight, token.end, false, mapOf())
        }
    }
}
