package org.dicio.skill;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.dicio.skill.chain.InputRecognizer;
import org.junit.Test;

import java.util.List;

public class FallbackSkillTest {

    @Test
    public void testConstructor() {
        new FallbackSkill() {
            @Override public void setInput(final String input, final List<String> inputWords,
                    final List<String> normalizedWordKeys) {}
            @Override public void processInput() {}
            @Override public void generateOutput() {}
            @Override public void cleanup() {}
        };
    }

    @Test
    public void testScoreAndSpecificity() {
        final Skill skill = new FallbackSkill() {
            @Override public void setInput(final String input, final List<String> inputWords,
                    final List<String> normalizedWordKeys) {}
            @Override public void processInput() {}
            @Override public void generateOutput() {}
            @Override public void cleanup() {}
        };

        assertEquals(0.0f, skill.score(), 0.0f);
        assertSame(InputRecognizer.Specificity.low, skill.specificity());
    }
}
