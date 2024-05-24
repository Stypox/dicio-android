package org.stypox.dicio.util

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import org.stypox.dicio.di.LocaleManager
import java.util.Locale
import javax.inject.Inject

abstract class LocaleAwareActivity2 : ComponentActivity() {

    @Inject
    lateinit var localeManager: LocaleManager

    /**
     * Sets the locale according to value calculated by the injected [LocaleManager].
     */
    private fun setLocale(locale: Locale) {
        Locale.setDefault(locale)
        for (resources in sequenceOf(resources, applicationContext.resources)) {
            val configuration = resources.configuration
            configuration.setLocale(locale)
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
    }

    /**
     * Calls [setContent] with the provided [content], but it also listens for changes in
     * [LocaleManager.locale] and forces recompositions in case of changes.
     *
     * `LocalContext provides createConfigurationContext()` can't be used because then the
     * `LocalContext.current` wouldn't be a [ComponentActivity] anymore but just a plain `Context`,
     * causing things relying on activities to fail (e.g. `registerLauncherForActivityResult` or
     * https://slack-chats.kotlinlang.org/t/511933/if-i-handle-config-changes-manually-no-activity-recreation-a#30b520aa-70a5-40e9-b49c-475d88fa72f4 )
     */
    fun localeAwareSetContent(content: @Composable () -> Unit) {
        setContent {
            val locale = localeManager.locale.collectAsState()
            setLocale(locale.value)
            content()
        }
    }
}
