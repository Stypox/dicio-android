package com.dicio.dicio_android.components;

import com.dicio.component.AssistanceComponent;
import com.dicio.component.input.standard.StandardRecognizer;
import com.dicio.component.output.views.BaseView;
import com.dicio.component.output.views.DescribedImage;
import com.dicio.component.output.views.Description;
import com.dicio.component.output.views.Image;
import com.dicio.dicio_android.Sentences;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class WeatherComponent extends StandardRecognizer implements AssistanceComponent {
    private static final String ipInfoUrl = "https://ipinfo.io/json";
    private static final String weatherApiUrl = "https://api.openweathermap.org/data/2.5/weather";
    private static final String iconUrl = "http://openweathermap.org/img/wn/";

    private String city, description, icon;
    private double temp, tempMin, tempMax, windSpeed;

    private JSONObject getPageJson(String url, HashMap<String, String> urlParams) throws IOException, JSONException {
        StringBuilder paramsStr = new StringBuilder("?");
        for (Map.Entry<String, String> urlParam : urlParams.entrySet()) {
            paramsStr.append("&");
            paramsStr.append(urlParam.getKey());
            paramsStr.append("=");
            paramsStr.append(urlParam.getValue());
        }

        URLConnection connection = new URL(url + paramsStr.toString()).openConnection();
        Scanner scanner = new Scanner(connection.getInputStream());
        String responseBody = scanner.useDelimiter("\\A").next();
        return new JSONObject(responseBody);
    }

    public WeatherComponent() {
        super(Sentences.weather);
    }

    @Override
    public void calculateOutput() throws Exception {
        JSONObject ipInfo = getPageJson(ipInfoUrl, new HashMap<>());
        String ipCity = ipInfo.getString("city");

        JSONObject weatherData = getPageJson(weatherApiUrl, new HashMap<String, String>() {{
            put("APPID", "061f24cf3cde2f60644a8240302983f2"); // testing api key from https://codepen.io/awalthefirst/pen/LVLBWy/
            put("units", "metric");
            put("lang", "en");
            put("q", ipCity);
        }});

        JSONObject weatherObject = weatherData.getJSONArray("weather").getJSONObject(0);
        JSONObject mainObject = weatherData.getJSONObject("main");
        JSONObject windObject = weatherData.getJSONObject("wind");

        city = weatherData.getString("name");
        description = weatherObject.getString("description");
        icon = weatherObject.getString("icon");
        temp = mainObject.getDouble("temp");
        tempMin = mainObject.getDouble("temp_min");
        tempMax = mainObject.getDouble("temp_max");
        windSpeed = windObject.getDouble("speed");

        description = Character.toUpperCase(description.charAt(0)) + description.substring(1);
    }

    @Override
    public List<BaseView> getGraphicalOutput() {
        return new ArrayList<BaseView>() {{
            add(new DescribedImage(iconUrl + icon + "@2x.png",
                    Image.SourceType.url,
                    city,
                    String.format(Locale.ROOT, "%s · %.1f°C", description, temp),
                    false));
            add(new Description(
                    String.format(Locale.ROOT,
                            "Minimum temperature: %.1f°C\nMaximum temperature: %.1f°C\nWind speed: %.1fm/s",
                            tempMin, tempMax, windSpeed),
                    false
            ));
        }};
    }

    @Override
    public String getSpeechOutput() {
        return "Currently in " + city + " there is " + description;
    }
}
