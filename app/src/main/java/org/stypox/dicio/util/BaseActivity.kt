package org.stypox.dicio.util

import android.os.Bundle
import androidx.annotation.StyleRes
import org.stypox.dicio.R

/**
 * A base for all of the activities that automatically recreates itself when the theme or the
 * language (locale) change
 */
abstract class BaseActivity : LocaleAwareActivity() {
    private var currentTheme = 0
    protected var isRecreating = false
        private set

    /**
     * Override this if extending activity needs some specific light/dark themes (different than the
     * default `R.style.Light/DarkAppTheme`).
     *
     * @return the id of the style resource to use as theme based on preferences
     */
    @get:StyleRes
    protected open val themeFromPreferences: Int
        get() = ThemeUtils.chooseThemeBasedOnPreferences(
            this,
            R.style.LightAppTheme, R.style.DarkAppTheme
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        isRecreating = false
        currentTheme = themeFromPreferences
        setTheme(currentTheme)
        super.onCreate(savedInstanceState)
    }

    override fun recreate() {
        isRecreating = true
        super.recreate()
    }

    override fun onResume() {
        super.onResume()
        if (currentTheme != themeFromPreferences) {
            recreate()
        }
    }
}