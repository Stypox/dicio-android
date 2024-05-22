package org.stypox.dicio.skills.calculator

import androidx.fragment.app.Fragment
import org.dicio.skill.skill.Skill
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated.calculator

object CalculatorInfo : SkillInfo(
    "calculator", R.string.skill_name_calculator,
    R.string.skill_sentence_example_calculator, R.drawable.ic_calculate_white, false
) {
    override fun isAvailable(context: SkillContext): Boolean {
        return Sections.isSectionAvailable(calculator) && context.parserFormatter != null
    }

    override fun build(context: SkillContext): Skill<*> {
        return CalculatorSkill(CalculatorInfo, Sections.getSection(calculator))
    }

    override val preferenceFragment: Fragment? = null
}
