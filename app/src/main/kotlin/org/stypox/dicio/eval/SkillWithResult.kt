package org.stypox.dicio.eval

import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Score
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillOutput

data class SkillWithResult<InputData>(
    val skill: Skill<InputData>,
    val score: Score,
    val inputData: InputData,
) {
    suspend fun generateOutput(ctx: SkillContext): SkillOutput {
        return skill.generateOutput(ctx, inputData)
    }
}

fun <InputData> Skill<InputData>.scoreAndWrapResult(
    ctx: SkillContext,
    input: String,
): SkillWithResult<InputData> {
    val (score, inputData) = score(ctx, input)
    return SkillWithResult(
        skill = this,
        score = score,
        inputData = inputData,
    )
}
