package org.stypox.dicio.skills.weather

import kotlinx.coroutines.flow.first
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.StandardRecognizerSkill
import org.stypox.dicio.sentences.Sentences.Weather
import org.stypox.dicio.skills.weather.WeatherInfo.weatherDataStore
import org.stypox.dicio.util.ConnectionUtils
import org.stypox.dicio.util.StringUtils
import java.io.FileNotFoundException
import java.util.Locale
import kotlin.math.roundToInt

class WeatherSkill(correspondingSkillInfo: SkillInfo, data: StandardRecognizerData<Weather>) :
    StandardRecognizerSkill<Weather>(correspondingSkillInfo, data) {
    override suspend fun generateOutput(ctx: SkillContext, inputData: Weather): SkillOutput {
        var city = when (inputData) {
            is Weather.Current -> inputData.where
        }

        if (city.isNullOrEmpty()) {
            city = ctx.android.weatherDataStore.data.first().defaultCity
                .let { StringUtils.removePunctuation(it.trim { ch -> ch <= ' ' }) }
        }

        if (city.isEmpty()) {
            city = ConnectionUtils.getPageJson(IP_INFO_URL).getString("city")
        }

        if (city.isNullOrEmpty()) {
            return WeatherOutput.Failed(city = city ?: "")
        }

        val weatherData = try {
            ConnectionUtils.getPageJson(
                "$WEATHER_API_URL?APPID=$API_KEY&units=metric&lang=" +
                        ctx.locale.language.lowercase(Locale.getDefault()) +
                        "&q=" + ConnectionUtils.urlEncode(city)
            )
        } catch (ignored: FileNotFoundException) {
            return WeatherOutput.Failed(city = city)
        }

        val weatherObject = weatherData.getJSONArray("weather").getJSONObject(0)
        val mainObject = weatherData.getJSONObject("main")
        val windObject = weatherData.getJSONObject("wind")

        val temp = mainObject.getDouble("temp")
        return WeatherOutput.Success(
            city = weatherData.getString("name"),
            description = weatherObject.getString("description")
                .apply { this[0].uppercaseChar() + this.substring(1) },
            iconUrl = ICON_BASE_URL + weatherObject.getString("icon") + ICON_FORMAT,
            temp = temp,
            tempMin = mainObject.getDouble("temp_min"),
            tempMax = mainObject.getDouble("temp_max"),
            tempString = ctx.parserFormatter
                ?.niceNumber(temp.roundToInt().toDouble())?.speech(true)?.get()
                ?: (temp.roundToInt().toString()),
            windSpeed = windObject.getDouble("speed"),
        )
    }

    companion object {
        private const val IP_INFO_URL = "https://ipinfo.io/json"
        private const val WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather"
        private const val API_KEY = "061f24cf3cde2f60644a8240302983f2"
        private const val ICON_BASE_URL = "https://openweathermap.org/img/wn/"
        private const val ICON_FORMAT = "@2x.png"
    }
}
