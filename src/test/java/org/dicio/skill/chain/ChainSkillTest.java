package org.dicio.skill.chain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.dicio.skill.Skill;
import org.dicio.skill.SkillContext;
import org.dicio.skill.SkillInfo;
import org.dicio.skill.output.GraphicalOutputDevice;
import org.dicio.skill.output.SpeechOutputDevice;
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
        @Override public Specificity specificity() { return Specificity.high; }
        @Override public void setInput(final String input, final List<String> inputWords,
                final List<String> normalizedInputWords) {}
        @Override public float score() { return 0; }
        @Override public Integer getResult() { return 8; }
        @Override public void cleanup() {}
    };

    private static final IntermediateProcessor<Integer, Double> ip1 = (data, context) -> data / 3.0;

    private static final IntermediateProcessor<Double, Float> ip2
            = (data, context) -> (float) (2 * data);

    private static final OutputGenerator<Object> og = new OutputGenerator<Object>() {
        @Override
        public void generate(final Object data, final SkillContext context,
                final SpeechOutputDevice speechOutputDevice,
                final GraphicalOutputDevice graphicalOutputDevice) {
            assertNotNull(data);
            speechOutputDevice.speak(data.getClass() + "-" + data);
        }
        @Override public void cleanup() {}
    };

    private static void assertGeneratedOutput(final String expected, final Skill skill)
            throws Exception {
        final StringBuilder generatedOutput = new StringBuilder();

        skill.processInput(null);
        skill.generateOutput(null, new SpeechOutputDevice() {
            @Override
            public void speak(@NonNull final String speechOutput) {
                generatedOutput.append(speechOutput);
            }
            @Override public void stopSpeaking() {}
            @Override public boolean isSpeaking() { return false; }
            @Override public void runWhenFinishedSpeaking(Runnable runnable) {}
            @Override public void cleanup() {}
        }, null);

        assertEquals(expected, generatedOutput.toString());
    }


    @Test
    public void testBuildNoIntermediateProcessors() throws Exception {
        final Skill skill = new ChainSkill.Builder(skillInfo)
                .recognize(ir)
                .output(og);
        assertSame(skillInfo, skill.getSkillInfo());
        assertEquals(ir.specificity(), skill.specificity());
        assertGeneratedOutput(Integer.class + "-" + 8, skill);
    }

    @Test
    public void testBuildOneIntermediateProcessor() throws Exception {
        final Skill skill = new ChainSkill.Builder(skillInfo)
                .recognize(ir)
                .process(ip1)
                .output(og);
        assertSame(skillInfo, skill.getSkillInfo());
        assertEquals(ir.specificity(), skill.specificity());
        assertGeneratedOutput(Double.class + "-" + (8 / 3.0), skill);
    }

    @Test
    public void testBuildTwoIntermediateProcessors() throws Exception {
        final Skill skill = new ChainSkill.Builder(skillInfo)
                .recognize(ir)
                .process(ip1)
                .process(ip2)
                .output(og);
        assertSame(skillInfo, skill.getSkillInfo());
        assertEquals(ir.specificity(), skill.specificity());
        assertGeneratedOutput(Float.class + "-" + (float)(2 * (8 / 3.0)), skill);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoInputRecognizerProcess() {
        new ChainSkill.Builder(skillInfo).process(ip1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoInputRecognizerOutput() {
        new ChainSkill.Builder(skillInfo).output(og);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTwoInputRecognizers() {
        new ChainSkill.Builder(skillInfo).recognize(ir).recognize(ir);
    }
}
