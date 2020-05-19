package com.dicio.dicio_android.components.output;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dicio.dicio_android.R;
import com.dicio.dicio_android.output.OutputGenerator;
import com.dicio.dicio_android.output.graphical.GraphicalOutputDevice;
import com.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import com.dicio.dicio_android.output.speech.SpeechOutputDevice;
import com.squareup.picasso.Picasso;

import java.util.Locale;

public class WeatherOutput implements OutputGenerator<WeatherOutput.Data> {

    private static final String iconUrl = "http://openweathermap.org/img/wn/";

    public static class Data {
        public boolean failed = false;
        public String city, description, icon;
        public double temp, tempMin, tempMax, windSpeed;
    }


    @Override
    public void generate(Data data,
                         Context context,
                         SpeechOutputDevice speechOutputDevice,
                         GraphicalOutputDevice graphicalOutputDevice) {
        if (data.failed) {
            final String message = "I could not find city " + data.city;
            speechOutputDevice.speak(message);
            graphicalOutputDevice.display(GraphicalOutputUtils.buildHeader(context, message));

        } else {
            speechOutputDevice.speak("Currently in " + data.city
                    + " there is " + data.description);

            View weatherView = GraphicalOutputUtils.inflate(context, R.layout.component_weather);
            Picasso.get().load(iconUrl + data.icon + "@2x.png").into(
                    (ImageView) weatherView.findViewById(R.id.image));
            ((TextView) weatherView.findViewById(R.id.city)).setText(data.city);
            ((TextView) weatherView.findViewById(R.id.basicInfo)).setText(
                    String.format(Locale.getDefault(),
                            "%s · %.1f°C",
                            data.description, data.temp));
            ((TextView) weatherView.findViewById(R.id.advancedInfo)).setText(
                    String.format(Locale.getDefault(),
                            "Minimum temperature: %.1f°C\nMaximum temperature: %.1f°C\nWind speed: %.1fm/s",
                            data.tempMin, data.tempMax, data.windSpeed));
            graphicalOutputDevice.display(weatherView);
        }
    }
}
