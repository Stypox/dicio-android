package org.dicio.dicio_android.skills.lyrics;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import org.dicio.skill.standard.StandardRecognizer;
import org.dicio.dicio_android.skills.Skill;
import org.dicio.dicio_android.skills.SkillInfo;
import org.dicio.dicio_android.skills.ChainSkill;

import static org.dicio.dicio_android.Sections.getSection;
import static org.dicio.dicio_android.SectionsGenerated.lyrics;

public class LyricsInfo implements SkillInfo {

    @Override
    public Skill build(final Context context, final SharedPreferences preferences) {
        return new ChainSkill.Builder()
                .recognize(new StandardRecognizer(getSection(lyrics)))
                .process(new GeniusProcessor())
                .output(new LyricsOutput());
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
