package org.stypox.dicio.skills.weather

import org.dicio.skill.chain.IntermediateProcessor
import org.dicio.skill.standard.StandardResult
import org.json.JSONObject
import org.stypox.dicio.R
import org.stypox.dicio.Sentences_en.weather
import org.stypox.dicio.util.ConnectionUtils
import org.stypox.dicio.util.StringUtils
import java.io.FileNotFoundException
import java.util.Locale

class OpenWeatherMapProcessor : IntermediateProcessor<StandardResult, WeatherOutput.Data?>() {
    @Throws(Exception::class)
    override fun process(data: StandardResult): WeatherOutput.Data {
        val result = WeatherOutput.Data()
        result.city = data.getCapturingGroup(weather.where)
            ?.let { StringUtils.removePunctuation(it.trim { ch -> ch <= ' ' }) }

        if (result.city.isNullOrEmpty()) {
            result.city = ctx().preferences.getString(
                ctx().android().getString(R.string.pref_key_weather_default_city), ""
            )?.let { StringUtils.removePunctuation(it.trim { ch -> ch <= ' ' }) }
        }

        if (result.city.isNullOrEmpty()) {
            result.city = ConnectionUtils.getPageJson(IP_INFO_URL).getString("city")
        }

        val weatherData: JSONObject
        try {
            weatherData = ConnectionUtils.getPageJson(
                "$WEATHER_API_URL?APPID=$API_KEY&units=metric&lang=" +
                        ctx().locale.language.lowercase(Locale.getDefault()) +
                        "&q=" + ConnectionUtils.urlEncode(result.city)
            )
        } catch (ignored: FileNotFoundException) {
            result.failed = true
            return result
        }

        val weatherObject: JSONObject = weatherData.getJSONArray("weather").getJSONObject(0)
        val mainObject: JSONObject = weatherData.getJSONObject("main")
        val windObject: JSONObject = weatherData.getJSONObject("wind")
        result.city = weatherData.getString("name")
        result.description = weatherObject.getString("description")
            .apply { this[0].uppercaseChar() + this.substring(1) }
        result.iconUrl = ICON_BASE_URL + weatherObject.getString("icon") + ICON_FORMAT
        result.temp = mainObject.getDouble("temp")
        result.tempMin = mainObject.getDouble("temp_min")
        result.tempMax = mainObject.getDouble("temp_max")
        result.windSpeed = windObject.getDouble("speed")
        return result
    }

    companion object {
        private const val IP_INFO_URL = "https://ipinfo.io/json"
        private const val WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather"
        private const val API_KEY = "061f24cf3cde2f60644a8240302983f2"
        private const val ICON_BASE_URL = "https://openweathermap.org/img/wn/"
        private const val ICON_FORMAT = "@2x.png"
    }
}