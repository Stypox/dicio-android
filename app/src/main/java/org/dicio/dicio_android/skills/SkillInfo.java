package org.dicio.dicio_android.skills;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.preference.PreferenceFragmentCompat;

public abstract class SkillInfo {

    private final String id;
    @StringRes private final int nameResource;
    private final boolean hasPreferences;

    /**
     * Constructor for {@link SkillInfo}, providing basic information
     * @param id a unique identifier for this skill (different from that of all other skills)
     * @param nameResource the Android string resource containing the skill name to show to the user
     * @param hasPreferences whether this skill has preferences. If this is false, then
     *                       {@link #getPreferenceFragment()} is assumed to return {@code null},
     *                       otherwise it has to return a non-null preference fragment.
     */
    public SkillInfo(final String id,
                     @StringRes final int nameResource,
                     final boolean hasPreferences) {
        this.id = id;
        this.nameResource = nameResource;
        this.hasPreferences = hasPreferences;
    }

    /**
     * @return a unique identifier for this skill (different from that of all other skills)
     */
    public final String getId() {
        return id;
    }

    /**
     * @return the Android string resource containing the skill name to show to the user
     */
    @StringRes
    public final int getNameResource() {
        return nameResource;
    }

    /**
     * @return whether this skill has preferences. {@link #getPreferenceFragment()} returns
     *         {@code null} only if this is false.
     */
    public final boolean hasPreferences() {
        return hasPreferences;
    }

    /**
     * Builds an instance of the {@link Skill} this {@link SkillInfo} object represents
     * @param context the Android context
     * @param preferences the app preferences
     * @return a skill
     */
    public abstract Skill build(Context context, SharedPreferences preferences);

    /**
     * Provides a custom preferences screen for this skill, allowing the user to customize it to
     * their needs.
     * @return {@code null} if {@link #hasPreferences()} is {@code false}, otherwise an instance of
     *         a preference fragment. Please note that the {@code class} of the returned object
     *         should be {@code public static}, otherwise Android libraries complain.
     */
    @Nullable
    public abstract PreferenceFragmentCompat getPreferenceFragment();
}
