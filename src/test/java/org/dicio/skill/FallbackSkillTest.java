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
    public void testConstructorAndGetSkillInfo() {
        final SkillInfo skillInfo = new SkillInfo("id", 0, 0, 0, false) {
            @Override public boolean isAvailable(final SkillContext context) { return false; }
            @Override public Skill build(final SkillContext context) { return null; }
            @Nullable @Override public Fragment getPreferenceFragment() { return null; }
        };

        final Skill skill = new FallbackSkill(null, skillInfo) {
            @Override public void setInput(final String input, final List<String> inputWords,
                    final List<String> normalizedWordKeys) {}
            @Override public void processInput() {}
            @Override public void generateOutput() {}
            @Override public void cleanup() {}
        };

        assertSame(skillInfo, skill.getSkillInfo());
    }

    @Test
    public void testScoreAndSpecificity() {
        final Skill skill = new FallbackSkill(null, null) {
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
