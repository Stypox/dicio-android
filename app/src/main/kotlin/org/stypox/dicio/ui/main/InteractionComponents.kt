package org.stypox.dicio.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import org.dicio.skill.SkillContext
import org.dicio.skill.SkillInfo
import org.dicio.skill.output.SkillOutput
import org.stypox.dicio.di.LocaleManager
import org.stypox.dicio.di.SkillContextImpl
import org.stypox.dicio.skills.SkillHandler
import org.stypox.dicio.ui.theme.AppTheme
import org.stypox.dicio.ui.util.InteractionLogPreviews
import org.stypox.dicio.ui.util.SkillInfoPreviews
import org.stypox.dicio.ui.util.SkillOutputPreviews
import org.stypox.dicio.ui.util.UserInputPreviews
import java.util.Locale

@Composable
fun InteractionList(
    skillContext: SkillContext,
    interactionLog: InteractionLog,
    onConfirmedQuestionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactions = interactionLog.interactions
    val pendingQuestion = interactionLog.pendingQuestion

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        interactions.forEachIndexed { index, interaction ->
            if (index != 0) {
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
            interaction.questionsAnswers.forEach {
                item {
                    ConfirmedQuestionCard(
                        userInput = it.first,
                        onClick = onConfirmedQuestionClick,
                    )
                }
                item {
                    SkillAnswerCard {
                        it.second.GraphicalOutput(ctx = skillContext)
                    }
                }
            }
        }

        if (pendingQuestion != null) {
            if (interactions.isNotEmpty() && !pendingQuestion.continuesLastInteraction) {
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            if (pendingQuestion.skillBeingEvaluated == null) {
                item { PendingQuestionCard(userInput = pendingQuestion.userInput) }
            } else {
                item {
                    ConfirmedQuestionCard(
                        userInput = pendingQuestion.userInput,
                        onClick = onConfirmedQuestionClick,
                    )
                }
                item { LoadingAnswerCard(skill = pendingQuestion.skillBeingEvaluated) }
            }
        }

        item { Spacer(modifier = Modifier.height(84.dp)) }
    }
}

@Preview
@Composable
fun InteractionListPreview(
    @PreviewParameter(InteractionLogPreviews::class) interactionLog: InteractionLog,
) {
    AppTheme(dynamicColor = false) {
        InteractionList(
            skillContext = SkillContextImpl.newForPreviews(),
            interactionLog = interactionLog,
            onConfirmedQuestionClick = {},
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
fun ConfirmedQuestionCard(
    @PreviewParameter(UserInputPreviews::class) userInput: String,
    onClick: (String) -> Unit = {},
) {
    MessageCard(
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        onClick = { onClick(userInput) },
    ) {
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = skill.iconResource),
                contentDescription = null,
                tint = ProgressIndicatorDefaults.circularColor,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            CircularProgressIndicator()
        }
    }
}

@Composable
fun SkillAnswerCard(content: @Composable BoxScope.() -> Unit) {
    MessageCard(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            content = content,
        )
    }
}

@Preview
@Composable
fun SkillAnswerCardPreview(@PreviewParameter(SkillOutputPreviews::class) skillOutput: SkillOutput) {
    SkillAnswerCard {
        skillOutput.GraphicalOutput(ctx = SkillContextImpl.newForPreviews())
    }
}

@Composable
fun MessageCard(
    containerColor: Color,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CardDefaults.cardColors(containerColor = containerColor)
    val modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp)
    if (onClick == null) {
        Card(colors = colors, modifier = modifier, content = content)
    } else {
        Card(onClick = onClick, modifier = modifier, colors = colors, content = content)
    }
}
