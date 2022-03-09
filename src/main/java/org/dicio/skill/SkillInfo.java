package org.dicio.skill;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import java.util.Collections;
import java.util.List;

public abstract class SkillInfo {

    private final String id;
    @StringRes private final int nameResource;
    @StringRes private final int sentenceExampleResource;
    @DrawableRes private final int iconResource;
    private final boolean hasPreferences;

    /**
     * Constructor for {@link SkillInfo}, providing basic information about a skill
     * @param id a unique identifier for this skill (different from that of all other skills)
     * @param nameResource the Android string resource containing the skill name to show to the user
     *                     (e.g. "Weather")
     * @param sentenceExampleResource the Android string resource containing an example of the usage
     *                                of this skill to show to the user (e.g. "What's the
     *                                weather?"). Should be 0 if this is a fallback skill.
     * @param iconResource the Android drawable resource containing the skill icon to show to the
     *                     user (e.g. an icon with sun and clouds representing weather)
     * @param hasPreferences whether this skill has preferences. If this is false, then
     *                       {@link #getPreferenceFragment()} is assumed to return {@code null},
     *                       otherwise it has to return a non-null preference fragment.
     */
    public SkillInfo(final String id,
                     @StringRes final int nameResource,
                     @StringRes final int sentenceExampleResource,
                     @DrawableRes final int iconResource,
                     final boolean hasPreferences) {
        this.id = id;
        this.nameResource = nameResource;
        this.sentenceExampleResource = sentenceExampleResource;
        this.iconResource = iconResource;
        this.hasPreferences = hasPreferences;
    }

    /**
     * @return a unique identifier for this skill (different from that of all other skills)
     */
    public final String getId() {
        return id;
    }

    /**
     * @return the Android string resource containing the skill name to show to the user (e.g.
     *         "Weather")
     */
    @StringRes
    public final int getNameResource() {
        return nameResource;
    }

    /**
     * @return the Android string resource containing an example of the usage of this skill to show
     *         to the user (e.g. "What's the weather?")
     */
    @StringRes
    public int getSentenceExampleResource() {
        return sentenceExampleResource;
    }

    /**
     * @return the Android drawable resource containing the skill icon to show to the user (e.g. an
     *         icon with sun and clouds representing weather)
     */
    @DrawableRes
    public int getIconResource() {
        return iconResource;
    }

    /**
     * @return whether this skill has preferences. {@link #getPreferenceFragment()} returns
     *         {@code null} only if this is false.
     */
    public final boolean hasPreferences() {
        return hasPreferences;
    }


    /**
     * Use this method to signal that the skill is not available in case, for example, the user
     * locale is not supported.
     * @param context the skill context with useful resources, see {@link SkillContext}
     * @return whether this skill can be used with the current system configuation or not
     */
    public abstract boolean isAvailable(SkillContext context);

    /**
     * Builds an instance of the {@link Skill} this {@link SkillInfo} object represents. There is no
     * need to call {@link Skill#setContext(SkillContext)} and {@link Skill#setSkillInfo(SkillInfo)}
     * on the built skill, as that has to be done by the caller.
     * @param context the skill context with useful resources, see {@link SkillContext}
     * @return a skill
     */
    public abstract Skill build(SkillContext context);

    /**
     * Provides a custom preferences screen for this skill, allowing the user to customize it to
     * their needs.
     * @return {@code null} if {@link #hasPreferences()} is {@code false}, otherwise an instance of
     *         a preference fragment. Please note that the {@code class} of the returned object
     *         should be {@code public static}, otherwise Android libraries complain.
     */
    @Nullable
    public abstract Fragment getPreferenceFragment();

    /**
     * Provides all of the permissions this skill needs in order to run. For example, the telephone
     * skill needs the {@code CALL_PHONE} and {@code READ_CONTACTS} permissions to run. The
     * permissions expressed here will be requested to the user when the skill is first used, or
     * via settings. A skill should therefore be able to be built with {@link #build(SkillContext)}
     * without any permission, and a skill's input scoring (i.e. {@link Skill#score()} and the
     * related methods) should also work without permissions.
     * @return all of the special permissions this skill requires, or an empty list if no special
     *         permissions are needed
     */
    @NonNull
    public List<String> getNeededPermissions() {
        return Collections.emptyList();
    }
}
