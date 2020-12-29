package org.dicio.dicio_android.skills.fallback.text;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import org.dicio.dicio_android.R;
import org.dicio.skill.Skill;
import org.dicio.skill.SkillInfo;

import java.util.Locale;

public class TextFallbackInfo extends SkillInfo {

    public TextFallbackInfo() {
        super("text", R.string.skill_fallback_name_text, false);
    }

    @Override
    public Skill build(final Context context,
                       final SharedPreferences preferences,
                       final Locale locale) {
        return new TextFallback();
    }

    @Nullable
    @Override
    public PreferenceFragmentCompat getPreferenceFragment() {
        return null;
    }
}
