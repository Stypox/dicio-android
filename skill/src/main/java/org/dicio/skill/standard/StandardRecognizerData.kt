package org.dicio.skill.standard

import org.dicio.skill.skill.Specificity
import org.dicio.skill.standard.capture.LocaleAndTranslation
import org.dicio.skill.standard.construct.Construct
import org.dicio.skill.standard.util.MatchHelper
import org.dicio.skill.standard.util.initialMemToEnd

open class StandardRecognizerData<out T>(
    val specificity: Specificity,
    private val converter: (input: String, sentenceId: String, matchResult: StandardScore) -> T,
    private val sentencesWithId: List<Pair<String, Construct>>,
    // used by LanguageNameCapturingConstruct
    private val languageNames: List<LocaleAndTranslation>,
) {
    fun score(input: String): Pair<StandardScore, T> {
        val helper = MatchHelper(input, languageNames)
        val cumulativeWeight = helper.cumulativeWeight

        var bestRes: Pair<String, StandardScore>? = null
        for ((sentenceId, construct) in sentencesWithId) {
            val memToEnd = initialMemToEnd(cumulativeWeight)
            construct.matchToEnd(memToEnd, helper)

            if (bestRes == null || memToEnd[0].score() > bestRes.second.score()) {
                bestRes = Pair(sentenceId, memToEnd[0])
            }
        }

        // it is impossible for the result to be null because sentencesWithId is non-empty
        bestRes!!
        return Pair(
            bestRes.second,
            converter(
                input,
                bestRes.first,
                bestRes.second,
            )
        )
    }
}
