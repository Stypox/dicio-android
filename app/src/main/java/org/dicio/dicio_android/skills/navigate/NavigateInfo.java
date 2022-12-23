package org.dicio.dicio_android.skills.navigate;

import static org.dicio.dicio_android.SectionsGenerated.navigate;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.Sections;
import org.dicio.skill.Skill;
import org.dicio.skill.SkillContext;
import org.dicio.skill.SkillInfo;
import org.dicio.skill.chain.ChainSkill;
import org.dicio.skill.standard.StandardRecognizer;

public class NavigateInfo extends SkillInfo {
    public NavigateInfo() {
        super("navigate", R.string.skill_name_navigate,
                R.string.skill_sentence_example_navigate, R.drawable.ic_navigate_white,
                false);
    }

    @Override
    public boolean isAvailable(final SkillContext context) {
        return true;
    }

    @Override
    public Skill build(final SkillContext context) {
        return new ChainSkill.Builder()
                .recognize(new StandardRecognizer(Sections.getSection(navigate)))
                .process(new NavigateProcessor())
                .output(new NavigateOutput());
    }

    @Nullable
    @Override
    public Fragment getPreferenceFragment() {
        return null;
    }
}
