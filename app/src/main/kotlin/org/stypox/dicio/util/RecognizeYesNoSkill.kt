package org.stypox.dicio.util

import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.Specificity
import org.dicio.skill.standard.StandardRecognizerData

abstract class RecognizeYesNoSkill(
    correspondingSkillInfo: SkillInfo,
    private val data: StandardRecognizerData,
) : Skill<Boolean>(correspondingSkillInfo, data.specificity) {
    override fun score(
        ctx: SkillContext,
        input: String,
        inputWords: List<String>,
        normalizedWordKeys: List<String>
    ): Pair<Float, Boolean> {
        return data.score(input, inputWords, normalizedWordKeys).let { (score, standardResult) ->
            Pair(score, standardResult.sentenceId == "yes")
        }
    }
}
