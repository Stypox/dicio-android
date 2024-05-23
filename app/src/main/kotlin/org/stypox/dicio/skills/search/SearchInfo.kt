package org.stypox.dicio.skills.search

import android.content.Context
import android.os.Bundle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import org.dicio.skill.skill.Skill
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated.search

object SearchInfo : SkillInfo("search") {
    override fun name(context: Context) =
        context.getString(R.string.skill_name_search)

    override fun sentenceExample(context: Context) =
        context.getString(R.string.skill_sentence_example_search)

    @Composable
    override fun icon() =
        rememberVectorPainter(Icons.Default.Search)

    override fun isAvailable(ctx: SkillContext): Boolean {
        return Sections.isSectionAvailable(search)
    }

    override fun build(ctx: SkillContext): Skill<*> {
        return SearchSkill(SearchInfo, Sections.getSection(search))
    }
}
