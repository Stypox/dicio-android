package org.dicio.skill

import org.dicio.skill.chain.InputRecognizer.Specificity

abstract class FallbackSkill : Skill() {
    override fun specificity(): Specificity {
        return Specificity.LOW // useless
    }

    override fun score(): Float {
        return 0.0f // useless
    }
}
