package org.stypox.dicio.skills.weather

import android.content.Context
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.dicio.skill.SkillContext
import org.dicio.skill.output.SkillOutput
import org.stypox.dicio.R
import org.stypox.dicio.output.graphical.Headline
import org.stypox.dicio.util.getString
import org.stypox.dicio.util.lowercaseCapitalized
import java.util.Locale

class WeatherOutput(
    context: Context,
    private val data: WeatherGenerator.Data,
) : SkillOutput {
    override fun getSpeechOutput(ctx: SkillContext): String = when (data) {
        is WeatherGenerator.Data.Success -> ctx.getString(
            R.string.skill_weather_in_city_there_is_description, data.city, data.description
        )
        is WeatherGenerator.Data.Failed -> ctx.getString(
            R.string.skill_weather_could_not_find_city, data.city
        )
    }

    @Composable
    override fun GraphicalOutput(ctx: SkillContext) {
        when (data) {
            is WeatherGenerator.Data.Success -> CurrentWeatherRow(data = data)
            is WeatherGenerator.Data.Failed -> Headline(text = getSpeechOutput(ctx))
        }
    }
}


@Composable
fun CurrentWeatherRow(data: WeatherGenerator.Data.Success) {
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
                    data.temp
                ),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.skill_weather_min_max_wind,
                    data.tempMin, data.tempMax, data.windSpeed
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
            .aspectRatio(1.0f),
    )
}
