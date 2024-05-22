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
        return data.score(input, inputWords, normalizedWordKeys)
    }
}
