package org.dicio.skill.util

import org.dicio.skill.Skill

interface NextSkills {
    /**
     * @return a list of skills to use for the next user input. This is needed to allow providing a
     * stateful interaction with a set of skills. If the list is empty, the current stateful
     * conversation is interrupted. This function will be called only once, after
     * [Skill.generateOutput] / [org.dicio.skill.chain.OutputGenerator.generate], so that the
     * calculated data can be used to choose what to do.
     * There is no need to call [Skill.setContext] and
     * [Skill.setSkillInfo] on the returned skills, as that
     * has to be done by the caller. By default this method returns the last list fed to
     * [.setNextSkills] during the last [Skill.generateOutput] /
     * [org.dicio.skill.chain.OutputGenerator.generate] phase (and
     * then resets that list to an empty list), or an empty list if no list was fed.
     */
    fun nextSkills(): List<Skill>

    /**
     * @param skills the list of skills to return on the next call to [.nextSkills]. Clears
     * any previously set list of skills. There is no need to call [Skill.setContext] and
     * [Skill.setSkillInfo] on the skills, as that has to be
     * done by the caller of [.nextSkills].
     */
    fun setNextSkills(skills: List<Skill>)
}
