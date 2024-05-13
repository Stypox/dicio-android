package org.stypox.dicio.skills.lyrics

import org.dicio.skill.chain.OutputGenerator
import org.dicio.skill.output.SkillOutput

class LyricsGenerator : OutputGenerator<LyricsGenerator.Data>() {
    sealed class Data {
        data class Success(
            val title: String,
            val artist: String,
            val lyrics: String,
        ) : Data()

        data class Failed(
            val title: String,
        ) : Data()
    }

    override fun generate(data: Data): SkillOutput {
        return LyricsOutput(ctx().android!!, data)
    }
}
