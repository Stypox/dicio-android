package org.stypox.dicio.skills.timer

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LongState
import org.dicio.numbers.ParserFormatter
import org.dicio.skill.Skill
import org.dicio.skill.chain.ChainSkill
import org.dicio.skill.chain.InputRecognizer
import org.dicio.skill.chain.OutputGenerator
import org.dicio.skill.output.SkillOutput
import org.dicio.skill.standard.StandardRecognizer
import org.dicio.skill.standard.StandardResult
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated
import org.stypox.dicio.output.graphical.Headline
import org.stypox.dicio.skills.fallback.text.TextFallbackOutput
import java.text.DecimalFormatSymbols
import java.time.Duration
import kotlin.math.absoluteValue

sealed class TimerOutput : SkillOutput {
    class Set(
        context: Context,
        private val parserFormatter: ParserFormatter,
        milliseconds: Long,
        private val lastTickMillis: LongState,
        name: String?
    ) : TimerOutput() {
        override val speechOutput = formatStringWithName(context, parserFormatter,
            name, milliseconds, R.string.skill_timer_set, R.string.skill_timer_set_name)

        @Composable
        override fun GraphicalOutput() {
            Headline(
                text = getFormattedDuration(
                    parserFormatter,
                    lastTickMillis.longValue,
                    false,
                ),
            )
        }
    }

    class SetAskDuration(
        context: Context,
        val onGotDuration: (Duration) -> SkillOutput,
    ) : TimerOutput() {
        override val speechOutput = context.getString(R.string.skill_timer_how_much_time)

        override val nextSkills = listOf<Skill>(object : Skill() {
            private var input: String? = null
            private var duration: Duration? = null
            override fun specificity(): InputRecognizer.Specificity {
                return InputRecognizer.Specificity.HIGH
            }

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
                    ?: TextFallbackOutput(ctx().android!!)
            }

            override fun cleanup() {
                super.cleanup()
                input = null
                duration = null
            }
        })

        @Composable
        override fun GraphicalOutput() {
            Headline(text = speechOutput)
        }
    }

    class Cancel(
        override val speechOutput: String,
    ) : TimerOutput() {
        @Composable
        override fun GraphicalOutput() {
            Headline(text = speechOutput)
        }
    }

    class ConfirmCancel(
        context: Context,
        val onConfirm: () -> SkillOutput,
    ) : TimerOutput() {
        override val speechOutput = context.getString(R.string.skill_timer_confirm_cancel)

        override val nextSkills = listOf(
            ChainSkill.Builder(
                StandardRecognizer(Sections.getSection(SectionsGenerated.util_yes_no))
            ).output(object : OutputGenerator<StandardResult>() {
                override fun generate(data: StandardResult): SkillOutput {
                    if ("yes" == data.sentenceId) {
                        return onConfirm()
                    }

                    return Cancel(ctx().android!!.getString(R.string.skill_timer_none_canceled))
                }
            })
        )

        @Composable
        override fun GraphicalOutput() {
            Headline(text = speechOutput)
        }
    }

    class Query(
        override val speechOutput: String,
    ) : TimerOutput() {
        @Composable
        override fun GraphicalOutput() {
            Headline(text = speechOutput)
        }
    }
}

fun formatStringWithName(
    context: Context,
    parserFormatter: ParserFormatter,
    name: String?,
    milliseconds: Long,
    @StringRes stringWithoutName: Int,
    @StringRes stringWithName: Int
): String {
    val duration = getFormattedDuration(parserFormatter, milliseconds, true)
    return if (name == null) {
        context.getString(stringWithoutName, duration)
    } else {
        context.getString(stringWithName, name, duration)
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
