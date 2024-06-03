package org.dicio.skill.standard2.construct

import org.dicio.skill.standard2.StandardMatchResult
import org.dicio.skill.standard2.helper.MatchHelper
import org.dicio.skill.standard2.helper.normalizeMemToEnd


data class WordConstruct(
    private val text: String,
    private val isRegex: Boolean,
    private val isDiacriticsSensitive: Boolean,
    private val weight: Float,
) : Construct {
    private val compiledRegex = if (isRegex) Regex(text) else null

    override fun matchToEnd(memToEnd: Array<StandardMatchResult>, helper: MatchHelper) {
        val cumulativeWeight = helper.cumulativeWeight

        for (start in memToEnd.indices) {
            val wordIndex = helper.splitWordsIndices[start]
            if (wordIndex >= 0) {
                val word = helper.splitWords[wordIndex]
                val wordText = if (isDiacriticsSensitive)
                    word.originalText
                else
                    word.nfkdNormalizedText

                if ((compiledRegex?.matches(wordText)) ?: (text == wordText)) {
                    val userWeight = cumulativeWeight[word.end] - cumulativeWeight[start]
                    memToEnd[start] = StandardMatchResult.keepBest(
                        memToEnd[word.end].plus(
                            userMatched = userWeight,
                            userWeight = userWeight,
                            refMatched = weight,
                            refWeight = weight,
                        ),
                        memToEnd[start].plus(refWeight = weight),
                    )
                    continue
                }
            }

            memToEnd[start] = memToEnd[start].plus(refWeight = weight)
        }

        normalizeMemToEnd(memToEnd, cumulativeWeight)
    }
}
