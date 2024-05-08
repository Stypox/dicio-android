package org.dicio.skill

/**
 * Should be extended by all classes that aim to implement all, or a part, of a skill, so e.g. the
 * [Skill] class but also the various chain skill parts. This way the developer implementing a
 * skill always has skill context and info at its disposal.
 */
abstract class SkillComponent {
    private var context: SkillContext? = null

    /**
     * The nullable [SkillInfo] object this component was built with, to be used to
     * find details about this skill. There could be no associated skill info, for example
     * if the skill component is only temporary. This could be the case e.g. for temporary
     * skills created as return values for [org.dicio.skill.chain.OutputGenerator.nextSkills].
     *
     * The setter is called at build time, created mostly so as to prevent having to overload the
     * constructor, but it could also be used later if the whole skill context object has to change
     * (I cannot think any such case though). The setter should set the [SkillInfo] object this
     * [Skill] is being created with (using [SkillInfo.build]), or `null` if this skill is
     * not being built by a [SkillInfo].
     */
    open var skillInfo: SkillInfo? = null

    /**
     * @return the [SkillContext] object associated with this component, to be used to process
     * user input, query information from the environment (e.g. get user contacts) and
     * finally generate output
     * @implNote the name is like this because getContext would be too long
     */
    fun ctx(): SkillContext {
        return context!!
    }

    /**
     * Called at build time, created mostly so as to prevent having to overload the constructor, but
     * it could also be used later if the whole skill context object has to change. Though, if
     * possible, prefer changing the objects inside the used [SkillContext] object instead of
     * replacing the whole skill context object with this function.
     *
     * @param context the [SkillContext] object this [Skill] is being created with
     * @see .ctx
     */
    open fun setContext(context: SkillContext) {
        this.context = context
    }
}
