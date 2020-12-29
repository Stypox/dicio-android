package org.dicio.dicio_android.skills.lyrics;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import org.dicio.dicio_android.R;
import org.dicio.skill.Skill;
import org.dicio.skill.SkillInfo;
import org.dicio.skill.chain.ChainSkill;
import org.dicio.skill.standard.StandardRecognizer;

import java.util.Locale;

import static org.dicio.dicio_android.Sections.getSection;
import static org.dicio.dicio_android.SectionsGenerated.lyrics;

public class LyricsInfo extends SkillInfo {

    public LyricsInfo() {
        super("lyrics", R.string.skill_name_lyrics, false);
    }

    @Override
    public Skill build(final Context context,
                       final SharedPreferences preferences,
                       final Locale locale) {

        return new ChainSkill.Builder()
                .recognize(new StandardRecognizer(getSection(lyrics)))
                .process(new GeniusProcessor())
                .output(new LyricsOutput());
    }

    @Nullable
    @Override
    public PreferenceFragmentCompat getPreferenceFragment() {
        return null;
    }
}
