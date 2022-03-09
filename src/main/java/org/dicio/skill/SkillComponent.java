package org.dicio.skill;

import androidx.annotation.Nullable;

/**
 * Should be extended by all classes that aim to implement all, or a part, of a skill, so e.g. the
 * {@link Skill} class but also the various chain skill parts. This way the developer implementing a
 * skill always has skill context and info at its disposal.
 */
public abstract class SkillComponent {

    private SkillContext context;
    @Nullable
    private SkillInfo skillInfo;

    /**
     * @return the {@link SkillContext} object associated with this component, to be used to process
     *         user input, query information from the environment (e.g. get user contacts) and
     *         finally generate output
     * @implNote the name is like this because getContext would be too long
     */
    public SkillContext ctx() {
        return context;
    }

    /**
     * @return the nullable {@link SkillInfo} object this component was built with, to be used to
     *         find details about this skill. There could be no associated skill info, for example
     *         if the skill component is only temporary. This could be the case e.g. for temporary
     *         skills created as return values for {@link
     *         org.dicio.skill.chain.OutputGenerator#nextSkills()}.
     */
    @Nullable
    public SkillInfo getSkillInfo() {
        return skillInfo;
    }

    /**
     * Called at build time, created mostly so as to prevent having to overload the constructor, but
     * it could also be used later if the whole skill context object has to change. Though, if
     * possible, prefer changing the objects inside the used {@link SkillContext} object instead of
     * replacing the whole skill context object with this function.
     *
     * @param context the {@link SkillContext} object this {@link Skill} is being created with
     * @see #ctx()
     */
    public void setContext(final SkillContext context) {
        this.context = context;
    }

    /**
     * Called at build time, created mostly so as to prevent having to overload the constructor, but
     * it could also be used later if the whole skill context object has to change (I cannot think
     * any such case though).
     *
     * @param skillInfo the {@link SkillInfo} object this {@link Skill} is being created with (using
     *                  {@link SkillInfo#build(SkillContext)}), or {@code null} if this skill is
     *                  not being built by a {@link SkillInfo}
     * @see #getSkillInfo()
     */
    public void setSkillInfo(@Nullable final SkillInfo skillInfo) {
        this.skillInfo = skillInfo;
    }
}
