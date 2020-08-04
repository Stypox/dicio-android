package org.dicio.dicio_android.components;

import android.content.Context;

import org.dicio.component.InputRecognizer;
import org.dicio.dicio_android.output.graphical.GraphicalOutputDevice;
import org.dicio.dicio_android.output.speech.SpeechOutputDevice;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public interface AssistanceComponent {

    InputRecognizer.Specificity specificity();

    void setInput(String input, List<String> inputWords);

    float score();

    void processInput(Locale locale) throws Exception;

    void generateOutput(Context context,
                        SpeechOutputDevice speechOutputDevice,
                        GraphicalOutputDevice graphicalOutputDevice);

    void cleanup();

    default List<AssistanceComponent> nextAssistanceComponents() {
        return Collections.emptyList();
    }
}
