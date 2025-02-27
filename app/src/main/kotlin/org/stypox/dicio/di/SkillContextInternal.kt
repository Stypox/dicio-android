package org.stypox.dicio.di

import org.dicio.skill.context.SkillContext

interface SkillContextInternal : SkillContext {
    // allows modifying this value
    override var previousInteractionWasFallback: Boolean
}