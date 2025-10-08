package org.stypox.dicio.skills.joke

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.sentences.Sentences
import org.stypox.dicio.util.LocaleUtils

object JokeInfo : SkillInfo("Joke") {
    override fun name(context: Context) =
        context.getString(R.string.skill_name_joke)

    override fun sentenceExample(context: Context) =
        context.getString(R.string.skill_sentence_example_joke)

    @Composable
    override fun icon() =
        rememberVectorPainter(Icons.Default.EmojiEmotions)

    override fun isAvailable(ctx: SkillContext): Boolean {
        val hasSupportedLocale = LocaleUtils.isLanguageSupported(
            ctx, JokeSkill.JOKE_SUPPORTED_LOCALES
        )
       return (Sentences.Joke[ctx.sentencesLanguage] != null) && hasSupportedLocale
    }

    override fun build(ctx: SkillContext): Skill<*> {
        return JokeSkill(JokeInfo, Sentences.Joke[ctx.sentencesLanguage]!!)
    }
}
