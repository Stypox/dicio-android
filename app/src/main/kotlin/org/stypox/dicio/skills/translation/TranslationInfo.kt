package org.stypox.dicio.skills.translation

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.sentences.Sentences
import org.stypox.dicio.util.LocaleUtils

object TranslationInfo : SkillInfo("translation") {
    override fun name(context: Context) =
        context.getString(R.string.skill_name_translation)

    override fun sentenceExample(context: Context) =
        context.getString(R.string.skill_sentence_example_translation)

    @Composable
    override fun icon() =
        rememberVectorPainter(Icons.Default.Language)

    override fun isAvailable(ctx: SkillContext): Boolean {
        val hasSupportedLocale = LocaleUtils.isLanguageSupported(
            ctx, TranslationSkill.TRANSLATE_SUPPORTED_LOCALES
        )
        return (Sentences.Translation[ctx.sentencesLanguage] != null) && hasSupportedLocale
    }

    override fun build(ctx: SkillContext): Skill<*> {
        return TranslationSkill(TranslationInfo, Sentences.Translation[ctx.sentencesLanguage]!!)
    }
}
