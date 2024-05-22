package org.stypox.dicio.skills.navigation

import androidx.fragment.app.Fragment
import org.dicio.skill.skill.Skill
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated.navigation

object NavigationInfo : SkillInfo(
    "navigation", R.string.skill_name_navigation,
    R.string.skill_sentence_example_navigation, R.drawable.ic_navigate_white,
    false
) {
    override fun isAvailable(context: SkillContext): Boolean {
        return Sections.isSectionAvailable(navigation)
    }

    override fun build(context: SkillContext): Skill<*> {
        return NavigationSkill(NavigationInfo, Sections.getSection(navigation))
    }

    override val preferenceFragment: Fragment? = null
}
