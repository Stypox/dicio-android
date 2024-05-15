package org.stypox.dicio.skills.lyrics

import androidx.fragment.app.Fragment
import org.dicio.skill.Skill
import org.dicio.skill.SkillContext
import org.dicio.skill.SkillInfo
import org.dicio.skill.chain.ChainSkill
import org.dicio.skill.standard.StandardRecognizer
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated.lyrics

object LyricsInfo : SkillInfo(
    "lyrics", R.string.skill_name_lyrics, R.string.skill_sentence_example_lyrics,
    R.drawable.ic_music_note_white, false
) {
    override fun isAvailable(context: SkillContext): Boolean {
        return Sections.isSectionAvailable(lyrics)
    }

    override fun build(context: SkillContext): Skill {
        return ChainSkill.Builder(
            LyricsInfo,
            StandardRecognizer(Sections.getSection(lyrics))
        )
            .process(GeniusProcessor())
            .output(LyricsGenerator())
    }

    override val preferenceFragment: Fragment? = null
}
