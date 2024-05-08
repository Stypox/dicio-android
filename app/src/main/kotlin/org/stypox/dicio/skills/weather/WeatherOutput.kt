package org.stypox.dicio.skills.weather

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import org.dicio.skill.chain.OutputGenerator
import org.stypox.dicio.R
import org.stypox.dicio.output.graphical.GraphicalOutputUtils

class WeatherOutput : OutputGenerator<WeatherOutput.Data>() {
    class Data {
        var failed = false
        var city: String? = null
        var description: String? = null
        var iconUrl: String? = null
        var temp = 0.0
        var tempMin = 0.0
        var tempMax = 0.0
        var windSpeed = 0.0
    }

    override fun generate(data: Data) {
        if (data.failed) {
            val message = ctx().android().getString(
                R.string.skill_weather_could_not_find_city, data.city
            )
            ctx().speechOutputDevice.speak(message)
            ctx().graphicalOutputDevice.display(
                GraphicalOutputUtils.buildSubHeader(
                    ctx().android(), message
                )
            )
        } else {
            ctx().speechOutputDevice.speak(
                ctx().android().getString(
                    R.string.skill_weather_in_city_there_is_description,
                    data.city, data.description
                )
            )
            val weatherView = GraphicalOutputUtils.inflate(
                ctx().android(),
                R.layout.skill_weather
            )
            Picasso.get().load(data.iconUrl).into(
                weatherView.findViewById<View>(R.id.image) as ImageView
            )
            (weatherView.findViewById<View>(R.id.city) as TextView).text = data.city
            (weatherView.findViewById<View>(R.id.basicInfo) as TextView).text =
                ctx().android().getString(
                    R.string.skill_weather_description_temperature,
                    data.description, data.temp
                )
            (weatherView.findViewById<View>(R.id.advancedInfo) as TextView).text =
                ctx().android().getString(
                    R.string.skill_weather_min_max_wind,
                    data.tempMin, data.tempMax, data.windSpeed
                )
            ctx().graphicalOutputDevice.display(weatherView)
        }
    }
}