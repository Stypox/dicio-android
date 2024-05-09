package org.stypox.dicio.skills.timer

import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.CountDownTimer
import androidx.annotation.StringRes
import org.dicio.skill.Skill
import org.dicio.skill.chain.ChainSkill
import org.dicio.skill.chain.InputRecognizer
import org.dicio.skill.chain.OutputGenerator
import org.dicio.skill.standard.StandardRecognizer
import org.dicio.skill.standard.StandardResult
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated.util_yes_no
import org.stypox.dicio.output.graphical.GraphicalOutputUtils
import org.stypox.dicio.util.StringUtils
import java.text.DecimalFormatSymbols
import java.time.Duration
import kotlin.math.absoluteValue
import org.dicio.numbers.unit.Duration as DicioNumbersDuration

// TODO cleanup this skill and use a service to manage timers
class TimerOutput : OutputGenerator<TimerOutput.Data>() {
    enum class Action {
        SET, CANCEL, QUERY // TODO add action to stop playing ringtone, if one is playing
    }

    class Data(val action: Action, val duration: Duration?, val name: String?)

    private class SetTimer(
        duration: Duration,
        name: String?,
        onMillisTickCallback: (Long) -> Unit,
        onSecondsTickCallback: (Long) -> Unit,
        onExpiredCallback: (String?) -> Unit,
        val onCancelCallback: () -> Unit
    ) {
        val name: String? = name?.trim { it <= ' ' }
        var lastTickMillis: Long = duration.toMillis()

        val countDownTimer = object : CountDownTimer(
            lastTickMillis + RINGTONE_DURATION_MILLIS,
            TIMER_TICK_MILLIS
        ) {
            override fun onTick(millisUntilFinished: Long) {
                val prevLastTickMillis = lastTickMillis
                lastTickMillis = millisUntilFinished - RINGTONE_DURATION_MILLIS

                if (prevLastTickMillis >= 0) {
                    // round up to the nearest second
                    val prevLastTickSeconds = (prevLastTickMillis + 999) / 1000
                    val lastTickSeconds = (lastTickMillis + 999) / 1000

                    if (prevLastTickSeconds != lastTickSeconds) {
                        onSecondsTickCallback(lastTickSeconds)
                    }
                    if (lastTickMillis < 0) {
                        onExpiredCallback(name)
                    }
                }

                onMillisTickCallback(lastTickMillis)
            }

            override fun onFinish() {
                onMillisTickCallback(-RINGTONE_DURATION_MILLIS)
                onCancelCallback()
                // removed from setTimers list automatically here
                SET_TIMERS.removeIf { setTimer -> setTimer === this@SetTimer }
            }
        }.apply { start() }

        fun cancel() {
            // removed from setTimers list by caller
            countDownTimer.cancel()
            onCancelCallback()
        }
    }

    override fun generate(data: Data) {
        when (data.action) {
            Action.SET -> {
                data.duration?.also { setTimer(it, data.name) } ?: run {
                    val message: String = ctx().android!!
                        .getString(R.string.skill_timer_how_much_time)
                    ctx().speechOutputDevice!!.speak(message)
                    ctx().graphicalOutputDevice!!.display(
                        GraphicalOutputUtils.buildSubHeader(ctx().android!!, message)
                    )
                    setNextSkills(listOf(object : Skill() {
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
                        override fun generateOutput() {
                            duration?.also { setTimer(it, data.name) }
                        }

                        override fun cleanup() {
                            super.cleanup()
                            input = null
                            duration = null
                        }
                    }))
                }
            }
            Action.CANCEL -> {
                if (data.name == null && SET_TIMERS.size > 1) {
                    val message: String = ctx().android!!
                        .getString(R.string.skill_timer_confirm_cancel)
                    ctx().speechOutputDevice!!.speak(message)
                    ctx().graphicalOutputDevice!!.display(
                        GraphicalOutputUtils.buildSubHeader(ctx().android!!, message)
                    )
                    setNextSkills(listOf<Skill>(ChainSkill.Builder(StandardRecognizer(Sections.getSection(util_yes_no)))
                        .output(object : OutputGenerator<StandardResult>() {
                            override fun generate(data: StandardResult) {
                                if ("yes" == data.sentenceId) {
                                    cancelTimer(null)
                                    return
                                }

                                val cancelMessage: String = ctx().android!!
                                    .getString(R.string.skill_timer_none_canceled)
                                ctx().speechOutputDevice!!.speak(cancelMessage)
                                ctx().graphicalOutputDevice!!.display(
                                    GraphicalOutputUtils.buildSubHeader(
                                        ctx().android!!,
                                        cancelMessage
                                    )
                                )
                            }
                        }))
                    )
                    return
                }
                cancelTimer(data.name)
            }
            Action.QUERY -> queryTimer(data.name)
        }
    }

