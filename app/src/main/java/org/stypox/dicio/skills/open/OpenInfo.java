package org.stypox.dicio.skills.open;

import static org.stypox.dicio.Sections.getSection;
import static org.stypox.dicio.Sections.isSectionAvailable;
import static org.stypox.dicio.SectionsGenerated.open;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import org.stypox.dicio.R;
import org.dicio.skill.Skill;
import org.dicio.skill.SkillContext;
import org.dicio.skill.SkillInfo;
import org.dicio.skill.chain.ChainSkill;
import org.dicio.skill.standard.StandardRecognizer;

public class OpenInfo extends SkillInfo {

    public OpenInfo() {
        super("open", R.string.skill_name_open, R.string.skill_sentence_example_open,
                R.drawable.ic_open_in_new_white, false);
    }

    @Override
    public boolean isAvailable(final SkillContext context) {
        return isSectionAvailable(open);
    }

    @Override
    public Skill build(final SkillContext context) {
        return new ChainSkill.Builder()
                .recognize(new StandardRecognizer(getSection(open)))
                .output(new OpenOutput());
    }

    @Nullable
    @Override
    public PreferenceFragmentCompat getPreferenceFragment() {
        return null;
    }
}
