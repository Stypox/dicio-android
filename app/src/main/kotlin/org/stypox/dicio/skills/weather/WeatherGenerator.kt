package org.stypox.dicio.skills.weather

import org.dicio.skill.chain.OutputGenerator
import org.dicio.skill.output.SkillOutput

class WeatherGenerator : OutputGenerator<WeatherGenerator.Data>() {
    sealed class Data {
        data class Success(
            val city: String,
            val description: String,
            val iconUrl: String,
            val temp: Double,
            val tempMin: Double,
            val tempMax: Double,
            val windSpeed: Double,
        ) : Data()

        data class Failed(
            val city: String?
        ) : Data()
    }

    override fun generate(data: Data): SkillOutput {
        return WeatherOutput(ctx().android!!, data)
    }
}
