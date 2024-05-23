package org.dicio.skill.context

import android.content.Context
import android.content.SharedPreferences
import org.dicio.numbers.ParserFormatter
import java.util.Locale

/**
 * An interface for providing access to various services and information to skills. Contains the
 * Android context, the Android shared preferences, the user locale, the parser/formatter and the
 * speech output device.
 */
interface SkillContext {
    /**
     * The Android context, useful for example to get resources, etc.
     */
    val android: Context

    /**
     * The Android shared preferences, useful for user customization, also see
     * [org.dicio.skill.skill.SkillInfo.renderSettings].
     */
    val preferences: SharedPreferences

    /**
     * The current user locale, useful for example to customize web requests to get the correct
     * language or country.
     */
    val locale: Locale

    /**
     * The number parser formatter for the current locale, useful for example to format a
     * number to show to the user or extract numbers from an utterance. Is set to `null` if
     * the current user language is not supported by any [ParserFormatter].
     * @see ParserFormatter
     */
    val parserFormatter: ParserFormatter?

    /**
     * The [SpeechOutputDevice] that should be used for skill speech output.
     */
    val speechOutputDevice: SpeechOutputDevice
}
