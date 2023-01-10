package org.dicio.skill.chain;

import static org.dicio.skill.SkillTest.buildEmptySkill;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.dicio.skill.Skill;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OutputGeneratorTest {
    @Test
    public void testNextSkills() {
        final OutputGenerator<Void> og = new OutputGenerator<Void>() {
            @Override public void generate(final Void data) {}
        };

        final List<Skill> skills = Collections.unmodifiableList(
                Arrays.asList(buildEmptySkill(), buildEmptySkill()));
        assertTrue(og.nextSkills().isEmpty());

        og.setNextSkills(skills);
        assertSame(skills, og.nextSkills());
        assertTrue(og.nextSkills().isEmpty());
        assertTrue(og.nextSkills().isEmpty());

        og.setNextSkills(skills);
        assertSame(skills, og.nextSkills());
        assertTrue(og.nextSkills().isEmpty());

        og.setNextSkills(skills);
        og.cleanup();
        assertTrue(og.nextSkills().isEmpty());
    }
}
