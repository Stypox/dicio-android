package org.stypox.dicio.util

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.LocaleListCompat
import androidx.preference.PreferenceManager
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.skills.SkillHandler
import org.stypox.dicio.util.LocaleUtils.UnsupportedLocaleException
import java.util.Locale

/**
 * A base for all of the activities that automatically recreates itself when the language (locale)
 * changes
 */
abstract class LocaleAwareActivity : AppCompatActivity() {
    private var currentLanguage: String? = null
    private val localeFromPreferences: String?
        get() = PreferenceManager.getDefaultSharedPreferences(this)
            .getString(getString(R.string.pref_key_language), null)

    private fun setLocale() {
        val sectionsLocale = try {
            Sections.setLocale(LocaleUtils.getAvailableLocalesFromPreferences(this))
        } catch (e: UnsupportedLocaleException) {
            Log.w(TAG, "Current locale is not supported, defaulting to English", e)
            try {
                // TODO ask the user to manually choose a locale instead of defaulting to english
                Sections.setLocale(LocaleListCompat.create(Locale.ENGLISH))
            } catch (e1: UnsupportedLocaleException) {
                Log.wtf(TAG, "COULD NOT LOAD THE ENGLISH LOCALE SECTIONS, IMPOSSIBLE!", e1)
                return
            }
        }

        Locale.setDefault(sectionsLocale)
        val resources = resources
        val configuration = resources.configuration
        configuration.setLocale(sectionsLocale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        currentLanguage = localeFromPreferences
        setLocale()
        // setup each time the activity is (re)created, but only clear in MainActivity.onDestroy()
        SkillHandler.setSkillContextAndroidAndLocale(this)
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (currentLanguage != localeFromPreferences) {
            recreate()
        }
    }

    companion object {
        val TAG: String = LocaleAwareActivity::class.java.simpleName
    }
}