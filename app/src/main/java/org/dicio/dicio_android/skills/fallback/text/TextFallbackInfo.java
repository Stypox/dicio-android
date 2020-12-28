package org.dicio.dicio_android.skills.fallback.text;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.preference.PreferenceFragmentCompat;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.skills.Skill;
import org.dicio.dicio_android.skills.SkillInfo;

public class TextFallbackInfo extends SkillInfo {

    public TextFallbackInfo() {
        super("text", R.string.skill_fallback_name_text, false);
    }

    @Override
    public Skill build(final Context context, final SharedPreferences preferences) {
        return new TextFallback();
    }

    @Nullable
    @Override
    public PreferenceFragmentCompat getPreferenceFragment() {
        return null;
    }
}
