package org.dicio.skill;

import androidx.annotation.Nullable;

/**
 * Should be extended by all classes that aim to implement all, or a part, of a skill, so e.g. the
 * {@link Skill} class but also the various chain skill parts.
 */
public abstract class SkillComponent {

    private final SkillContext context;
    @Nullable
    private final SkillInfo skillInfo;

    /**
     * @param context the {@link SkillContext} object this {@link Skill} is being created with,
     *                see {@link #ctx()}
     * @param skillInfo the {@link SkillInfo} object this {@link Skill} is being created with (using
     *                  {@link SkillInfo#build(SkillContext)}), or {@code null} if this skill is
     *                  not being built by a {@link SkillInfo}, see {@link #getSkillInfo()}
     */
    public SkillComponent(final SkillContext context, @Nullable final SkillInfo skillInfo) {
        this.context = context;
        this.skillInfo = skillInfo;
    }

    /**
     * @return the {@link SkillContext} object passed to the constructor {@link
     *         #SkillComponent(SkillContext, SkillInfo)}, to be used to process user input, query
     *         information from the environment (e.g. get user contacts) and finally generate output
     * @implNote the name is like this because getContext would be too long
     */
    public SkillContext ctx() {
        return context;
    }

    /**
     * @return the nullable {@link SkillInfo} object passed to the constructor {@link
     *         #SkillComponent(SkillContext, SkillInfo)}, to be used to find details about this
     *         skill
     */
    @Nullable
    public SkillInfo getSkillInfo() {
        return skillInfo;
    }
}
