package com.dicio.dicio_android.components.fallback;

import android.content.Context;

import com.dicio.dicio_android.R;
import com.dicio.dicio_android.output.graphical.GraphicalOutputDevice;
import com.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import com.dicio.dicio_android.output.speech.SpeechOutputDevice;

import java.util.List;

public class TextFallbackComponent implements FallbackComponent {

    @Override
    public void setInput(List<String> words) {}

    @Override
    public void processInput() {}

    @Override
    public void generateOutput(Context context,
                               SpeechOutputDevice speechOutputDevice,
                               GraphicalOutputDevice graphicalOutputDevice) {

        speechOutputDevice.speak(context.getString(R.string.eval_speech_no_match));
        graphicalOutputDevice.display(GraphicalOutputUtils.buildHeader(context,
                context.getString(R.string.eval_header_no_match)));
    }
}
