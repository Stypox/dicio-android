package org.dicio.skill;

import android.content.Context;

import org.dicio.skill.output.GraphicalOutputDevice;
import org.dicio.skill.output.SpeechOutputDevice;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public interface Skill {

    /**
     * @see InputRecognizer#specificity()
     */
    InputRecognizer.Specificity specificity();

    /**
     * @see InputRecognizer#setInput(String, List, List)
     */
    void setInput(String input, List<String> inputWords, List<String> normalizedWordKeys);

    /**
     * @see InputRecognizer#score()
     */
    float score();

    /**
     * This will be called if this skill was deemed as the best one which could provide output for
     * what the user requested and it should therefore process the input previously received with
     * {@link #setInput(String, List, List)}, so that when {@link #generateOutput(Context,
     * SpeechOutputDevice, GraphicalOutputDevice)} is called everything is ready.
     * @see IntermediateProcessor#process(Object, Locale)
     * @param locale the locale to use, useful for example to customize web requests to get the
     *               correct language or country.
     */
    void processInput(Locale locale) throws Exception;

    /**
     * @see OutputGenerator#generate(Object, Context, SpeechOutputDevice, GraphicalOutputDevice)
     */
    void generateOutput(Context context,
                        SpeechOutputDevice speechOutputDevice,
                        GraphicalOutputDevice graphicalOutputDevice);

    /**
     * To prevent excessive memory usage, release all temporary resources and set to {@code null}
     * all temporary variables used while calculating the score, getting the result or generating
     * output.
     */
    void cleanup();

    /**
     * @see OutputGenerator#nextSkills()
     */
    default List<Skill> nextSkills() {
        return Collections.emptyList();
    }
}
