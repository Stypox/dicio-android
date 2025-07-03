package org.stypox.dicio.skills.weather

import androidx.annotation.StringRes
import androidx.core.text.util.LocalePreferences
import org.stypox.dicio.R

enum class ResolvedTemperatureUnit(
    @StringRes
    val unitString: Int,
    val offset: Double,
    val multiplier: Double,
) {
    CELSIUS(R.string.skill_weather_celsius, 0.0, 1.0),
    FAHRENHEIT(R.string.skill_weather_fahrenheit, 32.0, 1.8),
    KELVIN(R.string.skill_weather_kelvin, 274.15, 1.0);

    fun convert(tempInCelsius: Double): Double {
        return tempInCelsius * multiplier + offset
    }

    companion object {
        fun from(prefs: SkillSettingsWeather): ResolvedTemperatureUnit {
            return when (prefs.temperatureUnit) {
                TemperatureUnit.UNRECOGNIZED,
                TemperatureUnit.TEMPERATURE_UNIT_SYSTEM -> {
                    when (LocalePreferences.getTemperatureUnit()) {
                        LocalePreferences.TemperatureUnit.FAHRENHEIT -> FAHRENHEIT
                        LocalePreferences.TemperatureUnit.KELVIN -> KELVIN
                        else -> CELSIUS // default
                    }
                }
                TemperatureUnit.TEMPERATURE_UNIT_CELSIUS -> CELSIUS
                TemperatureUnit.TEMPERATURE_UNIT_FAHRENHEIT -> FAHRENHEIT
                TemperatureUnit.TEMPERATURE_UNIT_KELVIN -> KELVIN
            }
        }
    }
}
