package org.dicio.skill

import androidx.fragment.app.Fragment
import org.junit.Assert
import org.junit.Test

class SkillComponentTest {
    @Test
    fun testConstructorAndSetGet() {
        val skillInfo: SkillInfo = object : SkillInfo("id", 0, 0, 0, false) {
            override fun isAvailable(context: SkillContext): Boolean {
                return false
            }

            override fun build(context: SkillContext): Skill {
                throw NotImplementedError()
            }

            override val preferenceFragment: Fragment?
                get() = null
        }

        val skill: SkillComponent = object : SkillComponent() {}

        Assert.assertNull(skill.skillInfo)
        skill.skillInfo = skillInfo
        Assert.assertSame(skillInfo, skill.skillInfo)

        val skillContext = SkillContext()
        skill.setContext(skillContext)
        Assert.assertSame(skillContext, skill.ctx())
    }
}
