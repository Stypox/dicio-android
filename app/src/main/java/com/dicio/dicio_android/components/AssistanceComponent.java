package com.dicio.dicio_android.components;

import android.content.Context;

import com.dicio.component.InputRecognizer;
import com.dicio.dicio_android.output.graphical.GraphicalOutputDevice;
import com.dicio.dicio_android.output.speech.SpeechOutputDevice;

import java.util.Collections;
import java.util.List;

public interface AssistanceComponent {

    InputRecognizer.Specificity specificity();

    void setInput(List<String> words);

    float score();

    void processInput() throws Exception;

    void generateOutput(Context context,
                        SpeechOutputDevice speechOutputDevice,
                        GraphicalOutputDevice graphicalOutputDevice);

    default List<AssistanceComponent> nextAssistanceComponents() {
        return Collections.emptyList();
    }
}
