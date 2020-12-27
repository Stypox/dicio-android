package org.dicio.dicio_android.skills;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

public interface SkillInfo {
    Skill build(Context context, SharedPreferences preferences);

    boolean hasPreferences();

    @Nullable
    PreferenceFragmentCompat getPreferenceFragment();
}
