package org.dicio.dicio_android.skills.fallback;

import org.dicio.skill.InputRecognizer;
import org.dicio.dicio_android.skills.Skill;

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
