package org.dicio.skill.skill

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import org.dicio.skill.MockSkillInfo

class SkillInfoTest : StringSpec({
    "get needed permissions" {
        val skillInfo: SkillInfo = MockSkillInfo

        withClue("Needed permissions should be empty by default") {
            skillInfo.neededPermissions.shouldBeEmpty()
        }
    }
    "get needed secure settings" {
        val skillInfo: SkillInfo = MockSkillInfo

        withClue("Needed secure settings should be empty by default") {
            skillInfo.neededSecureSettings.shouldBeEmpty()
        }
    }
})
