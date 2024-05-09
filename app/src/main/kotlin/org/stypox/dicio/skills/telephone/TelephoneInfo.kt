package org.stypox.dicio.skills.telephone

import android.Manifest
import androidx.fragment.app.Fragment
import org.dicio.skill.Skill
import org.dicio.skill.SkillContext
import org.dicio.skill.SkillInfo
import org.dicio.skill.chain.ChainSkill
import org.dicio.skill.standard.StandardRecognizer
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated.telephone
import org.stypox.dicio.SectionsGenerated.util_yes_no

class TelephoneInfo : SkillInfo(
    "open", R.string.skill_name_telephone, R.string.skill_sentence_example_telephone,
    R.drawable.ic_call_white, false
) {
    override fun isAvailable(context: SkillContext): Boolean {
        return Sections.isSectionAvailable(telephone) && Sections.isSectionAvailable(util_yes_no)
    }

    override fun build(context: SkillContext): Skill {
        return ChainSkill.Builder(StandardRecognizer(Sections.getSection(telephone)))
            .output(TelephoneOutput())
    }

    override val preferenceFragment: Fragment? = null

    override val neededPermissions: List<String>
        = listOf(Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE)
}
