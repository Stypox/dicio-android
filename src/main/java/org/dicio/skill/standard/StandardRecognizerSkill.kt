package org.dicio.skill.standard

import org.dicio.skill.skill.Skill
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import kotlin.math.abs

abstract class StandardRecognizerSkill(
    correspondingSkillInfo: SkillInfo,
    private val data: StandardRecognizerData,
) : Skill<StandardResult>(correspondingSkillInfo, data.specificity) {

    override fun score(
        ctx: SkillContext,
        input: String,
        inputWords: List<String>,
        normalizedWordKeys: List<String>
    ): Pair<Float, StandardResult> {
        var bestResultSoFar = data.sentences[0].score(inputWords, normalizedWordKeys)
        var bestValueSoFar = bestResultSoFar.value(inputWords.size)
        var bestSentenceIdSoFar = data.sentences[0].sentenceId

        for (i in 1 until data.sentences.size) {
            val result = data.sentences[i].score(inputWords, normalizedWordKeys)
            val value = result.value(inputWords.size)

            val valuesAlmostEqual = abs((value - bestValueSoFar).toDouble()) < 0.01f
            val lessWordsInCapturingGroups = (result.wordsInCapturingGroups
                    < bestResultSoFar.wordsInCapturingGroups)

            if ((valuesAlmostEqual && lessWordsInCapturingGroups) || value > bestValueSoFar) {
                // update the best result so far also if new result evaluates approximately equal
                // but has less words in capturing groups
                bestResultSoFar = result
                bestValueSoFar = value
                bestSentenceIdSoFar = data.sentences[i].sentenceId
            }
        }

        return Pair(
            bestResultSoFar.value(inputWords.size),
            bestResultSoFar.toStandardResult(bestSentenceIdSoFar, input),
        )
    }
}
