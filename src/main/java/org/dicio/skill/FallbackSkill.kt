package org.dicio.skill

import org.dicio.skill.chain.InputRecognizer.Specificity

abstract class FallbackSkill(
    correspondingSkillInfo: SkillInfo,
) : Skill(
    correspondingSkillInfo,
    Specificity.LOW, // useless
) {
    override fun score(): Float {
        return 0.0f // useless
    }
}
