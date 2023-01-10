package org.dicio.skill.util;

import org.dicio.skill.Skill;

import java.util.List;

public interface NextSkills {

    /**
     * @return a list of skills to use for the next user input. This is needed to allow providing a
     *         stateful interaction with a set of skills. If the list is empty, the current stateful
     *         conversation is interrupted. This function will be called only once, after {@link
     *         Skill#generateOutput()} /
     *         {@link org.dicio.skill.chain.OutputGenerator#generate(Object)}, so that the
     *         calculated data can be used to choose what to do.
     *         There is no need to call {@link Skill#setContext(org.dicio.skill.SkillContext)} and
     *         {@link Skill#setSkillInfo(org.dicio.skill.SkillInfo)} on the returned skills, as that
     *         has to be done by the caller. By default this method returns the last list fed to
     *         {@link #setNextSkills(List)} during the last {@link Skill#generateOutput()} /
     *         {@link org.dicio.skill.chain.OutputGenerator#generate(Object)} phase (and
     *         then resets that list to an empty list), or an empty list if no list was fed.
     */
    List<Skill> nextSkills();

    /**
     * @param skills the list of skills to return on the next call to {@link #nextSkills()}. Clears
     *               any previously set list of skills. There is no need to call {@link
     *               Skill#setContext(org.dicio.skill.SkillContext)} and {@link
     *               Skill#setSkillInfo(org.dicio.skill.SkillInfo)} on the skills, as that has to be
     *               done by the caller of {@link #nextSkills()}.
     */
    void setNextSkills(final List<Skill> skills);
}
