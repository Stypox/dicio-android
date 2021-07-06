package org.dicio.dicio_android.skills.weather;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import org.dicio.skill.SkillContext;
import org.dicio.skill.chain.OutputGenerator;
import org.dicio.skill.output.GraphicalOutputDevice;
import org.dicio.skill.output.SpeechOutputDevice;

public class WeatherOutput implements OutputGenerator<WeatherOutput.Data> {

    public static class Data {
        public boolean failed = false;
        public String city, description, iconUrl;
        public double temp, tempMin, tempMax, windSpeed;
    }


    @Override
    public void generate(final Data data,
                         final SkillContext context,
                         final SpeechOutputDevice speechOutputDevice,
                         final GraphicalOutputDevice graphicalOutputDevice) {

        if (data.failed) {
            final String message = context.getAndroidContext().getString(
                    R.string.skill_weather_could_not_find_city, data.city);
            speechOutputDevice.speak(message);
            graphicalOutputDevice.display(GraphicalOutputUtils.buildSubHeader(
                    context.getAndroidContext(), message));

        } else {
            speechOutputDevice.speak(context.getAndroidContext().getString(
                    R.string.skill_weather_in_city_there_is_description,
                    data.city, data.description));

            final View weatherView = GraphicalOutputUtils.inflate(context.getAndroidContext(),
                    R.layout.skill_weather);
            Picasso.get().load(data.iconUrl).into(
                    (ImageView) weatherView.findViewById(R.id.image));
            ((TextView) weatherView.findViewById(R.id.city)).setText(data.city);
            ((TextView) weatherView.findViewById(R.id.basicInfo)).setText(
                    context.getAndroidContext().getString(
                            R.string.skill_weather_description_temperature,
                            data.description, data.temp));
            ((TextView) weatherView.findViewById(R.id.advancedInfo)).setText(
                    context.getAndroidContext().getString(R.string.skill_weather_min_max_wind,
                            data.tempMin, data.tempMax, data.windSpeed));
            graphicalOutputDevice.display(weatherView);
        }
    }

    @Override
    public void cleanup() {
    }
}
