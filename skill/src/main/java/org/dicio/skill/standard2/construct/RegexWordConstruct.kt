package org.dicio.skill.standard2.construct

import org.dicio.skill.standard2.StandardMatchResult
import org.dicio.skill.standard2.helper.MatchHelper
import org.dicio.skill.standard2.helper.normalizeMemToEnd


data class RegexWordConstruct(
    private val regex: String,
    // TODO isDiacriticsSensitive
    private val isDiacriticsSensitive: Boolean,
    private val weight: Float,
) : Construct {
    private val compiledRegex = Regex(regex)

    override fun matchToEnd(memToEnd: Array<StandardMatchResult>, helper: MatchHelper) {
        val cumulativeWeight = helper.cumulativeWeight
        val splitWords = helper.splitWords
        val splitWordsIndices = helper.splitWordsIndices

        for (start in memToEnd.indices) {
            val wordIndex = splitWordsIndices[start]
            if (wordIndex >= 0) {
                val word = splitWords[wordIndex]
                if (compiledRegex.matches(word.text)) {
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
