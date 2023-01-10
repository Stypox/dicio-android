package org.dicio.dicio_android.skills.navigation;

import static org.dicio.dicio_android.SectionsGenerated.navigation;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.Sections;
import org.dicio.skill.Skill;
import org.dicio.skill.SkillContext;
import org.dicio.skill.SkillInfo;
import org.dicio.skill.chain.ChainSkill;
import org.dicio.skill.standard.StandardRecognizer;

public class NavigationInfo extends SkillInfo {
    public NavigationInfo() {
        super("navigation", R.string.skill_name_navigation,
                R.string.skill_sentence_example_navigation, R.drawable.ic_navigate_white,
                false);
    }

    @Override
    public boolean isAvailable(final SkillContext context) {
        return Sections.isSectionAvailable(navigation);
    }

    @Override
    public Skill build(final SkillContext context) {
        return new ChainSkill.Builder()
                .recognize(new StandardRecognizer(Sections.getSection(navigation)))
                .process(new NavigationProcessor())
                .output(new NavigationOutput());
    }

    @Nullable
    @Override
    public Fragment getPreferenceFragment() {
        return null;
    }
}
