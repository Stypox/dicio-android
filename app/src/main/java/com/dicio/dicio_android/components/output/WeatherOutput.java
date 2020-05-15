package com.dicio.dicio_android.components.output;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dicio.dicio_android.R;
import com.dicio.dicio_android.components.processing.WeatherProcessor;
import com.dicio.dicio_android.output.OutputGenerator;
import com.dicio.dicio_android.output.graphical.GraphicalOutputDevice;
import com.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import com.dicio.dicio_android.output.graphical.OutputContainerView;
import com.dicio.dicio_android.output.speech.SpeechOutputDevice;
import com.squareup.picasso.Picasso;

import java.util.Locale;

public class WeatherOutput implements OutputGenerator<WeatherProcessor.Result> {

    private static final String iconUrl = "http://openweathermap.org/img/wn/";

    @Override
    public void generate(WeatherProcessor.Result data,
                         Context context,
                         SpeechOutputDevice speechOutputDevice,
                         GraphicalOutputDevice graphicalOutputDevice) {
        OutputContainerView graphicalOutput = new OutputContainerView(context);

        if (data.failed) {
            speechOutputDevice.speak("I could not find city " + data.city);

            graphicalOutput.setContent(GraphicalOutputUtils.buildHeader(context,
                    "I could not find city " + data.city));
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
            graphicalOutput.setContent(weatherView);
        }

        graphicalOutputDevice.display(graphicalOutput);
    }
}