    private fun setTimer(
        duration: Duration,
        name: String?
    ) {
        ctx().speechOutputDevice!!.speak(
            formatStringWithName(
                name, duration.toMillis(),
                R.string.skill_timer_set, R.string.skill_timer_set_name
            )
        )
        val textView = GraphicalOutputUtils.buildSubHeader(
            ctx().android!!,
            getFormattedDuration(duration.toMillis(), false)
        )
        ctx().graphicalOutputDevice!!.display(textView)

        var ringtone: Ringtone? = null

        SET_TIMERS.add(SetTimer(duration, name,
            onMillisTickCallback = { milliseconds ->
                textView.text = getFormattedDuration(milliseconds, false)
                if (milliseconds < 0 && ringtone?.isPlaying == false) {
                    ringtone?.play()
                }
            },
            onSecondsTickCallback = { seconds ->
                if (seconds <= 5) {
                    ctx().speechOutputDevice!!.speak(
                        ctx()
                            .parserFormatter!!.pronounceNumber(seconds.toDouble())
                            .get()
                    )
                }
            },
            onExpiredCallback = { theName ->
                // initialize ringtone when the timer has expired (play will be called right after)
                ringtone = RingtoneManager.getActualDefaultRingtoneUri(
                    ctx().android!!, RingtoneManager.TYPE_ALARM
                )
                    ?.let {
                        RingtoneManager.getRingtone(ctx().android!!, it)
                    }
                    ?.also {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            // on older API versions it is looped manually in onMillisTickCallback
                            it.isLooping = true
                        }
                        it.play()
                    }

                if (ringtone == null) {
                    // we could not load a ringtone, so we can announce via speech instead
                    ctx().speechOutputDevice!!.speak(
                        formatStringWithName(
                            theName,
                            R.string.skill_timer_expired,
                            R.string.skill_timer_expired_name
                        )
                    )
                }
            },
            onCancelCallback = {
                ringtone?.stop()
                ringtone = null
                // keep the previous message
            }
        ))
    }

    private fun cancelTimer(name: String?) {
        val message: String
        if (SET_TIMERS.isEmpty()) {
            message = ctx().android!!
                .getString(R.string.skill_timer_no_active)
        } else if (name == null) {
            message = if (SET_TIMERS.size == 1) {
                formatStringWithName(
                    SET_TIMERS[0].name,
                    R.string.skill_timer_canceled,
                    R.string.skill_timer_canceled_name
                )
            } else {
                ctx().android!!.getString(R.string.skill_timer_all_canceled)
            }

            // cancel all
            for (setTimer in SET_TIMERS) {
                setTimer.cancel()
            }
            SET_TIMERS.clear()
        } else {
            val setTimer = getSetTimerWithSimilarName(name)
            if (setTimer == null) {
                message = ctx().android!!
                    .getString(R.string.skill_timer_no_active_name, name)
            } else {
                message = ctx().android!!
                    .getString(R.string.skill_timer_canceled_name, setTimer.name)
                setTimer.cancel()
                SET_TIMERS.remove(setTimer)
            }
        }
        ctx().speechOutputDevice!!.speak(message)
        ctx().graphicalOutputDevice!!.display(
            GraphicalOutputUtils.buildSubHeader(ctx().android!!, message)
        )
    }

    private fun queryTimer(name: String?) {
        val message = if (SET_TIMERS.isEmpty()) {
            ctx().android!!
                .getString(R.string.skill_timer_no_active)
        } else if (name == null) {
            // no name provided by the user: query the last timer, but adapt the message if only one
            val lastTimer = SET_TIMERS[SET_TIMERS.size - 1]
            @StringRes val noNameQueryString: Int = if (SET_TIMERS.size == 1)
                R.string.skill_timer_query
            else
                R.string.skill_timer_query_last

            formatStringWithName(
                lastTimer.name, lastTimer.lastTickMillis,
                noNameQueryString, R.string.skill_timer_query_name
            )

        } else {
            val setTimer = getSetTimerWithSimilarName(name)
            if (setTimer == null) {
                ctx().android!!.getString(R.string.skill_timer_no_active_name, name)
            } else {
                ctx().android!!.getString(
                    R.string.skill_timer_query_name, setTimer.name,
                    getFormattedDuration(setTimer.lastTickMillis, true)
                )
            }
        }
        ctx().speechOutputDevice!!.speak(message)
        ctx().graphicalOutputDevice!!.display(
            GraphicalOutputUtils.buildSubHeader(ctx().android!!, message)
        )
    }

    private fun getFormattedDuration(
        milliseconds: Long,
        speech: Boolean
    ): String {
        val niceDuration = ctx().parserFormatter!!
            .niceDuration(DicioNumbersDuration(Duration.ofMillis(milliseconds.absoluteValue)))
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

    private fun formatStringWithName(
        name: String?,
        @StringRes stringWithoutName: Int,
        @StringRes stringWithName: Int
    ): String {
        return if (name == null) {
            ctx().android!!.getString(stringWithoutName)
        } else {
            ctx().android!!.getString(stringWithName, name)
        }
    }

    private fun formatStringWithName(
        name: String?,
        seconds: Long,
        @StringRes stringWithoutName: Int,
        @StringRes stringWithName: Int
    ): String {
        val duration = getFormattedDuration(seconds, true)
        return if (name == null) {
            ctx().android!!.getString(stringWithoutName, duration)
        } else {
            ctx().android!!.getString(stringWithName, name, duration)
        }
    }

    private fun getSetTimerWithSimilarName(name: String): SetTimer? {
        class Pair(val setTimer: SetTimer, val distance: Int)
        return SET_TIMERS
            .mapNotNull { setTimer: SetTimer ->
                setTimer.name?.let { timerName ->
                    Pair(
                        setTimer,
                        StringUtils.customStringDistance(name, timerName)
                    )
                }
            }
            .filter { pair -> pair.distance < 6 }
            .minByOrNull { pair -> pair.distance }
            ?.setTimer
    }

    companion object {
        private val SET_TIMERS: MutableList<SetTimer> = ArrayList()
        private const val TIMER_TICK_MILLIS = 100L // milliseconds
        private const val RINGTONE_DURATION_MILLIS = 30000L // 30 seconds of ringtone
    }
}
