package org.stypox.dicio.skills.open

import androidx.fragment.app.Fragment
import org.dicio.skill.skill.Skill
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated.open

object OpenInfo : SkillInfo(
    "open", R.string.skill_name_open, R.string.skill_sentence_example_open,
    R.drawable.ic_open_in_new_white, false
) {
    override fun isAvailable(context: SkillContext): Boolean {
        return Sections.isSectionAvailable(open)
    }

    override fun build(context: SkillContext): Skill<*> {
        return OpenSkill(OpenInfo, Sections.getSection(open))
    }

    override val preferenceFragment: Fragment? = null
}
