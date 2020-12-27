package org.dicio.dicio_android.components.fallback.text;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import org.dicio.dicio_android.components.AssistanceComponent;
import org.dicio.dicio_android.components.AssistanceComponentInfo;

public class TextFallbackInfo implements AssistanceComponentInfo {

    @Override
    public AssistanceComponent build(final Context context, final SharedPreferences preferences) {
        return new TextFallback();
    }

    @Override
    public boolean hasPreferences() {
        return false;
    }

    @Nullable
    @Override
    public PreferenceFragmentCompat getPreferenceFragment() {
        return null;
    }
}
