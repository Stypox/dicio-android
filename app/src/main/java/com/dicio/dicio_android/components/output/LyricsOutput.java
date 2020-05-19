package com.dicio.dicio_android.components.output;

import android.content.Context;

import com.dicio.dicio_android.output.OutputGenerator;
import com.dicio.dicio_android.output.graphical.GraphicalOutputDevice;
import com.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import com.dicio.dicio_android.output.speech.SpeechOutputDevice;

public class LyricsOutput implements OutputGenerator<LyricsOutput.Data> {

    public static class Data {
        public boolean failed = false;
        public String artist, title, lyrics;
    }


    @Override
    public void generate(Data data,
                         Context context,
                         SpeechOutputDevice speechOutputDevice,
                         GraphicalOutputDevice graphicalOutputDevice) {

        if (data.failed) {
            final String message = "Unable to find song " + data.title;
            speechOutputDevice.speak(message);
            graphicalOutputDevice.display(GraphicalOutputUtils.buildHeader(context, message));

        } else {
            speechOutputDevice.speak(data.title + " by " + data.artist);
            graphicalOutputDevice.display(
                    GraphicalOutputUtils.buildDescription(context, data.lyrics));
        }
    }
}
