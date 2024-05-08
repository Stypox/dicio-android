package org.dicio.skill.chain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.dicio.skill.Skill;
import org.dicio.skill.SkillComponent;
import org.dicio.skill.SkillContext;
import org.dicio.skill.SkillInfo;
import org.junit.Test;

import java.util.List;

public class ChainSkillTest {

    private static final SkillInfo skillInfo = new SkillInfo("", 0, 0, 0, false) {
        @Override public boolean isAvailable(final SkillContext context) { return false; }
        @Override public Skill build(final SkillContext context) { return null; }
        @Nullable @Override public Fragment getPreferenceFragment() { return null; }
    };

    // the type parameters used here are just random: the chain skill does not check if they match

    private static final InputRecognizer<Integer> ir = new InputRecognizer<Integer>() {
        @Override public Specificity specificity() { return Specificity.HIGH; }
        @Override public void setInput(final String input, final List<String> inputWords,
                final List<String> normalizedInputWords) {}
        @Override public float score() { return 0; }
        @Override public Integer getResult() { return 8; }
        @Override public void cleanup() {}
    };

    private static final IntermediateProcessor<Integer, Double> ip1
            = new IntermediateProcessor<Integer, Double>() {
        @Override public Double process(final Integer data) { return data / 3.0; }
    };

    private static final IntermediateProcessor<Double, Float> ip2
            = new IntermediateProcessor<Double, Float>() {
        @Override public Float process(final Double data) { return (float)(2 * data); }
    };

    private static final StringBuilder generatedOutput = new StringBuilder();
    private static final OutputGenerator<Object> og
            = new OutputGenerator<Object>() {
        @Override
        public void generate(final Object data) {
            assertNotNull(data);
            generatedOutput.append(data.getClass()).append("-").append(data);
        }
        @Override public void cleanup() {}
    };

    private static void assertGeneratedOutput(final String expected, final Skill skill)
            throws Exception {
        // use static generatedOutput variable
        generatedOutput.setLength(0);
        skill.processInput();
        skill.generateOutput();
        assertEquals(expected, generatedOutput.toString());
    }

    private static void assertSetSkillInfo(final Skill skill, final SkillComponent... skillComponents) {
        skill.setSkillInfo(null);
        for (final SkillComponent skillComponent : skillComponents) {
            skillComponent.setSkillInfo(null);
        }

        skill.setSkillInfo(skillInfo);

        assertSame(skillInfo, skill.getSkillInfo());
        for (final SkillComponent skillComponent : skillComponents) {
            assertSame(skillInfo, skillComponent.getSkillInfo());
        }
    }


    @Test
    public void testBuildNoIntermediateProcessors() throws Exception {
        final Skill skill = new ChainSkill.Builder(ir)
                .output(og);

        assertSetSkillInfo(skill, ir, og);
        assertEquals(ir.specificity(), skill.specificity());
        assertGeneratedOutput(Integer.class + "-" + 8, skill);
    }

    @Test
    public void testBuildOneIntermediateProcessor() throws Exception {
        final Skill skill = new ChainSkill.Builder(ir)
                .process(ip1)
                .output(og);

        assertSetSkillInfo(skill, ir, ip1, og);
        assertEquals(ir.specificity(), skill.specificity());
        assertGeneratedOutput(Double.class + "-" + (8 / 3.0), skill);
    }

    @Test
    public void testBuildTwoIntermediateProcessors() throws Exception {
        final Skill skill = new ChainSkill.Builder(ir)
                .process(ip1)
                .process(ip2)
                .output(og);

        assertSetSkillInfo(skill, ir, ip1, ip2, og);
        assertEquals(ir.specificity(), skill.specificity());
        assertGeneratedOutput(Float.class + "-" + (float)(2 * (8 / 3.0)), skill);
    }
}
