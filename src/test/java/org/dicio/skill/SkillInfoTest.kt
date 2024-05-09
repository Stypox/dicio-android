package org.dicio.skill

import androidx.fragment.app.Fragment
import org.junit.Assert
import org.junit.Test

class SkillInfoTest {
    @Test
    fun testConstructorAndGetters() {
        val skillInfo: SkillInfo = object : SkillInfo("testId", 11, 222, 0, true) {
            override fun isAvailable(context: SkillContext): Boolean {
                return false
            }

            override fun build(context: SkillContext): Skill {
                throw NotImplementedError()
            }

            override val preferenceFragment: Fragment?
                get() = null
        }

        Assert.assertSame("testId", skillInfo.id)
        Assert.assertEquals(11, skillInfo.nameResource.toLong())
        Assert.assertEquals(222, skillInfo.sentenceExampleResource.toLong())
        Assert.assertEquals(0, skillInfo.iconResource.toLong())
        Assert.assertTrue(skillInfo.hasPreferences)
    }

    @Test
    fun testGetNeededPermissions() {
        val skillInfo: SkillInfo = object : SkillInfo("", 0, 0, 0, false) {
            override fun isAvailable(context: SkillContext): Boolean {
                return false
            }

            override fun build(context: SkillContext): Skill {
                throw NotImplementedError()
            }

            override val preferenceFragment: Fragment?
                get() = null
        }

        val permissions = skillInfo.neededPermissions
        Assert.assertNotNull(permissions)
        Assert.assertTrue(
            "Default permissions are not empty: " + permissions.toTypedArray().contentToString(),
            permissions.isEmpty()
        )
    }
}
