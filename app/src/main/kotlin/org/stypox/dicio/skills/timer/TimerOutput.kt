package org.stypox.dicio.skills.timer

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LongState
import org.dicio.numbers.ParserFormatter
import org.dicio.skill.Skill
import org.dicio.skill.SkillContext
import org.dicio.skill.chain.ChainSkill
import org.dicio.skill.chain.InputRecognizer
import org.dicio.skill.chain.OutputGenerator
import org.dicio.skill.output.SkillOutput
import org.dicio.skill.standard.StandardRecognizer
import org.dicio.skill.standard.StandardResult
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated
import org.stypox.dicio.io.graphical.Headline
import org.stypox.dicio.io.graphical.HeadlineSpeechSkillOutput
import org.stypox.dicio.skills.fallback.text.TextFallbackOutput
import org.stypox.dicio.util.getString
import java.text.DecimalFormatSymbols
import java.time.Duration
import kotlin.math.absoluteValue

sealed class TimerOutput : SkillOutput {
    class Set(
        private val milliseconds: Long,
        private val lastTickMillis: LongState,
        private val name: String?
    ) : TimerOutput() {
        override fun getSpeechOutput(ctx: SkillContext): String = formatStringWithName(
            ctx, name, milliseconds, R.string.skill_timer_set, R.string.skill_timer_set_name
        )

        @Composable
        override fun GraphicalOutput(ctx: SkillContext) {
            Headline(
                text = getFormattedDuration(
                    ctx.parserFormatter!!,
                    lastTickMillis.longValue,
                    false,
                ),
            )
        }
    }

    class SetAskDuration(
        private val onGotDuration: (Duration) -> SkillOutput,
    ) : TimerOutput(), HeadlineSpeechSkillOutput {
        override fun getSpeechOutput(ctx: SkillContext): String =
            ctx.getString(R.string.skill_timer_how_much_time)

        override fun getNextSkills(ctx: SkillContext): List<Skill> = listOf(object : Skill(
            TimerInfo,
            InputRecognizer.Specificity.HIGH,
        ) {
            private var input: String? = null
            private var duration: Duration? = null

            override fun setInput(
                input: String,
                inputWords: List<String>,
                normalizedWordKeys: List<String>
            ) {
                this.input = input
            }

            override fun score(): Float {
                duration = ctx().parserFormatter!!
                    .extractDuration(input!!)
                    .first
                    ?.toJavaDuration()
                return if (duration == null) 0.0f else 1.0f
            }

            override fun processInput() {}
            override fun generateOutput(): SkillOutput {
                return duration?.let { onGotDuration(it) }
                    ?: TextFallbackOutput()
            }

            override fun cleanup() {
                super.cleanup()
                input = null
                duration = null
            }
        })
    }

    class Cancel(
        private val speechOutput: String,
    ) : TimerOutput(), HeadlineSpeechSkillOutput {
        override fun getSpeechOutput(ctx: SkillContext): String = speechOutput
    }

    class ConfirmCancel(
        private val onConfirm: () -> SkillOutput,
    ) : TimerOutput(), HeadlineSpeechSkillOutput {
        override fun getSpeechOutput(ctx: SkillContext): String =
            ctx.getString(R.string.skill_timer_confirm_cancel)

        override fun getNextSkills(ctx: SkillContext): List<Skill> = listOf(
            ChainSkill.Builder(
                TimerInfo,
                StandardRecognizer(Sections.getSection(SectionsGenerated.util_yes_no))
            ).output(object : OutputGenerator<StandardResult>() {
                override fun generate(data: StandardResult): SkillOutput {
                    if ("yes" == data.sentenceId) {
                        return onConfirm()
                    }

                    return Cancel(ctx().getString(R.string.skill_timer_none_canceled))
                }
            })
        )
    }

    class Query(
        private val speechOutput: String,
    ) : TimerOutput(), HeadlineSpeechSkillOutput {
        override fun getSpeechOutput(ctx: SkillContext): String = speechOutput
    }
}

fun formatStringWithName(
    ctx: SkillContext,
    name: String?,
    milliseconds: Long,
    @StringRes stringWithoutName: Int,
    @StringRes stringWithName: Int
): String {
    val duration = getFormattedDuration(ctx.parserFormatter!!, milliseconds, true)
    return if (name == null) {
        ctx.getString(stringWithoutName, duration)
    } else {
        ctx.getString(stringWithName, name, duration)
    }
}

fun formatStringWithName(
    context: Context,
    name: String?,
    @StringRes stringWithoutName: Int,
    @StringRes stringWithName: Int
): String {
    return if (name == null) {
        context.getString(stringWithoutName)
    } else {
        context.getString(stringWithName, name)
    }
}

fun getFormattedDuration(
    parserFormatter: ParserFormatter,
    milliseconds: Long,
    speech: Boolean
): String {
    val niceDuration = parserFormatter
        .niceDuration(org.dicio.numbers.unit.Duration(Duration.ofMillis(milliseconds.absoluteValue)))
        .speech(speech)
        .get()

    return if (speech) {
        niceDuration // no need to speak milliseconds
    } else {
        (if (milliseconds < 0) "-" else "") +
                niceDuration +
                DecimalFormatSymbols.getInstance().decimalSeparator +
                // show only the first decimal place, rounded to the nearest one
                ((milliseconds.absoluteValue + 50) / 100) % 10
    }
}
