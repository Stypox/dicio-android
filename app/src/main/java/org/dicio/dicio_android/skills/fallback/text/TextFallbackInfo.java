package org.dicio.dicio_android.skills.fallback.text;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import org.dicio.dicio_android.skills.Skill;
import org.dicio.dicio_android.skills.SkillInfo;

public class TextFallbackInfo implements SkillInfo {

    @Override
    public Skill build(final Context context, final SharedPreferences preferences) {
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
