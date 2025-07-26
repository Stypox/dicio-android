package org.stypox.dicio.skills.weather

import android.icu.util.LocaleData
import android.icu.util.ULocale
import android.os.Build
import androidx.annotation.StringRes
import org.stypox.dicio.R
import java.util.Locale

enum class ResolvedLengthUnit(
    @StringRes
    val speedUnitString: Int,
    val multiplier: Double,
) {
    METRIC(R.string.skill_weather_meters_per_second, 1.0),
    IMPERIAL(R.string.skill_weather_miles_per_hour, 2.236936);

    fun convertSpeed(speedInMetersPerSecond: Double): Double {
        return speedInMetersPerSecond * multiplier
    }

    companion object {
        fun from(prefs: SkillSettingsWeather): ResolvedLengthUnit {
            return when (prefs.lengthUnit) {
                LengthUnit.UNRECOGNIZED,
                LengthUnit.LENGTH_UNIT_SYSTEM -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val locale = ULocale.forLocale(Locale.getDefault())
                        when (LocaleData.getMeasurementSystem(locale)) {
                            LocaleData.MeasurementSystem.UK,
                            LocaleData.MeasurementSystem.US -> IMPERIAL
                            else -> METRIC
                        }
                    } else {
                        METRIC // no system setting available on this device
                    }
                }
                LengthUnit.LENGTH_UNIT_METRIC -> METRIC
                LengthUnit.LENGTH_UNIT_IMPERIAL -> IMPERIAL
            }
        }
    }
}