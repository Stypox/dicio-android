package org.dicio.skill;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.dicio.skill.chain.InputRecognizer;
import org.junit.Test;

import java.util.List;

public class SkillTest {

    @Test
    public void testConstructor() {
        new Skill() {
            @Override public InputRecognizer.Specificity specificity() { return null; }
            @Override public void setInput(final String input, final List<String> inputWords,
                    final List<String> normalizedWordKeys) {}
            @Override public float score() { return 0; }
            @Override public void processInput() {}
            @Override public void generateOutput() {}
            @Override public void cleanup() {}
        };
    }
}
