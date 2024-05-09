package org.dicio.skill

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

/**
 * Constructor for [SkillInfo], providing basic information about a skill
 * @param id a unique identifier for this skill (different from that of all other skills)
 * @param nameResource the Android string resource containing the skill name to show to the user
 * (e.g. "Weather")
 * @param sentenceExampleResource the Android string resource containing an example of the usage
 * of this skill to show to the user (e.g. "What's the
 * weather?"). Should be 0 if this is a fallback skill.
 * @param iconResource the Android drawable resource containing the skill icon to show to the
 * user (e.g. an icon with sun and clouds representing weather)
 * @param hasPreferences whether this skill has preferences. If this is false, then
 * [.getPreferenceFragment] is assumed to return `null`,
 * otherwise it has to return a non-null preference fragment.
 */
abstract class SkillInfo(
    /**
     * @return a unique identifier for this skill (different from that of all other skills)
     */
    val id: String,

    /**
     * @return the Android string resource containing the skill name to show to the user (e.g.
     * "Weather")
     */
    @field:StringRes @get:StringRes @param:StringRes val nameResource: Int,

    /**
     * @return the Android string resource containing an example of the usage of this skill to show
     * to the user (e.g. "What's the weather?")
     */
    @field:StringRes @get:StringRes @param:StringRes val sentenceExampleResource: Int,

    /**
     * @return the Android drawable resource containing the skill icon to show to the user (e.g. an
     * icon with sun and clouds representing weather)
     */
    @field:DrawableRes @get:DrawableRes @param:DrawableRes val iconResource: Int,

    /**
     * @return whether this skill has preferences. [.getPreferenceFragment] returns
     * `null` only if this is false.
     */
    val hasPreferences: Boolean
) {
    /**
     * Use this method to signal that the skill is not available in case, for example, the user
     * locale is not supported.
     * @param context the skill context with useful resources, see [SkillContext]
     * @return whether this skill can be used with the current system configuration or not
     */
    abstract fun isAvailable(context: SkillContext): Boolean

    /**
     * Builds an instance of the [Skill] this [SkillInfo] object represents. There is no need to
     * call [Skill.setContext] and set [Skill.skillInfo] on the built skill, as that has to be done
     * by the caller.
     * @param context the skill context with useful resources, see [SkillContext]
     * @return a skill
     */
    abstract fun build(context: SkillContext): Skill

    /**
     * Provides a custom preferences screen for this skill, allowing the user to customize it to
     * their needs.
     * @return `null` if [.hasPreferences] is `false`, otherwise an instance of
     * a preference fragment. Please note that the `class` of the returned object
     * should be `public static`, otherwise Android libraries complain.
     */
    abstract val preferenceFragment: Fragment?

    /**
     * Provides all of the permissions this skill needs in order to run. For example, the telephone
     * skill needs the `CALL_PHONE` and `READ_CONTACTS` permissions to run. The
     * permissions expressed here will be requested to the user when the skill is first used, or
     * via settings. A skill should therefore be able to be built with [.build]
     * without any permission, and a skill's input scoring (i.e. [Skill.score] and the
     * related methods) should also work without permissions.
     * @return all of the special permissions this skill requires, or an empty list if no special
     * permissions are needed
     */
    open val neededPermissions: List<String> = listOf()
}
