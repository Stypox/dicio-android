package org.stypox.dicio.di

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import org.dicio.numbers.ParserFormatter
import org.dicio.skill.context.NoSettingsAccess
import org.dicio.skill.context.SettingsAccess
import org.dicio.skill.context.SkillContext
import org.dicio.skill.context.SpeechOutputDevice
import org.stypox.dicio.io.speech.NothingSpeechDevice
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SkillContextImpl private constructor(
    override val android: Context,
    private val localeManager: LocaleManager,
    // this constructor can take any SpeechOutputDevice to allow newForPreviews to provide
    // NothingSpeechDevice
    override val speechOutputDevice: SpeechOutputDevice,
    override val settingsAccess: SettingsAccess,
) : SkillContext {

    @Inject
    constructor(
        @ApplicationContext android: Context,
        localeManager: LocaleManager,
        speechOutputDevice: SpeechOutputDeviceWrapper,
        settingsAccess: SettingsAccessWrapper,
    ) : this(android, localeManager, speechOutputDevice as SpeechOutputDevice, settingsAccess as SettingsAccess)


    override val locale: Locale
        get() = localeManager.locale.value

    override val sentencesLanguage: String
        get() = localeManager.sentencesLanguage.value

    private var lastParserFormatter: Pair<ParserFormatter?, Locale>? = null
    override val parserFormatter: ParserFormatter?
        get() {
            val currentLocale = locale

            if (lastParserFormatter?.second?.equals(currentLocale) != true) {
                lastParserFormatter = try {
                    Pair(ParserFormatter(currentLocale), currentLocale)
                } catch (ignored: IllegalArgumentException) {
                    // current locale is not supported by dicio-numbers
                    Pair(null, currentLocale)
                }
            }

            return lastParserFormatter?.first
        }

    companion object {
        fun newForPreviews(context: Context): SkillContextImpl {
            val localeManager = LocaleManager.newForPreviews(context)
            val res = SkillContextImpl(
                context,
                localeManager,
                NothingSpeechDevice(),
                NoSettingsAccess(),
            )
            @SuppressLint("StateFlowValueCalledInComposition")
            res.lastParserFormatter = Pair(
                null,
                localeManager.locale.value
            )
            return res
        }
    }
}
