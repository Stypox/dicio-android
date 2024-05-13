package org.stypox.dicio.skills.timer

import android.os.CountDownTimer
import androidx.compose.runtime.LongState
import androidx.compose.runtime.mutableLongStateOf
import java.time.Duration


class SetTimer(
    duration: Duration,
    name: String?,
    onMillisTickCallback: (Long) -> Unit,
    onSecondsTickCallback: (Long) -> Unit,
    onExpiredCallback: (String?) -> Unit,
    val onCancelCallback: (SetTimer) -> Unit
) {
    val name: String? = name?.trim { it <= ' ' }
    private val _lastTickMillis = mutableLongStateOf(duration.toMillis())
    val lastTickMillis: Long get() = _lastTickMillis.longValue
    val lastTickMillisState: LongState get() = _lastTickMillis

    private val countDownTimer = object : CountDownTimer(
        duration.toMillis() + RINGTONE_DURATION_MILLIS,
        TIMER_TICK_MILLIS
    ) {
        override fun onTick(millisUntilFinished: Long) {
            val prevLastTickMillis = _lastTickMillis.longValue
            val newLastTickMillis = millisUntilFinished - RINGTONE_DURATION_MILLIS
            _lastTickMillis.longValue = newLastTickMillis

            if (prevLastTickMillis >= 0) {
                // round up to the nearest second
                val prevLastTickSeconds = (prevLastTickMillis + 999) / 1000
                val lastTickSeconds = (newLastTickMillis + 999) / 1000

                if (prevLastTickSeconds != lastTickSeconds) {
                    onSecondsTickCallback(lastTickSeconds)
                }
                if (newLastTickMillis < 0) {
                    onExpiredCallback(name)
                }
            }

            onMillisTickCallback(newLastTickMillis)
        }

        override fun onFinish() {
            onMillisTickCallback(-RINGTONE_DURATION_MILLIS)
            onCancelCallback(this@SetTimer)
        }
    }.apply { start() }

    fun cancel() {
        countDownTimer.cancel()
        onCancelCallback(this)
    }

    companion object {
        private const val TIMER_TICK_MILLIS = 100L // milliseconds
        private const val RINGTONE_DURATION_MILLIS = 30000L // 30 seconds of ringtone
    }
}
