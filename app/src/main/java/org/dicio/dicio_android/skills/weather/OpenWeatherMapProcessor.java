package org.dicio.dicio_android.skills.weather;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.util.ConnectionUtils;
import org.dicio.dicio_android.util.StringUtils;
import org.dicio.skill.SkillContext;
import org.dicio.skill.chain.IntermediateProcessor;
import org.dicio.skill.standard.StandardResult;
import org.json.JSONObject;

import java.io.FileNotFoundException;

import static org.dicio.dicio_android.Sentences_en.weather;
import static org.dicio.dicio_android.util.StringUtils.isNullOrEmpty;

public class OpenWeatherMapProcessor
        implements IntermediateProcessor<StandardResult, WeatherOutput.Data> {

    private static final String ipInfoUrl = "https://ipinfo.io/json";
    private static final String weatherApiUrl = "https://api.openweathermap.org/data/2.5/weather";
    private static final String apiKey = "061f24cf3cde2f60644a8240302983f2";
    private static final String iconBaseUrl = "https://openweathermap.org/img/wn/";
    private static final String iconFormat = "@2x.png";


    @Override
    public WeatherOutput.Data process(final StandardResult data, final SkillContext context)
            throws Exception {

        final WeatherOutput.Data result = new WeatherOutput.Data();
        result.city = data.getCapturingGroup(weather.where);
        if (result.city != null) {
            result.city = StringUtils.removePunctuation(result.city.trim());
        }

        if (isNullOrEmpty(result.city)) {
            result.city = context.getPreferences().getString(context.getAndroidContext().getString(
                    R.string.pref_key_weather_default_city), "");
            result.city = StringUtils.removePunctuation(result.city.trim());
        }

        if (result.city.isEmpty()) {
            final JSONObject ipInfo = ConnectionUtils.getPageJson(ipInfoUrl);
            result.city = ipInfo.getString("city");
        }

        final JSONObject weatherData;
        try {
            weatherData = ConnectionUtils.getPageJson(weatherApiUrl
                    + "?APPID=" + apiKey
                    + "&units=metric&lang=" + context.getLocale().getLanguage().toLowerCase()
                    + "&q=" + ConnectionUtils.urlEncode(result.city));
        } catch (FileNotFoundException ignored) {
            result.failed = true;
            return result;
        }

        final JSONObject weatherObject = weatherData.getJSONArray("weather").getJSONObject(0);
        final JSONObject mainObject = weatherData.getJSONObject("main");
        final JSONObject windObject = weatherData.getJSONObject("wind");

        result.city = weatherData.getString("name");
        result.description = weatherObject.getString("description");
        result.iconUrl = iconBaseUrl + weatherObject.getString("icon") + iconFormat;
        result.temp = mainObject.getDouble("temp");
        result.tempMin = mainObject.getDouble("temp_min");
        result.tempMax = mainObject.getDouble("temp_max");
        result.windSpeed = windObject.getDouble("speed");

        result.description = Character.toUpperCase(result.description.charAt(0)) +
                result.description.substring(1);
        return result;
    }
}
