package com.dicio.dicio_android.components.processing;

import com.dicio.component.IntermediateProcessor;
import com.dicio.component.standard.StandardResult;
import com.dicio.dicio_android.ApiKeys;
import com.dicio.dicio_android.components.output.WeatherOutput;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.List;

import static com.dicio.dicio_android.util.ConnectionUtils.getPageJson;

public class OpenWeatherMapProcessor implements IntermediateProcessor<StandardResult, WeatherOutput.Data> {

    private static final String ipInfoUrl = "https://ipinfo.io/json";
    private static final String weatherApiUrl = "https://api.openweathermap.org/data/2.5/weather";


    @Override
    public WeatherOutput.Data process(StandardResult data) throws Exception {
        WeatherOutput.Data result = new WeatherOutput.Data();
        if (data.getCapturingGroups().size() == 1) {
            List<String> capturingGroup = data.getCapturingGroups().get(0);
            StringBuilder capturingGroupJoined = new StringBuilder(capturingGroup.get(0));

            for (int i = 1; i < capturingGroup.size(); ++i) {
                capturingGroupJoined.append(" ");
                capturingGroupJoined.append(capturingGroup.get(i));
            }

            result.city = capturingGroupJoined.toString();
        } else {
            JSONObject ipInfo = getPageJson(ipInfoUrl);
            result.city = ipInfo.getString("city");
        }

        JSONObject weatherData;
        try {
            weatherData = getPageJson(weatherApiUrl + "?APPID=" + ApiKeys.openweathermap
                    + "&units=metric&lang=en&q=" + result.city);
        } catch (FileNotFoundException ignored) {
            result.failed = true;
            return result;
        }

        JSONObject weatherObject = weatherData.getJSONArray("weather").getJSONObject(0);
        JSONObject mainObject = weatherData.getJSONObject("main");
        JSONObject windObject = weatherData.getJSONObject("wind");

        result.city = weatherData.getString("name");
        result.description = weatherObject.getString("description");
        result.icon = weatherObject.getString("icon");
        result.temp = mainObject.getDouble("temp");
        result.tempMin = mainObject.getDouble("temp_min");
        result.tempMax = mainObject.getDouble("temp_max");
        result.windSpeed = windObject.getDouble("speed");

        result.description = Character.toUpperCase(result.description.charAt(0)) +
                result.description.substring(1);
        return result;
    }
}
