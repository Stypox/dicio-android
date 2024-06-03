package org.stypox.dicio.skills.lyrics

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated.lyrics
import org.stypox.dicio.sentences.Sentences

object LyricsInfo : SkillInfo("lyrics") {
    override fun name(context: Context) =
        context.getString(R.string.skill_name_lyrics)

    override fun sentenceExample(context: Context) =
        context.getString(R.string.skill_sentence_example_lyrics)

    @Composable
    override fun icon() =
        rememberVectorPainter(Icons.Default.MusicNote)

    override fun isAvailable(ctx: SkillContext): Boolean {
        return Sentences.Lyrics[ctx.locale.language] != null
    }

    override fun build(ctx: SkillContext): Skill<*> {
        return LyricsSkill(LyricsInfo, Sentences.Lyrics[ctx.locale.language]!!)
    }
}
