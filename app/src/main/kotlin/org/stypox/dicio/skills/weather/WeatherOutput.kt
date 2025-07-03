package org.stypox.dicio.skills.weather

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillOutput
import org.stypox.dicio.R
import org.stypox.dicio.io.graphical.HeadlineSpeechSkillOutput
import org.stypox.dicio.util.getString
import org.stypox.dicio.util.lowercaseCapitalized
import java.util.Locale

sealed interface WeatherOutput : SkillOutput {
    data class Success(
        val city: String,
        val description: String,
        val iconUrl: String,
        val temp: Double, // in Celsius
        val tempMin: Double, // in Celsius
        val tempMax: Double, // in Celsius
        val tempString: String, // spoken, in temperatureUnit
        val windSpeed: Double, // in m/s
        val temperatureUnit: ResolvedTemperatureUnit,
        val lengthUnit: ResolvedLengthUnit,
    ) : WeatherOutput {
        override fun getSpeechOutput(ctx: SkillContext): String = ctx.getString(
            R.string.skill_weather_in_city_there_is_description, city, description, tempString
        )

        @Composable
        override fun GraphicalOutput(ctx: SkillContext) {
            CurrentWeatherRow(data = this)
        }
    }

    data class Failed(
        val city: String
    ) : WeatherOutput, HeadlineSpeechSkillOutput {
        override fun getSpeechOutput(ctx: SkillContext): String = ctx.getString(
            R.string.skill_weather_could_not_find_city, city
        )
    }
}


@Composable
fun CurrentWeatherRow(data: WeatherOutput.Success) {
    val temperatureUnit = stringResource(data.temperatureUnit.unitString)
    val speedUnit = stringResource(data.lengthUnit.speedUnitString)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WeatherImage(
            iconUrl = data.iconUrl,
            description = data.description,
            widthFraction = 0.38f
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = data.city.lowercaseCapitalized(Locale.getDefault()),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = stringResource(
                    R.string.skill_weather_description_temperature,
                    data.description.lowercaseCapitalized(Locale.getDefault()),
                    data.temperatureUnit.convert(data.temp),
                    temperatureUnit,
                ),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.skill_weather_min_max_wind,
                    data.temperatureUnit.convert(data.tempMin),
                    data.temperatureUnit.convert(data.tempMax),
                    data.lengthUnit.convertSpeed(data.windSpeed),
                    temperatureUnit,
                    speedUnit
                ),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun WeatherImage(iconUrl: String, description: String, widthFraction: Float) {
    AsyncImage(
        model = iconUrl,
        contentDescription = description,
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .aspectRatio(1.0f)
            .testTag("weather_image"),
    )
}
