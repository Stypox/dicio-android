package org.dicio.skill

/**
 * Should be extended by all classes that aim to implement all, or a part, of a skill, so e.g. the
 * [Skill] class but also the various chain skill parts. This way the developer implementing a
 * skill always has skill context and info at its disposal.
 */
abstract class SkillComponent {
    private var context: SkillContext? = null

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
