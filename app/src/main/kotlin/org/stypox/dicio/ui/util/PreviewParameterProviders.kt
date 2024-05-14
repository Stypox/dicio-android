package org.stypox.dicio.ui.util

import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import org.dicio.skill.SkillInfo
import org.dicio.skill.output.SkillOutput
import org.stypox.dicio.skills.calculator.CalculatorInfo
import org.stypox.dicio.skills.fallback.text.TextFallbackOutput
import org.stypox.dicio.skills.lyrics.LyricsInfo
import org.stypox.dicio.skills.navigation.NavigationInfo
import org.stypox.dicio.skills.navigation.NavigationOutput
import org.stypox.dicio.skills.telephone.ConfirmCallOutput
import org.stypox.dicio.skills.telephone.ConfirmedCallOutput
import org.stypox.dicio.skills.telephone.TelephoneInfo
import org.stypox.dicio.skills.timer.TimerInfo
import org.stypox.dicio.skills.timer.TimerOutput
import org.stypox.dicio.skills.weather.WeatherInfo
import org.stypox.dicio.ui.main.Conversation
import org.stypox.dicio.ui.main.PendingQuestion


class UserInputPreviews : CollectionPreviewParameterProvider<String>(listOf(
    "",
    "When",
    "What's the weather?",
    LoremIpsum(50).values.first(),
))

class SkillInfoPreviews : CollectionPreviewParameterProvider<SkillInfo>(listOf(
    WeatherInfo,
    CalculatorInfo,
))

class SkillOutputPreviews : CollectionPreviewParameterProvider<SkillOutput>(listOf(
    TextFallbackOutput(),
))

class ConversationPreviews : CollectionPreviewParameterProvider<Pair<List<Conversation>, PendingQuestion?>>(listOf(
    Pair(
        listOf(),
        null,
    ),
    Pair(
        listOf(),
        PendingQuestion(
            userInput = "What's the weather?",
            continuesLastConversation = true,
            skillBeingEvaluated = null,
        ),
    ),
    Pair(
        listOf(),
        PendingQuestion(
            userInput = LoremIpsum(50).values.first(),
            continuesLastConversation = false,
            skillBeingEvaluated = SkillInfoPreviews().values.first(),
        ),
    ),
    Pair(
        listOf(
            Conversation(
                skill = NavigationInfo,
                questionsAnswers = listOf(
                    Pair("Take me to Paris", NavigationOutput("Paris"))
                )
            ),
            Conversation(
                skill = TimerInfo,
                questionsAnswers = listOf(
                    Pair("Set a timer", TimerOutput.SetAskDuration { TextFallbackOutput() })
                )
            )
        ),
        PendingQuestion(
            userInput = "Twenty",
            continuesLastConversation = true,
            skillBeingEvaluated = null,
        ),
    ),
    Pair(
        listOf(
            Conversation(
                skill = TelephoneInfo,
                questionsAnswers = listOf(
                    Pair("call mom", ConfirmCallOutput("Mom", "1234567890")),
                    Pair("yes", ConfirmedCallOutput("1234567890")),
                )
            )
        ),
        PendingQuestion(
            userInput = "lyrics i'm working on a dream",
            continuesLastConversation = false,
            skillBeingEvaluated = LyricsInfo,
        ),
    ),
))
