package org.stypox.dicio.skills.open

import androidx.fragment.app.Fragment
import org.dicio.skill.Skill
import org.dicio.skill.SkillContext
import org.dicio.skill.SkillInfo
import org.dicio.skill.chain.ChainSkill
import org.dicio.skill.standard.StandardRecognizer
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated.open

class OpenInfo : SkillInfo(
    "open", R.string.skill_name_open, R.string.skill_sentence_example_open,
    R.drawable.ic_open_in_new_white, false
) {
    override fun isAvailable(context: SkillContext): Boolean {
        return Sections.isSectionAvailable(open)
    }

    override fun build(context: SkillContext): Skill {
        return ChainSkill.Builder(StandardRecognizer(Sections.getSection(open)))
            .output(OpenGenerator())
    }

    override val preferenceFragment: Fragment? = null
}
