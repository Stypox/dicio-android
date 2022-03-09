package org.dicio.skill;

import org.dicio.skill.chain.InputRecognizer;

public abstract class FallbackSkill extends Skill {

    @Override
    public InputRecognizer.Specificity specificity() {
        return InputRecognizer.Specificity.low; // useless
    }

    @Override
    public float score() {
        return 0; // useless
    }
}
