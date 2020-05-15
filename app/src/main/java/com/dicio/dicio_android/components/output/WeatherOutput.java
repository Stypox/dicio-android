package com.dicio.dicio_android.components.output;

import android.content.Context;

import com.dicio.dicio_android.components.processing.WeatherProcessor;
import com.dicio.dicio_android.output.OutputGenerator;
import com.dicio.dicio_android.output.graphical.GraphicalOutputDevice;
import com.dicio.dicio_android.output.speech.SpeechOutputDevice;

public class WeatherOutput implements OutputGenerator<WeatherProcessor.Result> {

    private static final String iconUrl = "http://openweathermap.org/img/wn/";

    @Override
    public void generate(WeatherProcessor.Result data,
                         Context context,
                         SpeechOutputDevice speechOutputDevice,
                         GraphicalOutputDevice graphicalOutputDevice) {
        if (data.failed) {
            speechOutputDevice.speak("I could not find city " + data.city);
        } else {
            speechOutputDevice.speak("Currently in " + data.city
                    + " there is " + data.description);
        }
    }
}
