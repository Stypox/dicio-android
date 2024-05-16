package org.stypox.dicio.di

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import org.dicio.numbers.ParserFormatter
import org.dicio.skill.SkillContext
import org.dicio.skill.output.SpeechOutputDevice
import org.stypox.dicio.io.speech.NothingSpeechDevice
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SkillContextImpl @Inject constructor(
    @ApplicationContext override val android: Context,
    override val preferences: SharedPreferences,
    private val localeManager: LocaleManager,
) : SkillContext {
    override val locale: Locale
        get() = localeManager.locale

    private var lastParserFormatter: Pair<ParserFormatter?, Locale>? = null
    override val parserFormatter: ParserFormatter?
        get() {
            val locale = localeManager.locale

            if (lastParserFormatter?.second?.equals(locale) != true) {
                lastParserFormatter = try {
                    Pair(ParserFormatter(locale), locale)
                } catch (ignored: IllegalArgumentException) {
                    // current locale is not supported by dicio-numbers
                    Pair(null, locale)
                }
            }

            return lastParserFormatter?.first
        }

    override var speechOutputDevice: SpeechOutputDevice = NothingSpeechDevice()
        internal set

    companion object {
        @Composable
        fun newForPreviews(): SkillContext = SkillContextImpl(
            LocalContext.current,
            PreferenceManager.getDefaultSharedPreferences(LocalContext.current),
            LocaleManager(LocalContext.current),
        )
    }
}
