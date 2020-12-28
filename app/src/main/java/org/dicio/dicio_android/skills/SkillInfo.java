package org.dicio.dicio_android.skills;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.preference.PreferenceFragmentCompat;

public abstract class SkillInfo {

    final String id;
    @StringRes final int nameResource;
    final boolean hasPreferences;

    public SkillInfo(final String id,
                     @StringRes final int nameResource,
                     final boolean hasPreferences) {
        this.id = id;
        this.nameResource = nameResource;
        this.hasPreferences = hasPreferences;
    }

    public final String getId() {
        return id;
    }

    @StringRes
    public final int getName() {
        return nameResource;
    }

    public final boolean hasPreferences() {
        return hasPreferences;
    }

    public abstract Skill build(Context context, SharedPreferences preferences);

    @Nullable
    public abstract PreferenceFragmentCompat getPreferenceFragment();
}
