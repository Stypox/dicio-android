package org.stypox.dicio.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import org.dicio.skill.SkillContext
import org.dicio.skill.SkillInfo
import org.dicio.skill.output.SkillOutput
import org.stypox.dicio.R
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
import org.stypox.dicio.ui.nav.SearchTopAppBar
import org.stypox.dicio.ui.theme.AppTheme

@Composable
fun MainScreen(
    conversations: List<Conversation>,
    pendingQuestion: PendingQuestion?,
    navigationIcon: @Composable () -> Unit,
) {
    Scaffold(
        topBar = {
            SearchTopAppBar(
                onSearch = {},
                hint = stringResource(R.string.text_input_hint),
                title = {
                    Text(text = stringResource(R.string.app_name))
                },
                searchIcon = {
                    Icon(
                        imageVector = Icons.Default.QuestionAnswer,
                        contentDescription = null,
                    )
                },
                navigationIcon = navigationIcon,
            )
        },
        content = {
            LazyColumn(
                modifier = Modifier.padding(it),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                conversations.forEachIndexed { index, conversation ->
                    if (index != 0) {
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                    conversation.questionsAnswers.forEach {
                        item { ConfirmedQuestionCard(userInput = it.first) }
                        item { SkillAnswerCard(skillOutput = it.second) }
                    }
                }

                if (pendingQuestion != null) {
                    if (conversations.isNotEmpty() && !pendingQuestion.continuesLastConversation) {
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }

                    if (pendingQuestion.skillBeingEvaluated == null) {
                        item { PendingQuestionCard(userInput = pendingQuestion.userInput) }
                    } else {
                        item { ConfirmedQuestionCard(userInput = pendingQuestion.userInput) }
                        item { LoadingAnswerCard(skill = pendingQuestion.skillBeingEvaluated) }
                    }
                }
            }
        }
    )
}

@Preview
@Composable
private fun MainScreenPreview(@PreviewParameter(ConversationPreviews::class)
                              data: Pair<List<Conversation>, PendingQuestion?>) {
    AppTheme(dynamicColor = false) {
        MainScreen(
            conversations = data.first,
            pendingQuestion = data.second,
            navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = null,
                    )
                }
            }
        )
    }
}

@Preview
@Composable
fun PendingQuestionCard(@PreviewParameter(UserInputPreviews::class) userInput: String) {
    MessageCard(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
            )
            Text(
                text = userInput,
                fontStyle = FontStyle.Italic,
            )
        }
    }
}

@Preview
@Composable
fun ConfirmedQuestionCard(@PreviewParameter(UserInputPreviews::class) userInput: String) {
    MessageCard(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
            )
            Text(
                text = userInput,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Preview
@Composable
fun LoadingAnswerCard(@PreviewParameter(SkillInfoPreviews::class) skill: SkillInfo) {
    MessageCard(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            CircularProgressIndicator()
        }
    }
}

@Preview
@Composable
fun SkillAnswerCard(@PreviewParameter(SkillOutputPreviews::class) skillOutput: SkillOutput) {
    MessageCard(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            val context = LocalContext.current
            skillOutput.GraphicalOutput(ctx = SkillContext().apply { android = context })
        }
    }
}

@Composable
fun MessageCard(containerColor: Color, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        content = content,
    )
}


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

