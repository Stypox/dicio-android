package org.stypox.dicio.skills.timer

import android.media.RingtoneManager
import android.net.Uri
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
import java.time.Duration
import java.util.function.BiConsumer
import java.util.function.Consumer

// TODO cleanup this skill and use a service to manage timers
class TimerOutput : OutputGenerator<TimerOutput.Data>() {
    enum class Action {
        SET, CANCEL, QUERY
    }

    class Data(val action: Action, val duration: Duration?, val name: String?)

    private class SetTimer constructor(
        duration: Duration,
        name: String?,
        onTickCallback: BiConsumer<String?, Long>,
        onFinishCallback: Consumer<String?>,
        val onCancelCallback: Consumer<String?>
    ) {
        val name: String? = name?.trim { it <= ' ' }
        var lastTickSeconds: Long = duration.seconds
        val countDownTimer: CountDownTimer = object : CountDownTimer(duration.toMillis(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                lastTickSeconds = millisUntilFinished / 1000
                onTickCallback.accept(name, lastTickSeconds)
            }

            override fun onFinish() {
                onFinishCallback.accept(name)
                // removed from setTimers list automatically here
                SET_TIMERS.removeIf { setTimer: SetTimer -> setTimer.countDownTimer === this }
            }
        }.apply { start() }

        fun cancel() {
            // removed from setTimers list by caller
            countDownTimer.cancel()
            onCancelCallback.accept(name)
        }
    }

