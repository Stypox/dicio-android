package org.stypox.dicio.skills.calendar

import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.StandardRecognizerSkill
import org.stypox.dicio.sentences.Sentences.Calendar

class CalendarSkill(correspondingSkillInfo: SkillInfo, data: StandardRecognizerData<Calendar>) :
    StandardRecognizerSkill<Calendar>(correspondingSkillInfo, data) {

    override suspend fun generateOutput(ctx: SkillContext, inputData: Calendar): SkillOutput {
        val whenString = when (inputData) { is Calendar.Set -> inputData.`when` }
            ?: return CalendarOutput.FailedStringToDateTime(null)
        val whenDateTime = try { ctx.parserFormatter?.extractDateTime(whenString)?.first }
            catch (_: Throwable) { null }
            ?: return CalendarOutput.FailedStringToDateTime(whenString)
        val whenBackToString = try { ctx.parserFormatter?.niceDateTime(whenDateTime)?.get() }
            catch (_: Throwable) { null }
            ?: return CalendarOutput.FailedStringToDateTime(whenString)
        return CalendarOutput.Success(whenString, whenDateTime, whenBackToString)
    }
}
