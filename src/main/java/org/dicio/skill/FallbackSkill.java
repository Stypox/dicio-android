package org.dicio.skill;

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
