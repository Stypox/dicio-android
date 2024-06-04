package org.stypox.dicio.util

import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.AlwaysBestScore
import org.dicio.skill.skill.Score
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.Specificity

abstract class RecognizeEverythingSkill(correspondingSkillInfo: SkillInfo) :
    Skill<String>(correspondingSkillInfo, Specificity.LOW) {
    override fun score(
        ctx: SkillContext,
        input: String
    ): Pair<Score, String> {
        return Pair(AlwaysBestScore, input)
    }
}
