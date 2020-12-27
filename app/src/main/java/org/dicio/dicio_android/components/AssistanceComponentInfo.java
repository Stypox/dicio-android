package org.dicio.dicio_android.components;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

public interface AssistanceComponentInfo {
    AssistanceComponent build(Context context, SharedPreferences preferences);

    boolean hasPreferences();

    @Nullable
    PreferenceFragmentCompat getPreferenceFragment();
}
