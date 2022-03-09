package org.dicio.skill.chain;

import androidx.annotation.Nullable;

import org.dicio.skill.Skill;
import org.dicio.skill.SkillContext;
import org.dicio.skill.SkillInfo;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ChainSkill extends Skill {

    public static class Builder {
        final ChainSkill chainSkill = new ChainSkill();

        public Builder recognize(final InputRecognizer<?> inputRecognizer) {
            if (chainSkill.inputRecognizer != null) {
                throw new IllegalArgumentException("recognize() can only be called once");
            }

            chainSkill.inputRecognizer = inputRecognizer;
            return this;
        }

        public Builder process(final IntermediateProcessor<?, ?> intermediateProcessor) {
            if (chainSkill.inputRecognizer == null) {
                throw new IllegalArgumentException("process() can't be called before recognize()");
            }

            chainSkill.intermediateProcessors.add(intermediateProcessor);
            return this;
        }

        public ChainSkill output(final OutputGenerator<?> outputGenerator) {
            if (chainSkill.inputRecognizer == null) {
                throw new IllegalArgumentException("output() can't be called before recognize()");
            }

            chainSkill.outputGenerator = outputGenerator;
            return chainSkill;
        }
    }


    private InputRecognizer inputRecognizer;
    private final List<IntermediateProcessor> intermediateProcessors = new ArrayList<>();
    private OutputGenerator outputGenerator;
    private Object lastResult;


    @Override
    public InputRecognizer.Specificity specificity() {
        return inputRecognizer.specificity();
    }

    @Override
    public void setInput(final String input,
                         final List<String> inputWords,
                         final List<String> normalizedWordKeys) {
        inputRecognizer.setInput(input, inputWords, normalizedWordKeys);
    }

    @Override
    public float score() {
        return inputRecognizer.score();
    }

    @Override
    public void cleanup() {
        inputRecognizer.cleanup();
        outputGenerator.cleanup();
        lastResult = null;
    }


    @Override
    public void processInput()
            throws Exception {
        lastResult = inputRecognizer.getResult();
        for (final IntermediateProcessor intermediateProcessor : intermediateProcessors) {
            lastResult = intermediateProcessor.process(lastResult);
        }
    }

    @Override
    public void generateOutput() {
        outputGenerator.generate(lastResult);
    }

    /**
     * @see OutputGenerator#nextSkills()
     */
    @Override
    public List<Skill> nextSkills() {
        return outputGenerator.nextSkills();
    }

    /**
     * Also sets the context to all of this chain skill sub-components
     * @param context the {@link SkillContext} object this {@link Skill} is being created with
     */
    @Override
    public void setContext(final SkillContext context) {
        super.setContext(context);
        inputRecognizer.setContext(context);
        for (final IntermediateProcessor intermediateProcessor : intermediateProcessors) {
            intermediateProcessor.setContext(context);
        }
        outputGenerator.setContext(context);
    }

    /**
     * Also sets the skill info to all of this chain skill sub-components
     * @param skillInfo the {@link SkillInfo} object this {@link Skill} is being created with (using
     *                  {@link SkillInfo#build(SkillContext)}), or {@code null} if this skill is
     *                  not being built by a {@link SkillInfo}
     */
    @Override
    public void setSkillInfo(@Nullable final SkillInfo skillInfo) {
        super.setSkillInfo(skillInfo);
        inputRecognizer.setSkillInfo(skillInfo);
        for (final IntermediateProcessor intermediateProcessor : intermediateProcessors) {
            intermediateProcessor.setSkillInfo(skillInfo);
        }
        outputGenerator.setSkillInfo(skillInfo);
    }
}
