package org.dicio.skill

import android.content.Context
import android.content.SharedPreferences
import org.dicio.numbers.ParserFormatter
import org.dicio.skill.output.GraphicalOutputDevice
import org.dicio.skill.output.SpeechOutputDevice
import java.util.Locale

/**
 * A class that just wraps the Android context, the Android shared preferences, the user locale, the
 * graphical output device and the speech output device. This class extends [Context], so it
 * can be used as an Android context, but it also has some more methods, namely. The setter methods
 * **should not** be used by skills.
 */
class SkillContext {
    /**
     * The Android context, useful for example to get resources, etc. Will only be `null` while the
     * context is being built or right before being freed, so this can be easily considered always
     * nonnull.
     * @apiNote the setter is not intended for usage inside skills, but only for constructing and
     * maintaining a skill context
     */
    var android: Context? = null

    /**
     * The Android shared preferences, useful for user customization, also see
     * [SkillInfo.hasPreferences] and [SkillInfo.preferenceFragment].
     * Will only be `null` while the context is being built or right before being
     * freed, so this can be easily considered always nonnull.
     * @apiNote the setter is not intended for usage inside skills, but only for constructing and
     * maintaining a skill context
     */
    var preferences: SharedPreferences? = null

    /**
     * The current user locale, useful for example to customize web requests to get the correct
     * language or country. Will only be `null` while the context is being built or right before
     * being freed, so this can be easily considered always nonnull.
     * @apiNote the setter is not intended for usage inside skills, but only for constructing and
     * maintaining a skill context
     */
    var locale: Locale? = null

    /**
     * The number parser formatter for the current locale, useful for example to format a
     * number to show to the user or extract numbers from an utterance. Is set to `null` if
     * the current user language is not supported by any [ParserFormatter].
     * @see ParserFormatter
     * @apiNote the setter is not intended for usage inside skills, but only for constructing and
     * maintaining a skill context
     */
    var parserFormatter: ParserFormatter? = null

    /**
     * The [GraphicalOutputDevice] that should be used for skill graphical output. Will only be
     * `null` while the context is being built or right before being freed, so this can be easily
     * considered always nonnull.
     * @apiNote the setter is not intended for usage inside skills, but only for constructing and
     * maintaining a skill context
     */
    var graphicalOutputDevice: GraphicalOutputDevice? = null

    /**
     * The [SpeechOutputDevice] that should be used for skill speech output. Will only be
     * `null` while the context is being built or right before being freed, so this can be easily
     * considered always nonnull.
     * @apiNote the setter is not intended for usage inside skills, but only for constructing and
     * maintaining a skill context
     */
    var speechOutputDevice: SpeechOutputDevice? = null
}
