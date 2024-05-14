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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import org.dicio.skill.SkillContext
import org.dicio.skill.SkillInfo
import org.dicio.skill.output.SkillOutput
import org.stypox.dicio.ui.theme.AppTheme
import org.stypox.dicio.ui.util.ConversationPreviews
import org.stypox.dicio.ui.util.SkillInfoPreviews
import org.stypox.dicio.ui.util.SkillOutputPreviews
import org.stypox.dicio.ui.util.UserInputPreviews

@Composable
fun ConversationList(
    conversations: List<Conversation>,
    pendingQuestion: PendingQuestion?,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
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

@Preview
@Composable
private fun ConversationListPreview(@PreviewParameter(ConversationPreviews::class)
                                    data: Pair<List<Conversation>, PendingQuestion?>) {
    AppTheme(dynamicColor = false) {
        ConversationList(
            conversations = data.first,
            pendingQuestion = data.second,
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
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal),
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
