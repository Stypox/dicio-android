package org.stypox.dicio.skills.calculator

import androidx.fragment.app.Fragment
import org.dicio.skill.Skill
import org.dicio.skill.SkillContext
import org.dicio.skill.SkillInfo
import org.dicio.skill.chain.ChainSkill
import org.dicio.skill.standard.StandardRecognizer
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

    override fun build(context: SkillContext): Skill {
        return ChainSkill.Builder(StandardRecognizer(Sections.getSection(calculator)))
            .process(CalculatorProcessor())
            .output(CalculatorGenerator())
    }

    override val preferenceFragment: Fragment? = null
}
