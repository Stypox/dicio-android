package org.stypox.dicio.di

import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillOutput

interface SkillContextInternal : SkillContext {
    // allows modifying this value
    override var previousOutput: SkillOutput?
}