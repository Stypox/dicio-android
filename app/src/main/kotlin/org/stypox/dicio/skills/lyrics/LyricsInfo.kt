package org.stypox.dicio.skills.lyrics

import androidx.fragment.app.Fragment
import org.dicio.skill.skill.Skill
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
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

    override fun build(context: SkillContext): Skill<*> {
        return LyricsSkill(LyricsInfo, Sections.getSection(lyrics))
    }

    override val preferenceFragment: Fragment? = null
}
