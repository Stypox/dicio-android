package org.dicio.skill;

import static org.junit.Assert.assertTrue;

import org.dicio.skill.chain.InputRecognizer;
import org.junit.Test;

import java.util.List;

public class SkillTest {

    public static Skill buildEmptySkill() {
        return new Skill() {
            @Override public InputRecognizer.Specificity specificity() { return null; }
            @Override public void setInput(final String input, final List<String> inputWords,
                                           final List<String> normalizedWordKeys) {}
            @Override public float score() { return 0; }
            @Override public void processInput() {}
            @Override public void generateOutput() {}
            @Override public void cleanup() {}
        };
    }

    @Test
    public void testConstructor() {
        final Skill skill = buildEmptySkill();

        assertTrue(skill.nextSkills().isEmpty());
    }
}
