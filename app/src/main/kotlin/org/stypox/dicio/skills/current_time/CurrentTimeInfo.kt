package org.stypox.dicio.skills.current_time

import androidx.fragment.app.Fragment
import org.dicio.skill.skill.Skill
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated.current_time

object CurrentTimeInfo : SkillInfo(
    "current_time", R.string.skill_name_current_time,
    R.string.skill_sentence_example_current_time, R.drawable.ic_watch_white, false
) {
    override fun isAvailable(context: SkillContext): Boolean {
        return Sections.isSectionAvailable(current_time)
    }

    override fun build(context: SkillContext): Skill<*> {
        return CurrentTimeSkill(CurrentTimeInfo, Sections.getSection(current_time))
    }

    override val preferenceFragment: Fragment? = null
}
