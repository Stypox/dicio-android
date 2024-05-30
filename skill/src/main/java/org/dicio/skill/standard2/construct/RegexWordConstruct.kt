package org.dicio.skill.standard2.construct

import org.dicio.skill.standard2.StandardMatchResult
import org.dicio.skill.standard2.helper.MatchHelper
import org.dicio.skill.standard2.helper.findTokenStartingAt
import org.dicio.skill.standard2.helper.splitWords


data class RegexWordConstruct(
    private val regex: String,
    // TODO isDiacriticsSensitive
    private val isDiacriticsSensitive: Boolean,
    private val weight: Float,
) : Construct {
    private val compiledRegex = Regex(regex)

    override fun match(start: Int, end: Int, ctx: MatchHelper): StandardMatchResult {
        val token = ctx.getOrTokenize("splitWords", ::splitWords)
            .findTokenStartingAt(start)
        return if (token == null || !compiledRegex.matches(token.text)) {
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
