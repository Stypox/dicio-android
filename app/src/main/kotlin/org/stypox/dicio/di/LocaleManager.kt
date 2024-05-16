package org.stypox.dicio.di

import android.content.Context
import android.util.Log
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import org.stypox.dicio.Sections
import org.stypox.dicio.util.LocaleAwareActivity
import org.stypox.dicio.util.LocaleUtils
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Chooses locale and localeString from languages configured in the user's system, making sure in
 * particular that skill examples exist for the chosen locale, because otherwise the LLM wouldn't
 * work.
 */
@Singleton
class LocaleManager @Inject constructor(
    @ApplicationContext appContext: Context
) {
    // TODO maybe let the user choose the language in settings?
    val locale: Locale = try {
        Sections.setLocale(
            LocaleUtils.getAvailableLocalesFromPreferences(appContext)
        )
    } catch (e: LocaleUtils.UnsupportedLocaleException) {
        Log.w(LocaleAwareActivity.TAG, "Current locale is not supported, defaulting to English", e)
        try {
            // TODO ask the user to manually choose a locale instead of defaulting to english
            Sections.setLocale(LocaleListCompat.create(Locale.ENGLISH))
        } catch (e1: LocaleUtils.UnsupportedLocaleException) {
            Log.wtf(
                LocaleAwareActivity.TAG,
                "COULD NOT LOAD THE ENGLISH LOCALE SECTIONS, IMPOSSIBLE!",
                e1,
            )
            error("COULD NOT LOAD THE ENGLISH LOCALE SECTIONS, IMPOSSIBLE!")
        }
    }

    companion object {
        val TAG = LocaleManager::class.simpleName
    }
}
