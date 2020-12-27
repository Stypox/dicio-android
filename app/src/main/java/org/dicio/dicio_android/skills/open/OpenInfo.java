package org.dicio.dicio_android.skills.open;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import org.dicio.component.standard.StandardRecognizer;
import org.dicio.dicio_android.skills.Skill;
import org.dicio.dicio_android.skills.SkillInfo;
import org.dicio.dicio_android.skills.ChainSkill;

import static org.dicio.dicio_android.Sections.getSection;
import static org.dicio.dicio_android.SectionsGenerated.open;

public class OpenInfo implements SkillInfo {

    @Override
    public Skill build(final Context context, final SharedPreferences preferences) {
        return new ChainSkill.Builder()
                .recognize(new StandardRecognizer(getSection(open)))
                .output(new OpenOutput());
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
