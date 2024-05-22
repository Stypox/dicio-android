package org.stypox.dicio.skills.timer

import androidx.fragment.app.Fragment
import org.dicio.skill.skill.Skill
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated.timer
import org.stypox.dicio.SectionsGenerated.util_yes_no

object TimerInfo : SkillInfo(
    "timer", R.string.skill_name_timer, R.string.skill_sentence_example_timer,
    R.drawable.ic_timer_white, false
) {
    override fun isAvailable(context: SkillContext): Boolean {
        return Sections.isSectionAvailable(timer)
                && Sections.isSectionAvailable(util_yes_no)
                && context.parserFormatter != null
    }

    override fun build(context: SkillContext): Skill<*> {
        return TimerSkill(TimerInfo, Sections.getSection(timer))
    }

    override val preferenceFragment: Fragment? = null
}
