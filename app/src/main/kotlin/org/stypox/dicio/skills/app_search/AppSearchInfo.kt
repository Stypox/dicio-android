package org.stypox.dicio.skills.app_search

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.sentences.Sentences

object AppSearchInfo : SkillInfo("app_search") {
    override fun name(context: Context) =
        context.getString(R.string.skill_name_app_search)

    override fun sentenceExample(context: Context) =
        context.getString(R.string.skill_sentence_example_app_search)

    @Composable
    override fun icon() =
        rememberVectorPainter(Icons.Filled.Search)

    override fun isAvailable(ctx: SkillContext): Boolean {
        return Sentences.AppSearch[ctx.sentencesLanguage] != null
    }

    override fun build(ctx: SkillContext): Skill<*> {
        return AppSearchSkill(AppSearchInfo, Sentences.AppSearch[ctx.sentencesLanguage]!!)
    }
}
