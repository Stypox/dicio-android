package org.stypox.dicio.skills.fallback.text

import androidx.preference.PreferenceFragmentCompat
import org.dicio.skill.skill.Skill
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R

object TextFallbackInfo :
    SkillInfo("text", R.string.skill_fallback_name_text, 0, R.drawable.ic_short_text_white, false) {
    override fun isAvailable(context: SkillContext): Boolean {
        return true
    }

    override fun build(context: SkillContext): Skill<*> {
        return TextFallbackSkill(TextFallbackInfo)
    }

    override val preferenceFragment: PreferenceFragmentCompat? = null
}