    override fun generate(data: Data) {
        when (data.action) {
            Action.SET -> {
                data.duration?.also { setTimer(it, data.name) } ?: run {
                    val message: String = ctx().android()
                        .getString(R.string.skill_timer_how_much_time)
                    ctx().speechOutputDevice.speak(message)
                    ctx().graphicalOutputDevice.display(
                        GraphicalOutputUtils.buildSubHeader(ctx().android(), message)
                    )
                    setNextSkills(listOf(object : Skill() {
                        private var input: String? = null
                        private var duration: Duration? = null
                        override fun specificity(): InputRecognizer.Specificity {
                            return InputRecognizer.Specificity.high
                        }

                        override fun setInput(
                            input: String,
                            inputWords: List<String>,
                            normalizedWordKeys: List<String>
                        ) {
                            this.input = input
                        }

                        override fun score(): Float {
                            duration = ctx().requireNumberParserFormatter()
                                .extractDuration(input).get()
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
                    val message: String = ctx().android()
                        .getString(R.string.skill_timer_confirm_cancel)
                    ctx().speechOutputDevice.speak(message)
                    ctx().graphicalOutputDevice.display(
                        GraphicalOutputUtils.buildSubHeader(ctx().android(), message)
                    )
                    setNextSkills(listOf<Skill>(ChainSkill.Builder()
                        .recognize(StandardRecognizer(Sections.getSection(util_yes_no)))
                        .output(object : OutputGenerator<StandardResult>() {
                            override fun generate(data: StandardResult) {
                                if ("yes" == data.sentenceId) {
                                    cancelTimer(null)
                                    return
                                }

                                val cancelMessage: String = ctx().android()
                                    .getString(R.string.skill_timer_none_canceled)
                                ctx().speechOutputDevice.speak(cancelMessage)
                                ctx().graphicalOutputDevice.display(
                                    GraphicalOutputUtils.buildSubHeader(
                                        ctx().android(),
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
        ctx().speechOutputDevice.speak(
            formatStringWithName(
                name, duration.seconds,
                R.string.skill_timer_set, R.string.skill_timer_set_name
            )
        )
        val textView = GraphicalOutputUtils.buildSubHeader(
            ctx().android(),
            getFormattedDuration(duration.seconds, false)
        )
        ctx().graphicalOutputDevice.display(textView)
        SET_TIMERS.add(SetTimer(duration, name,
            { _, seconds: Long ->
                textView.text = getFormattedDuration(seconds, false)
                if (seconds <= 5) {
                    ctx().speechOutputDevice.speak(
                        ctx()
                            .requireNumberParserFormatter().pronounceNumber(seconds.toDouble())
                            .get()
                    )
                }
            },
            { theName: String? ->
                // TODO improve how alarm is played, and allow stopping it
                val message = formatStringWithName(
                    theName,
                    R.string.skill_timer_expired, R.string.skill_timer_expired_name
                )
                val ringtoneUri: Uri? = RingtoneManager.getActualDefaultRingtoneUri(
                    ctx().android(), RingtoneManager.TYPE_ALARM
                )

                val ringtone = ringtoneUri.let {
                    RingtoneManager.getRingtone(ctx().android(), ringtoneUri)
                }

                ringtone?.play() ?: run { ctx().speechOutputDevice.speak(message) }
                textView.text = message
            }
        ) { theName: String? ->
            textView.text = formatStringWithName(
                theName,
                R.string.skill_timer_canceled, R.string.skill_timer_canceled_name
            )
        })
    }

    private fun cancelTimer(name: String?) {
        val message: String
        if (SET_TIMERS.isEmpty()) {
            message = ctx().android()
                .getString(R.string.skill_timer_no_active)
        } else if (name == null) {
            message = if (SET_TIMERS.size == 1) {
                if (SET_TIMERS[0].name == null) {
                    ctx().android().getString(R.string.skill_timer_canceled)
                } else {
                    ctx().android()
                        .getString(R.string.skill_timer_canceled_name, SET_TIMERS[0].name)
                }
            } else {
                ctx().android().getString(R.string.skill_timer_all_canceled)
            }

            // cancel all
            for (setTimer in SET_TIMERS) {
                setTimer.cancel()
            }
            SET_TIMERS.clear()
        } else {
            val setTimer = getSetTimerWithSimilarName(name)
            if (setTimer == null) {
                message = ctx().android()
                    .getString(R.string.skill_timer_no_active_name, name)
            } else {
                message = ctx().android()
                    .getString(R.string.skill_timer_canceled_name, setTimer.name)
                setTimer.cancel()
                SET_TIMERS.remove(setTimer)
            }
        }
        ctx().speechOutputDevice.speak(message)
        ctx().graphicalOutputDevice.display(
            GraphicalOutputUtils.buildSubHeader(ctx().android(), message)
        )
    }

    private fun queryTimer(name: String?) {
        val message = if (SET_TIMERS.isEmpty()) {
            ctx().android()
                .getString(R.string.skill_timer_no_active)
        } else if (name == null) {
            // no name provided by the user: query the last timer, but adapt the message if only one
            val lastTimer = SET_TIMERS[SET_TIMERS.size - 1]
            @StringRes val noNameQueryString: Int = if (SET_TIMERS.size == 1)
                R.string.skill_timer_query
            else
                R.string.skill_timer_query_last

            formatStringWithName(
                lastTimer.name, lastTimer.lastTickSeconds,
                noNameQueryString, R.string.skill_timer_query_name
            )

        } else {
            val setTimer = getSetTimerWithSimilarName(name)
            if (setTimer == null) {
                ctx().android().getString(R.string.skill_timer_no_active_name, name)
            } else {
                ctx().android().getString(
                    R.string.skill_timer_query_name, setTimer.name,
                    getFormattedDuration(setTimer.lastTickSeconds, true)
                )
            }
        }
        ctx().speechOutputDevice.speak(message)
        ctx().graphicalOutputDevice.display(
            GraphicalOutputUtils.buildSubHeader(ctx().android(), message)
        )
    }

    private fun getFormattedDuration(
        seconds: Long,
        speech: Boolean
    ): String {
        return ctx().requireNumberParserFormatter()
            .niceDuration(Duration.ofSeconds(seconds))
            .speech(speech)
            .get()
    }

    private fun formatStringWithName(
        name: String?,
        @StringRes stringWithoutName: Int,
        @StringRes stringWithName: Int
    ): String {
        return if (name == null) {
            ctx().android().getString(stringWithoutName)
        } else {
            ctx().android().getString(stringWithName, name)
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
            ctx().android().getString(stringWithoutName, duration)
        } else {
            ctx().android().getString(stringWithName, name, duration)
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
    }
}