package org.dicio.skill;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class SkillInfoTest {
    @Test
    public void testConstructorAndGetters() {
        final SkillInfo skillInfo = new SkillInfo("testId", 11, 222, 0, true) {
            @Override public boolean isAvailable(final SkillContext context) { return false; }
            @Override public Skill build(final SkillContext context) { return null; }
            @Nullable @Override public Fragment getPreferenceFragment() { return null; }
        };

        assertSame("testId", skillInfo.getId());
        assertEquals(11, skillInfo.getNameResource());
        assertEquals(222, skillInfo.getSentenceExampleResource());
        assertEquals(0, skillInfo.getIconResource());
        assertTrue(skillInfo.hasPreferences());
    }

    @Test
    public void testGetNeededPermissions() {
        final SkillInfo skillInfo = new SkillInfo("", 0, 0, 0, false) {
            @Override public boolean isAvailable(final SkillContext context) { return false; }
            @Override public Skill build(final SkillContext context) { return null; }
            @Nullable @Override public Fragment getPreferenceFragment() { return null; }
        };

        final List<String> permissions = skillInfo.getNeededPermissions();
        assertNotNull(permissions);
        assertTrue("Default permissions are not empty: " + Arrays.toString(permissions.toArray()),
                permissions.isEmpty());
    }
}
