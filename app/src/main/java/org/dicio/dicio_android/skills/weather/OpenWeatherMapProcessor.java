package org.dicio.dicio_android.skills.weather;

import static org.dicio.dicio_android.Sentences_en.weather;
import static org.dicio.dicio_android.util.StringUtils.isNullOrEmpty;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.util.ConnectionUtils;
import org.dicio.dicio_android.util.StringUtils;
import org.dicio.skill.chain.IntermediateProcessor;
import org.dicio.skill.standard.StandardResult;
import org.json.JSONObject;

import java.io.FileNotFoundException;

public class OpenWeatherMapProcessor
        extends IntermediateProcessor<StandardResult, WeatherOutput.Data> {

    private static final String IP_INFO_URL = "https://ipinfo.io/json";
    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String API_KEY = "061f24cf3cde2f60644a8240302983f2";
    private static final String ICON_BASE_URL = "https://openweathermap.org/img/wn/";
    private static final String ICON_FORMAT = "@2x.png";


    @Override
    public WeatherOutput.Data process(final StandardResult data) throws Exception {

        final WeatherOutput.Data result = new WeatherOutput.Data();
        result.city = data.getCapturingGroup(weather.where);
        if (result.city != null) {
            result.city = StringUtils.removePunctuation(result.city.trim());
        }

        if (isNullOrEmpty(result.city)) {
            result.city = ctx().getPreferences().getString(ctx().android().getString(
                    R.string.pref_key_weather_default_city), "");
            result.city = StringUtils.removePunctuation(result.city.trim());
        }

        if (result.city.isEmpty()) {
            final JSONObject ipInfo = ConnectionUtils.getPageJson(IP_INFO_URL);
            result.city = ipInfo.getString("city");
        }

        final JSONObject weatherData;
        try {
            weatherData = ConnectionUtils.getPageJson(WEATHER_API_URL
                    + "?APPID=" + API_KEY
                    + "&units=metric&lang=" + ctx().getLocale().getLanguage().toLowerCase()
                    + "&q=" + ConnectionUtils.urlEncode(result.city));
        } catch (final FileNotFoundException ignored) {
            result.failed = true;
            return result;
        }

        final JSONObject weatherObject = weatherData.getJSONArray("weather").getJSONObject(0);
        final JSONObject mainObject = weatherData.getJSONObject("main");
        final JSONObject windObject = weatherData.getJSONObject("wind");

        result.city = weatherData.getString("name");
        result.description = weatherObject.getString("description");
        result.iconUrl = ICON_BASE_URL + weatherObject.getString("icon") + ICON_FORMAT;
        result.temp = mainObject.getDouble("temp");
        result.tempMin = mainObject.getDouble("temp_min");
        result.tempMax = mainObject.getDouble("temp_max");
        result.windSpeed = windObject.getDouble("speed");

        result.description = Character.toUpperCase(result.description.charAt(0))
                + result.description.substring(1);
        return result;
    }
}
