package org.dicio.skill.old_standard_impl

import org.dicio.skill.skill.Skill
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.old_standard.WordExtractor
import org.dicio.skill.skill.Score

abstract class StandardRecognizerSkill(
    correspondingSkillInfo: SkillInfo,
    private val data: StandardRecognizerData,
) : Skill<StandardResult>(correspondingSkillInfo, data.specificity) {

    override fun score(
        ctx: SkillContext,
        input: String
    ): Pair<Score, StandardResult> {
        val inputWords = WordExtractor.extractWords(input)
        val normalizedWords = WordExtractor.normalizeWords(inputWords)
        return data.score(input, inputWords, normalizedWords)
    }
}
