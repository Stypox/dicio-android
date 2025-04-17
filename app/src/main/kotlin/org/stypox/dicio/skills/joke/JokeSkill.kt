package org.stypox.dicio.skills.joke

import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.StandardRecognizerSkill
import org.json.JSONObject
import org.stypox.dicio.sentences.Sentences.Joke
import org.stypox.dicio.util.ConnectionUtils


class JokeSkill(correspondingSkillInfo: SkillInfo, data: StandardRecognizerData<Joke>)
    : StandardRecognizerSkill<Joke>(correspondingSkillInfo, data) {
    override suspend fun generateOutput(ctx: SkillContext, inputData: Joke): SkillOutput {
        val joke: JSONObject = ConnectionUtils.getPageJson(RANDOM_JOKE_URL)
        return JokeOutput.Success(
            setup = joke.getString("setup"),
            punchline = joke.getString("punchline")
        )
    }

    companion object {
        private const val RANDOM_JOKE_URL = "https://official-joke-api.appspot.com/random_joke"
    }
}
