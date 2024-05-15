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
import org.stypox.dicio.ui.main.Interaction
import org.stypox.dicio.ui.main.InteractionLog
import org.stypox.dicio.ui.main.PendingQuestion
import org.stypox.dicio.ui.main.SttState
import java.io.IOException


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

class InteractionLogPreviews : CollectionPreviewParameterProvider<InteractionLog>(listOf(
    InteractionLog(
        listOf(),
        null,
    ),
    InteractionLog(
        listOf(),
        PendingQuestion(
            userInput = "What's the weather?",
            continuesLastInteraction = true,
            skillBeingEvaluated = null,
        ),
    ),
    InteractionLog(
        listOf(),
        PendingQuestion(
            userInput = LoremIpsum(50).values.first(),
            continuesLastInteraction = false,
            skillBeingEvaluated = SkillInfoPreviews().values.first(),
        ),
    ),
    InteractionLog(
        listOf(
            Interaction(
                skill = NavigationInfo,
                questionsAnswers = listOf(
                    Pair("Take me to Paris", NavigationOutput("Paris"))
                )
            ),
            Interaction(
                skill = TimerInfo,
                questionsAnswers = listOf(
                    Pair("Set a timer", TimerOutput.SetAskDuration { TextFallbackOutput() })
                )
            )
        ),
        PendingQuestion(
            userInput = "Twenty",
            continuesLastInteraction = true,
            skillBeingEvaluated = null,
        ),
    ),
    InteractionLog(
        listOf(
            Interaction(
                skill = TelephoneInfo,
                questionsAnswers = listOf(
                    Pair("call mom", ConfirmCallOutput("Mom", "1234567890")),
                    Pair("yes", ConfirmedCallOutput("1234567890")),
                )
            )
        ),
        PendingQuestion(
            userInput = "lyrics i'm working on a dream",
            continuesLastInteraction = false,
            skillBeingEvaluated = LyricsInfo,
        ),
    ),
))

class SttStatesPreviews : CollectionPreviewParameterProvider<SttState>(listOf(
    SttState.NoMicrophonePermission,
    SttState.NotDownloaded,
    SttState.Downloading(987654, 0),
    SttState.Downloading(987654, 1234567),
    SttState.ErrorDownloading(IOException("ErrorDownloading exception")),
    SttState.Downloaded,
    SttState.Unzipping(765432, 0),
    SttState.Unzipping(765432, 9876543),
    SttState.ErrorUnzipping(Exception("ErrorUnzipping exception")),
    SttState.NotLoaded,
    SttState.Loading(true),
    SttState.Loading(false),
    SttState.ErrorLoading(Exception("ErrorLoading exception")),
    SttState.Loaded,
    SttState.Listening,
))
