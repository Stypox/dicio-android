package org.stypox.dicio.skills.current_time

import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.StandardRecognizerSkill
import org.stypox.dicio.sentences.Sentences.CurrentTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class CurrentTimeSkill(correspondingSkillInfo: SkillInfo, data: StandardRecognizerData<CurrentTime>)
    : StandardRecognizerSkill<CurrentTime>(correspondingSkillInfo, data) {
    override suspend fun generateOutput(ctx: SkillContext, inputData: CurrentTime): SkillOutput {
        val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(ctx.locale)
        return CurrentTimeOutput(LocalTime.now().format(formatter))
    }
}
