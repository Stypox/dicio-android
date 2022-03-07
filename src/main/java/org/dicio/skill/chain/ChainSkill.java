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
        final ChainSkill chainSkill;

        /**
         * @see Skill#Skill(SkillContext, SkillInfo)
         */
        public Builder(final SkillContext context, @Nullable final SkillInfo skillInfo) {
            this.chainSkill = new ChainSkill(context, skillInfo);
        }

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
    private final List<IntermediateProcessor> intermediateProcessors;
    private OutputGenerator outputGenerator;
    private Object lastResult;

    private ChainSkill(final SkillContext context, @Nullable final SkillInfo skillInfo) {
        super(context, skillInfo);
        intermediateProcessors = new ArrayList<>();
    }


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
}
