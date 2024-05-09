package org.stypox.dicio.skills.navigation

import androidx.fragment.app.Fragment
import org.dicio.skill.Skill
import org.dicio.skill.SkillContext
import org.dicio.skill.SkillInfo
import org.dicio.skill.chain.ChainSkill
import org.dicio.skill.standard.StandardRecognizer
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated.navigation

class NavigationInfo : SkillInfo(
    "navigation", R.string.skill_name_navigation,
    R.string.skill_sentence_example_navigation, R.drawable.ic_navigate_white,
    false
) {
    override fun isAvailable(context: SkillContext): Boolean {
        return Sections.isSectionAvailable(navigation)
    }

    override fun build(context: SkillContext): Skill {
        return ChainSkill.Builder(StandardRecognizer(Sections.getSection(navigation)))
            .process(NavigationProcessor())
            .output(NavigationOutput())
    }

    override val preferenceFragment: Fragment? = null
}
