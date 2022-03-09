package org.dicio.dicio_android.skills.weather;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import org.dicio.skill.chain.OutputGenerator;

public class WeatherOutput extends OutputGenerator<WeatherOutput.Data> {

    public static class Data {
        public boolean failed = false;
        public String city, description, iconUrl;
        public double temp, tempMin, tempMax, windSpeed;
    }


    @Override
    public void generate(final Data data) {

        if (data.failed) {
            final String message = ctx().android().getString(
                    R.string.skill_weather_could_not_find_city, data.city);
            ctx().getSpeechOutputDevice().speak(message);
            ctx().getGraphicalOutputDevice().display(GraphicalOutputUtils.buildSubHeader(
                    ctx().android(), message));

        } else {
            ctx().getSpeechOutputDevice().speak(ctx().android().getString(
                    R.string.skill_weather_in_city_there_is_description,
                    data.city, data.description));

            final View weatherView = GraphicalOutputUtils.inflate(ctx().android(),
                    R.layout.skill_weather);
            Picasso.get().load(data.iconUrl).into(
                    (ImageView) weatherView.findViewById(R.id.image));
            ((TextView) weatherView.findViewById(R.id.city)).setText(data.city);
            ((TextView) weatherView.findViewById(R.id.basicInfo)).setText(
                    ctx().android().getString(
                            R.string.skill_weather_description_temperature,
                            data.description, data.temp));
            ((TextView) weatherView.findViewById(R.id.advancedInfo)).setText(
                    ctx().android().getString(R.string.skill_weather_min_max_wind,
                            data.tempMin, data.tempMax, data.windSpeed));
            ctx().getGraphicalOutputDevice().display(weatherView);
        }
    }

    @Override
    public void cleanup() {}
}
