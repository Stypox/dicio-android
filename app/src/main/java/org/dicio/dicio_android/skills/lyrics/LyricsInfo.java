package org.dicio.dicio_android.skills.lyrics;

import static org.dicio.dicio_android.Sections.getSection;
import static org.dicio.dicio_android.Sections.isSectionAvailable;
import static org.dicio.dicio_android.SectionsGenerated.lyrics;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import org.dicio.dicio_android.R;
import org.dicio.skill.Skill;
import org.dicio.skill.SkillContext;
import org.dicio.skill.SkillInfo;
import org.dicio.skill.chain.ChainSkill;
import org.dicio.skill.standard.StandardRecognizer;

public class LyricsInfo extends SkillInfo {

    public LyricsInfo() {
        super("lyrics", R.string.skill_name_lyrics, R.string.skill_sentence_example_lyrics,
                R.drawable.ic_music_note_white, false);
    }

    @Override
    public boolean isAvailable(final SkillContext context) {
        return isSectionAvailable(lyrics);
    }

    @Override
    public Skill build(final SkillContext context) {
        return new ChainSkill.Builder(this)
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
