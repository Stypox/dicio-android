package org.stypox.dicio.skills.lyrics

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.dicio.skill.output.SkillOutput
import org.stypox.dicio.R
import org.stypox.dicio.output.graphical.Body
import org.stypox.dicio.output.graphical.Headline
import org.stypox.dicio.output.graphical.Subtitle

class LyricsOutput(
    context: Context,
    private val data: LyricsGenerator.Data
) : SkillOutput {
    override val speechOutput = when (data) {
        is LyricsGenerator.Data.Success -> context.getString(
            R.string.skill_lyrics_found_song_by_artist, data.title, data.artist
        )
        is LyricsGenerator.Data.Failed -> context.getString(
            R.string.skill_lyrics_song_not_found, data.title
        )
    }

    @Composable
    override fun GraphicalOutput() {
        when (data) {
            is LyricsGenerator.Data.Success -> Column {
                Headline(text = data.title)
                Subtitle(text = data.artist)
                Spacer(modifier = Modifier.height(12.dp))
                Body(text = data.lyrics)
            }
            is LyricsGenerator.Data.Failed -> Headline(text = speechOutput)
        }
    }
}
