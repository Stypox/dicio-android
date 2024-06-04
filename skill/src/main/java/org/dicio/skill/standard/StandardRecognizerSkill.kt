package org.dicio.skill.standard

import org.dicio.skill.skill.Skill
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Score
import org.dicio.skill.skill.SkillInfo

abstract class StandardRecognizerSkill<T>(
    correspondingSkillInfo: SkillInfo,
    private val data: StandardRecognizerData<T>,
) : Skill<T>(correspondingSkillInfo, data.specificity) {

    override fun score(
        ctx: SkillContext,
        input: String
    ): Pair<Score, T> {
        return data.score(input)
    }
}
