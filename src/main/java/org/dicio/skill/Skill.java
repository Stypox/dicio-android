package org.dicio.skill;

import org.dicio.skill.chain.InputRecognizer;
import org.dicio.skill.chain.IntermediateProcessor;
import org.dicio.skill.chain.OutputGenerator;
import org.dicio.skill.util.CleanableUp;

import java.util.Collections;
import java.util.List;

/**
 * A skill is the component that scores input, processes it and finally generates output. Take a
 * look at {@link org.dicio.skill.chain.ChainSkill} for a class that separates these three things.
 */
public abstract class Skill extends SkillComponent implements CleanableUp {

    /**
     * @see InputRecognizer#specificity()
     */
    public abstract InputRecognizer.Specificity specificity();

    /**
     * @see InputRecognizer#setInput(String, List, List)
     */
    public abstract void setInput(String input,
                                  List<String> inputWords,
                                  List<String> normalizedWordKeys);

    /**
     * @see InputRecognizer#score()
     */
    public abstract float score();

    /**
     * This will be called if this skill was deemed as the best one which could provide output for
     * what the user requested and it should therefore process the input previously received with
     * {@link #setInput(String, List, List)}, so that when {@link #generateOutput()} is called
     * everything is ready.
     *
     * @see IntermediateProcessor#process(Object)
     */
    public abstract void processInput() throws Exception;

    /**
     * @see OutputGenerator#generate(Object)
     */
    public abstract void generateOutput();

    /**
     * To prevent excessive memory usage, release all temporary resources and set to {@code null}
     * all temporary variables used while calculating the score, getting the result or generating
     * output.
     */
    @Override
    public abstract void cleanup();

    /**
     * @see OutputGenerator#nextSkills()
     */
    public List<Skill> nextSkills() {
        return Collections.emptyList();
    }
}
