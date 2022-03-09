package org.dicio.skill;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.junit.Test;

public class SkillComponentTest {
    @Test
    public void testConstructorAndSetGet() {
        final SkillInfo skillInfo = new SkillInfo("id", 0, 0, 0, false) {
            @Override public boolean isAvailable(final SkillContext context) { return false; }
            @Override public Skill build(final SkillContext context) { return null; }
            @Nullable
            @Override public Fragment getPreferenceFragment() { return null; }
        };

        final SkillComponent skill = new SkillComponent() {};

        assertNull(skill.getSkillInfo());
        skill.setSkillInfo(skillInfo);
        assertSame(skillInfo, skill.getSkillInfo());

        final SkillContext skillContext = new SkillContext();
        skill.setContext(skillContext);
        assertSame(skillContext, skill.ctx());
    }
}
