package org.dicio.skill;

import androidx.annotation.Nullable;

import org.dicio.skill.chain.InputRecognizer;

public abstract class FallbackSkill extends Skill {

    /**
     * @see Skill#Skill(SkillInfo)
     */
    public FallbackSkill(@Nullable final SkillInfo skillInfo) {
        super(skillInfo);
    }

    @Override
    public InputRecognizer.Specificity specificity() {
        return InputRecognizer.Specificity.low; // useless
    }

    @Override
    public float score() {
        return 0; // useless
    }
}
