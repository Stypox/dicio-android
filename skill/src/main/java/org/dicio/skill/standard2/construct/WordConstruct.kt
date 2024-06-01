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
    private var precalculatedResults: Array<StandardMatchResult> = arrayOf()

    override fun match(start: Int, end: Int, helper: MatchHelper): StandardMatchResult {
        val cachedResult = precalculatedResults[start]
        return if (cachedResult.end > end) {
            // canGrow=true since if end was bigger we would be able to match the word
            StandardMatchResult(0.0f, 0.0f, 0.0f, weight, start, true, null)
        } else {
            cachedResult
        }
    }

    override fun setupCache(helper: MatchHelper) {
        precalculatedResults = Array(helper.userInput.length + 1) { start ->
            val wordIndex = helper.splitWordsIndices[start]
            if (wordIndex < 0) {
                // canGrow=false since even if end was bigger we wouldn't match anything more
                return@Array StandardMatchResult(0.0f, 0.0f, 0.0f, weight, start, false, null)
            }

            val word = helper.splitWords[wordIndex]
            if (word.text != text) {
                // canGrow=false since even if end was bigger we wouldn't match anything more
                return@Array StandardMatchResult(0.0f, 0.0f, 0.0f, weight, start, false, null)
            }

            // canGrow=false since WordComponent matches only one word at a time
            return@Array StandardMatchResult(1.0f, 1.0f, weight, weight, word.end, false, null)
        }
    }

    override fun destroyCache() {
        precalculatedResults = arrayOf()
    }
}
