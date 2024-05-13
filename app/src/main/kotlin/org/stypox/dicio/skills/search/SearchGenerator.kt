package org.stypox.dicio.skills.search

import org.dicio.skill.chain.OutputGenerator
import org.dicio.skill.output.SkillOutput

class SearchGenerator : OutputGenerator<List<SearchGenerator.Data>?>() {
    class Data (
        val title: String,
        val thumbnailUrl: String,
        val url: String,
        val description: String,
    )

    override fun generate(data: List<Data>?): SkillOutput {
        return SearchOutput(ctx().android!!, data)
    }
}
