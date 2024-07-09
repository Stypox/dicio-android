package org.stypox.dicio.screenshot

import android.Manifest
import android.content.Context
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.datastore.core.DataStore
import androidx.test.rule.GrantPermissionRule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.stypox.dicio.MainActivity
import org.stypox.dicio.di.LocaleManager
import org.stypox.dicio.di.SttInputDeviceWrapper
import org.stypox.dicio.di.SttInputDeviceWrapperModule
import org.stypox.dicio.settings.datastore.UserSettings
import org.stypox.dicio.ui.home.SttState
import javax.inject.Inject
import javax.inject.Singleton

@UninstallModules(SttInputDeviceWrapperModule::class)
@HiltAndroidTest
class MainScreenScreenshots {
    @Module
    @InstallIn(SingletonComponent::class)
    class FakeSttInputDeviceWrapperModule {
        @Provides
        @Singleton
        fun provideInputDeviceWrapper(
            @ApplicationContext appContext: Context,
            dataStore: DataStore<UserSettings>,
            localeManager: LocaleManager,
            okHttpClient: OkHttpClient,
        ): SttInputDeviceWrapper {
            return FakeSttInputDeviceWrapper(appContext, dataStore, localeManager, okHttpClient)
        }
    }

    @Inject
    lateinit var sttInputDeviceWrapper: SttInputDeviceWrapper

    private val fakeSttInputDeviceWrapper: FakeSttInputDeviceWrapper get() =
        sttInputDeviceWrapper as FakeSttInputDeviceWrapper

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO)

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun takeScreenshots() {
        runBlocking { fakeSttInputDeviceWrapper.fakeUiState.emit(SttState.Listening) }
        composeRule.takeScreenshot("en-US", "0")
        runBlocking { fakeSttInputDeviceWrapper.fakeUiState.emit(SttState.Loaded) }
        composeRule.takeScreenshot("en-US", "1")
    }
}
