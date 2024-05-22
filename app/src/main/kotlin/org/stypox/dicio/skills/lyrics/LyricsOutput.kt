package org.stypox.dicio.skills.lyrics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillOutput
import org.stypox.dicio.R
import org.stypox.dicio.io.graphical.Body
import org.stypox.dicio.io.graphical.Headline
import org.stypox.dicio.io.graphical.HeadlineSpeechSkillOutput
import org.stypox.dicio.io.graphical.Subtitle
import org.stypox.dicio.util.getString

sealed class LyricsOutput : SkillOutput {
    data class Success(
        val title: String,
        val artist: String,
        val lyrics: String,
    ) : LyricsOutput() {
        override fun getSpeechOutput(ctx: SkillContext): String = ctx.getString(
            R.string.skill_lyrics_found_song_by_artist, title, artist
        )

        @Composable
        override fun GraphicalOutput(ctx: SkillContext) {
            Column {
                Headline(text = title)
                Subtitle(text = artist)
                Spacer(modifier = Modifier.height(12.dp))
                Body(text = lyrics)
            }
        }
    }

    data class Failed(
        val title: String,
    ) : HeadlineSpeechSkillOutput, LyricsOutput() {
        override fun getSpeechOutput(ctx: SkillContext): String = ctx.getString(
            R.string.skill_lyrics_song_not_found, title
        )
    }
}
