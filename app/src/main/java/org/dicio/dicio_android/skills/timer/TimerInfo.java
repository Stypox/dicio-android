package org.dicio.dicio_android.skills.timer;

import static org.dicio.dicio_android.Sections.getSection;
import static org.dicio.dicio_android.Sections.isSectionAvailable;
import static org.dicio.dicio_android.SectionsGenerated.timer;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.skills.search.DuckDuckGoProcessor;
import org.dicio.dicio_android.skills.search.SearchOutput;
import org.dicio.skill.Skill;
import org.dicio.skill.SkillContext;
import org.dicio.skill.SkillInfo;
import org.dicio.skill.chain.ChainSkill;
import org.dicio.skill.standard.StandardRecognizer;

public class TimerInfo extends SkillInfo {

    public TimerInfo() {
        super("timer", R.string.skill_name_timer, R.string.skill_sentence_example_timer,
                R.drawable.ic_timer_white, false);
    }

    @Override
    public boolean isAvailable(final SkillContext context) {
        return isSectionAvailable(timer);
    }

    @Override
    public Skill build(final SkillContext context) {
        return new ChainSkill.Builder(this)
                .recognize(new StandardRecognizer(getSection(timer)))
                .process(new TimerProcessor())
                .output(new TimerOutput());
    }

    @Nullable
    @Override
    public Fragment getPreferenceFragment() {
        return null;
    }
}
