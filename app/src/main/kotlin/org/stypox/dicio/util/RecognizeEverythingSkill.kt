package org.stypox.dicio.util

import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.Specificity

abstract class RecognizeEverythingSkill(correspondingSkillInfo: SkillInfo) :
    Skill<String>(correspondingSkillInfo, Specificity.LOW) {
    override fun score(
        ctx: SkillContext,
        input: String,
        inputWords: List<String>,
        normalizedWordKeys: List<String>
    ): Pair<Float, String> {
        return Pair(1.0f, input)
    }
}
