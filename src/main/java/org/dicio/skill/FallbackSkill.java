package org.dicio.skill;

import org.dicio.skill.chain.InputRecognizer;

public interface FallbackSkill extends Skill {
    @Override
    default InputRecognizer.Specificity specificity() {
        return InputRecognizer.Specificity.low; // useless
    }

    @Override
    default float score() {
        return 0; // useless
    }
}
