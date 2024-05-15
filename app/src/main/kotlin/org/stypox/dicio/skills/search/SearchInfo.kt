package org.stypox.dicio.skills.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import org.dicio.skill.Skill
import org.dicio.skill.SkillContext
import org.dicio.skill.SkillInfo
import org.dicio.skill.chain.ChainSkill
import org.dicio.skill.standard.StandardRecognizer
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

    override fun build(context: SkillContext): Skill {
        val builder: ChainSkill.Builder = ChainSkill.Builder(
            SearchInfo,
            StandardRecognizer(Sections.getSection(search))
        )

        // Qwant was once available as a second search engine; restore this if adding a new engine
        /*final String searchEngine = context.getPreferences().getString(
                ctx().getString(R.string.pref_key_search_engine), "");
        if (searchEngine.equals(ctx()
                .getString(R.string.pref_val_search_engine_duckduckgo))) {
        }*/

        builder.process(DuckDuckGoProcessor())
        return builder.output(SearchGenerator())
    }

    override val preferenceFragment: Fragment
        get() = Preferences()

    class Preferences : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_search)
        }
    }
}
