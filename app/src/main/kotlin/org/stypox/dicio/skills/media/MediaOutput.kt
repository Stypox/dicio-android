package org.stypox.dicio.skills.media

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillOutput
import org.stypox.dicio.R
import org.stypox.dicio.sentences.Sentences.Media
import org.stypox.dicio.util.getString

class MediaOutput(
    private val command: Media
) : SkillOutput {
    override fun getSpeechOutput(ctx: SkillContext): String = when (command) {
        is Media.Play -> ctx.getString(R.string.skill_media_playing)
        is Media.Pause -> ctx.getString(R.string.skill_media_pausing)
        is Media.Previous -> ctx.getString(R.string.skill_media_previous)
        is Media.Next -> ctx.getString(R.string.skill_media_next)
    }

    @Composable
    override fun GraphicalOutput(ctx: SkillContext) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = getSpeechOutput(ctx),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
