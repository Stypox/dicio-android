package org.dicio.skill;

import android.content.Context;
import android.content.SharedPreferences;

import org.dicio.skill.chain.InputRecognizer;
import org.dicio.skill.chain.IntermediateProcessor;
import org.dicio.skill.chain.OutputGenerator;
import org.dicio.skill.output.GraphicalOutputDevice;
import org.dicio.skill.output.SpeechOutputDevice;
import org.dicio.skill.util.CleanableUp;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public interface Skill extends CleanableUp {

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
     * SharedPreferences, Locale, SpeechOutputDevice, GraphicalOutputDevice)} is called everything
     * is ready.
     * @see IntermediateProcessor#process(Object, Context, SharedPreferences, Locale)
     * @param context the Android context, useful for example to get preferences, resources, etc.
     * @param preferences the Android preferences, useful for user customization, also see {@link
     *                    SkillInfo#hasPreferences()} and {@link SkillInfo#getPreferenceFragment()}
     * @param locale the current user locale, useful for example to customize web requests to get
     *               the correct language or country
     */
    void processInput(Context context, SharedPreferences preferences, Locale locale)
            throws Exception;

    /**
     * @see OutputGenerator#generate(Object, Context, SharedPreferences, Locale, SpeechOutputDevice,
     * GraphicalOutputDevice)
     */
    void generateOutput(Context context,
                        SharedPreferences preferences,
                        Locale locale,
                        SpeechOutputDevice speechOutputDevice,
                        GraphicalOutputDevice graphicalOutputDevice);

    /**
     * To prevent excessive memory usage, release all temporary resources and set to {@code null}
     * all temporary variables used while calculating the score, getting the result or generating
     * output.
     */
    @Override
    void cleanup();

    /**
     * @see OutputGenerator#nextSkills()
     */
    default List<Skill> nextSkills() {
        return Collections.emptyList();
    }
}
