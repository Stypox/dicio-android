package org.stypox.dicio.skills.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import org.dicio.skill.skill.Skill
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated.search

object SearchInfo : SkillInfo(
    "search", R.string.skill_name_search, R.string.skill_sentence_example_search,
    R.drawable.ic_search_white, true
) {
    override fun isAvailable(context: SkillContext): Boolean {
        return Sections.isSectionAvailable(search)
    }

    override fun build(context: SkillContext): Skill<*> {
        return SearchSkill(SearchInfo, Sections.getSection(search))
    }

    override val preferenceFragment: Fragment
        get() = Preferences()

    class Preferences : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_search)
        }
    }
}
