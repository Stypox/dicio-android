package org.stypox.dicio.skills.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillOutput
import org.stypox.dicio.io.graphical.Body
import org.stypox.dicio.io.graphical.Headline
import org.stypox.dicio.io.graphical.HeadlineSpeechSkillOutput
import java.time.LocalDateTime

sealed interface CalendarOutput : SkillOutput {
    data class Success(
        val whenString: String,
        val whenDateTime: LocalDateTime,
        val whenBackToString: String,
    ) : CalendarOutput {
        override fun getSpeechOutput(ctx: SkillContext): String =
            "Understood!"

        @Composable
        override fun GraphicalOutput(ctx: SkillContext) {
            Column {
                Headline(getSpeechOutput(ctx))
                Body("I parsed: \"$whenString\",\nI understood: $whenDateTime,\nBack to string: \"$whenBackToString\"")
            }
        }
    }

    data class FailedStringToDateTime(
        val whenString: String?
    ) : CalendarOutput, HeadlineSpeechSkillOutput {
        override fun getSpeechOutput(ctx: SkillContext): String =
            "Could not understand date/time: \"$whenString\""
    }

    data class FailedDateTimeToString(
        val whenDateTime: LocalDateTime
    ) : CalendarOutput, HeadlineSpeechSkillOutput {
        override fun getSpeechOutput(ctx: SkillContext): String =
            "Could not turn date/time back to string: \"$whenDateTime\""
    }
}
