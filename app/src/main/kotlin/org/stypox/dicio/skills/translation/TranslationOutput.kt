package org.stypox.dicio.skills.translation

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillOutput
import org.stypox.dicio.R
import org.stypox.dicio.io.graphical.Headline
import org.stypox.dicio.io.graphical.HeadlineSpeechSkillOutput
import org.stypox.dicio.util.getString

sealed interface TranslationOutput : SkillOutput {
    data class Success(
        val translation: String,
        val query: String,
        val target: String,
    ): TranslationOutput {
        override fun getSpeechOutput(ctx: SkillContext): String = ctx.getString(
            R.string.skill_translation_success, query, target
        )

        @Composable
        override fun GraphicalOutput(ctx: SkillContext) {
            Column {
                Headline(text = translation)
            }
        }
    }

    data class Failed(
        val target: String,
    ): HeadlineSpeechSkillOutput, TranslationOutput {
        override fun getSpeechOutput(ctx: SkillContext): String = ctx.getString(
            R.string.skill_translation_failed, target
        )
    }

    object EmptyQuery: HeadlineSpeechSkillOutput, TranslationOutput {
        override fun getSpeechOutput(ctx: SkillContext): String = ctx.getString(
            R.string.skill_translation_failed_no_query
        )
    }
}
