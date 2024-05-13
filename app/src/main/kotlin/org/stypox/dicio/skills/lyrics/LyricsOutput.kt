package org.stypox.dicio.skills.lyrics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dicio.skill.SkillContext
import org.dicio.skill.output.SkillOutput
import org.stypox.dicio.R
import org.stypox.dicio.output.graphical.Body
import org.stypox.dicio.output.graphical.Headline
import org.stypox.dicio.output.graphical.Subtitle
import org.stypox.dicio.util.getString

class LyricsOutput(
    private val data: LyricsGenerator.Data
) : SkillOutput {
    override fun getSpeechOutput(ctx: SkillContext): String = when (data) {
        is LyricsGenerator.Data.Success -> ctx.getString(
            R.string.skill_lyrics_found_song_by_artist, data.title, data.artist
        )
        is LyricsGenerator.Data.Failed -> ctx.getString(
            R.string.skill_lyrics_song_not_found, data.title
        )
    }

    @Composable
    override fun GraphicalOutput(ctx: SkillContext) {
        when (data) {
            is LyricsGenerator.Data.Success -> Column {
                Headline(text = data.title)
                Subtitle(text = data.artist)
                Spacer(modifier = Modifier.height(12.dp))
                Body(text = data.lyrics)
            }
            is LyricsGenerator.Data.Failed -> Headline(text = getSpeechOutput(ctx))
        }
    }
}
