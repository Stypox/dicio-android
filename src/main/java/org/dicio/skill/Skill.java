package org.dicio.skill;

import android.content.Context;

import org.dicio.skill.output.GraphicalOutputDevice;
import org.dicio.skill.output.SpeechOutputDevice;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public interface Skill {

    InputRecognizer.Specificity specificity();

    void setInput(String input, List<String> inputWords, List<String> normalizedWordKeys);

    float score();

    void processInput(Locale locale) throws Exception;

    void generateOutput(Context context,
                        SpeechOutputDevice speechOutputDevice,
                        GraphicalOutputDevice graphicalOutputDevice);

    /**
     * To prevent excessive memory usage, release all temporary resources and set to {@code null}
     * all temporary variables used while calculating the score, getting the result or generating
     * output.
     */
    void cleanup();

    default List<Skill> nextSkills() {
        return Collections.emptyList();
    }
}
