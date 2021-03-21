package org.dicio.skill;

import org.dicio.skill.chain.InputRecognizer;
import org.dicio.skill.chain.IntermediateProcessor;
import org.dicio.skill.chain.OutputGenerator;
import org.dicio.skill.output.GraphicalOutputDevice;
import org.dicio.skill.output.SpeechOutputDevice;
import org.dicio.skill.util.CleanableUp;

import java.util.Collections;
import java.util.List;

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
     * {@link #setInput(String, List, List)}, so that when {@link #generateOutput(SkillContext,
     * SpeechOutputDevice, GraphicalOutputDevice)} is called everything
     * is ready.
     *
     * @see IntermediateProcessor#process(Object, SkillContext)
     * @param context the skill context with useful resources, see {@link SkillContext}
     */
    void processInput(SkillContext context) throws Exception;

    /**
     * @see OutputGenerator#generate(Object, SkillContext, SpeechOutputDevice,
     * GraphicalOutputDevice)
     */
    void generateOutput(SkillContext context,
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
