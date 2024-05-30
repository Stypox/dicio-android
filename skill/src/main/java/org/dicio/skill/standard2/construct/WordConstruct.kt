package org.dicio.skill.standard2.construct

import org.dicio.skill.standard2.StandardMatchResult
import org.dicio.skill.standard2.helper.MatchHelper
import org.dicio.skill.standard2.helper.findTokenStartingAt
import org.dicio.skill.standard2.helper.splitWords


data class WordConstruct(
    private val text: String,
    // TODO isDiacriticsSensitive
    private val isDiacriticsSensitive: Boolean,
    private val weight: Float,
) : Construct {
    override fun match(start: Int, end: Int, helper: MatchHelper): StandardMatchResult {
        val token = helper.splitWords.findTokenStartingAt(start)
        return if (token == null || token.text != text) {
            // canGrow=false since even if end was bigger we wouldn't match anything more
            StandardMatchResult(0.0f, 0.0f, 0.0f, weight, start, false, null)
        } else if (token.end > end) {
            // canGrow=true since if end was bigger we would be able to match the word
            StandardMatchResult(0.0f, 0.0f, 0.0f, weight, start, true, null)
        } else {
            // canGrow=false since WordComponent matches only one word at a time
            StandardMatchResult(1.0f, 1.0f, weight, weight, token.end, false, null)
        }
    }
}
