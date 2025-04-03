package org.stypox.dicio.skills.weather

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import androidx.datastore.migrations.SharedPreferencesMigration
import kotlinx.coroutines.launch
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.sentences.Sentences
import org.stypox.dicio.settings.ui.StringSetting

object WeatherInfo : SkillInfo("weather") {
    override fun name(context: Context) =
        context.getString(R.string.skill_name_weather)

    override fun sentenceExample(context: Context) =
        context.getString(R.string.skill_sentence_example_weather)

    @Composable
    override fun icon() =
        rememberVectorPainter(Icons.Default.Cloud)

    override fun isAvailable(ctx: SkillContext): Boolean {
        return Sentences.Weather[ctx.sentencesLanguage] != null
    }

    override fun build(ctx: SkillContext): Skill<*> {
        return WeatherSkill(WeatherInfo, Sentences.Weather[ctx.sentencesLanguage]!!)
    }

    // no need to use Hilt injection here, let DataStore take care of handling the singleton itself
    internal val Context.weatherDataStore by dataStore(
        fileName = "skill_settings_weather.pb",
        serializer = SkillSettingsWeatherSerializer,
        corruptionHandler = ReplaceFileCorruptionHandler {
            SkillSettingsWeatherSerializer.defaultValue
        },
        produceMigrations = { context ->
            listOf(
                SharedPreferencesMigration(
                    context,
                    context.packageName + "_preferences",
                ) { prefs, skillSettingsWeather ->
                    skillSettingsWeather.toBuilder()
                        .setDefaultCity(prefs.getString("weather_default_city"))
                        .build()
                }
            )
        },
    )

    override val renderSettings: @Composable () -> Unit get() = @Composable {
        val dataStore = LocalContext.current.weatherDataStore
        val data by dataStore.data.collectAsState(SkillSettingsWeatherSerializer.defaultValue)
        val scope = rememberCoroutineScope()

        StringSetting(
            title = stringResource(R.string.pref_weather_default_city),
            descriptionWhenEmpty = stringResource(R.string.pref_weather_default_city_using_ip_info),
        ).Render(
            value = data.defaultCity,
            onValueChange = { defaultCity ->
                scope.launch {
                    dataStore.updateData { skillSettingsWeather ->
                        skillSettingsWeather.toBuilder()
                            .setDefaultCity(defaultCity)
                            .build()
                    }
                }
            },
        )
    }
}
