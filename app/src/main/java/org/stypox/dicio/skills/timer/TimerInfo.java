package org.stypox.dicio.skills.timer;

import static org.stypox.dicio.Sections.getSection;
import static org.stypox.dicio.Sections.isSectionAvailable;
import static org.stypox.dicio.SectionsGenerated.timer;
import static org.stypox.dicio.SectionsGenerated.util_yes_no;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.stypox.dicio.R;
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
        return isSectionAvailable(timer) && isSectionAvailable(util_yes_no)
                && context.getNumberParserFormatter() != null;
    }

    @Override
    public Skill build(final SkillContext context) {
        return new ChainSkill.Builder()
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
