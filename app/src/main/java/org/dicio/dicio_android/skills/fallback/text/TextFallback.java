package org.dicio.dicio_android.skills.fallback.text;

import android.content.Context;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.skills.fallback.FallbackSkill;
import org.dicio.dicio_android.output.graphical.GraphicalOutputDevice;
import org.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import org.dicio.dicio_android.output.speech.SpeechOutputDevice;

import java.util.List;
import java.util.Locale;

public class TextFallback implements FallbackSkill {

    @Override
    public void setInput(final String input,
                         final List<String> inputWords,
                         final List<String> normalizedWordKeys) {}

    @Override
    public void cleanup() {}

    @Override
    public void processInput(final Locale locale) {}

    @Override
    public void generateOutput(final Context context,
                               final SpeechOutputDevice speechOutputDevice,
                               final GraphicalOutputDevice graphicalOutputDevice) {

        final String noMatchString = context.getString(R.string.eval_no_match);
        speechOutputDevice.speak(noMatchString);
        graphicalOutputDevice.display(GraphicalOutputUtils.buildSubHeader(context, noMatchString));
    }
}
