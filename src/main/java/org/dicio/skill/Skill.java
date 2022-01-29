package org.dicio.skill;

import androidx.annotation.Nullable;

import org.dicio.skill.chain.InputRecognizer;
import org.dicio.skill.chain.IntermediateProcessor;
import org.dicio.skill.chain.OutputGenerator;
import org.dicio.skill.output.GraphicalOutputDevice;
import org.dicio.skill.output.SpeechOutputDevice;
import org.dicio.skill.util.CleanableUp;

import java.util.Collections;
import java.util.List;

public abstract class Skill implements CleanableUp {

    @Nullable
    private final SkillInfo skillInfo;

    /**
     * @param skillInfo the {@link SkillInfo} object this {@link Skill} is being created with (using
     *                  {@link SkillInfo#build(SkillContext)}), or {@code null} if this skill is
     *                  not being built by a {@link SkillInfo}
     */
    public Skill(@Nullable final SkillInfo skillInfo) {
        this.skillInfo = skillInfo;
    }

    /**
     * @return the {@link SkillInfo} object passed to the constructor {@link #Skill(SkillInfo)}
     */
    @Nullable
    public SkillInfo getSkillInfo() {
        return skillInfo;
    }


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
     * {@link #setInput(String, List, List)}, so that when {@link #generateOutput(SkillContext,
     * SpeechOutputDevice, GraphicalOutputDevice)} is called everything
     * is ready.
     *
     * @see IntermediateProcessor#process(Object, SkillContext)
     * @param context the skill context with useful resources, see {@link SkillContext}
     */
    public abstract void processInput(SkillContext context) throws Exception;

    /**
     * @see OutputGenerator#generate(Object, SkillContext, SpeechOutputDevice,
     * GraphicalOutputDevice)
     */
    public abstract void generateOutput(SkillContext context,
                                        SpeechOutputDevice speechOutputDevice,
                                        GraphicalOutputDevice graphicalOutputDevice);

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
