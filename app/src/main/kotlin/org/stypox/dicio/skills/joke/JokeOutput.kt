package org.stypox.dicio.skills.joke

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
import org.stypox.dicio.io.graphical.Subtitle
import org.stypox.dicio.util.getString

sealed interface JokeOutput : SkillOutput {
    data class Success(
        val setup: String,
        val delivery: String,
    ) : JokeOutput {
        override fun getSpeechOutput(ctx: SkillContext): String = ctx.getString(
            R.string.skill_joke_success, setup, delivery
        )

        @Composable
        override fun GraphicalOutput(ctx: SkillContext) {
            Column {
                Subtitle(text = setup)
                Spacer(modifier = Modifier.height(12.dp))
                Body(text = delivery)
            }
        }
    }
}