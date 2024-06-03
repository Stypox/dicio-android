package org.dicio.skill.standard

import org.dicio.skill.skill.Skill
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.util.WordExtractor

abstract class StandardRecognizerSkill(
    correspondingSkillInfo: SkillInfo,
    private val data: StandardRecognizerData,
) : Skill<StandardResult>(correspondingSkillInfo, data.specificity) {

    override fun score(
        ctx: SkillContext,
        input: String
    ): Pair<Float, StandardResult> {
        val inputWords = WordExtractor.extractWords(input)
        val normalizedWords = WordExtractor.normalizeWords(inputWords)
        return data.score(input, inputWords, normalizedWords)
    }
}
