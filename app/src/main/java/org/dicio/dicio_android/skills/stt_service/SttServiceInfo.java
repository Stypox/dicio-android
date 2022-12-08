package org.dicio.dicio_android.skills.stt_service;

import org.dicio.dicio_android.R;
import org.dicio.skill.Skill;
import org.dicio.skill.SkillContext;
import org.dicio.skill.SkillInfo;
import org.dicio.skill.chain.ChainSkill;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SttServiceInfo  extends SkillInfo {
    public SttServiceInfo() {
        super("stt_service", R.string.skill_stt_service, R.string.skill_sentence_example_stt_service,
                R.drawable.ic_timer_white, false);
    }

    @Override
    public boolean isAvailable(final SkillContext context) {
        return true;
    }

    @Override
    public Skill build(final SkillContext context) {
        return new ChainSkill.Builder()
                .recognize(new SimpleForwardRecognizer())
                .process(new SttServiceProcessor())
                .output(new SttServiceOutput());
    }

    @Nullable
    @Override
    public Fragment getPreferenceFragment() {
        return null;
    }
}
