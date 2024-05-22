package org.stypox.dicio.ui.main

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
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput
import org.stypox.dicio.di.SkillContextImpl
import org.stypox.dicio.ui.theme.AppTheme
import org.stypox.dicio.ui.util.InteractionLogPreviews
import org.stypox.dicio.ui.util.SkillInfoPreviews
import org.stypox.dicio.ui.util.SkillOutputPreviews
import org.stypox.dicio.ui.util.UserInputPreviews

@Composable
fun InteractionList(
    skillContext: SkillContext,
    interactionLog: InteractionLog,
    onConfirmedQuestionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var totalItemCount = 0
    var lastAnchorIndex = 0
    fun LazyListScope.countedItem(
        canBeAnchor: Boolean,
        content: @Composable LazyItemScope.() -> Unit,
    ) {
        if (canBeAnchor) {
            lastAnchorIndex = totalItemCount
        }
        item(content = content)
        totalItemCount += 1
    }

    val interactions = interactionLog.interactions
    val pendingQuestion = interactionLog.pendingQuestion
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = listState,
    ) {
        interactions.forEachIndexed { index, interaction ->
            if (index != 0) {
                countedItem(canBeAnchor = false) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            interaction.questionsAnswers.forEach {
                if (it.question != null) {
                    // can be null if this QA contains only an error output
                    // (and therefore the error was not caused by the input, but e.g. by the STT)
                    countedItem(canBeAnchor = true) {
                        ConfirmedQuestionCard(
                            userInput = it.question,
                            onClick = onConfirmedQuestionClick,
                        )
                    }
                }
                countedItem(canBeAnchor = it.question == null) {
                    SkillAnswerCard {
                        it.answer.GraphicalOutput(ctx = skillContext)
                    }
                }
            }
        }

        if (pendingQuestion != null) {
            if (interactions.isNotEmpty() && !pendingQuestion.continuesLastInteraction) {
                countedItem(canBeAnchor = false) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (pendingQuestion.skillBeingEvaluated == null) {
                countedItem(canBeAnchor = true) {
                    PendingQuestionCard(userInput = pendingQuestion.userInput)
                }
                countedItem(canBeAnchor = false) {
                    // still include a spacer in place of the LoadingAnswerCard, so that when the
                    // pending question first appears and the LaunchedEffect below scrolls to it,
                    // there is enough space for the loading card below the question card that will
                    // only appear after the pending question has been confirmed
                    Spacer(modifier = Modifier.height(72.dp))
                }
            } else {
                countedItem(canBeAnchor = true) {
                    ConfirmedQuestionCard(
                        userInput = pendingQuestion.userInput,
                        onClick = onConfirmedQuestionClick,
                    )
                }
                countedItem(canBeAnchor = false) {
                    LoadingAnswerCard(skill = pendingQuestion.skillBeingEvaluated)
                }
            }
        }

        countedItem(canBeAnchor = false) {
            Spacer(modifier = Modifier.height(84.dp))
        }
    }

    // TODO check if this causes performance problems
    var prevHasPending by rememberSaveable { mutableStateOf(false) }
    var prevInteractionCount by rememberSaveable { mutableIntStateOf(0) }
    val newHasPending = pendingQuestion != null
    val newInteractionCount = interactions.size
    LaunchedEffect(key1 = newHasPending, key2 = newInteractionCount) {
        if ((!prevHasPending && newHasPending) || newInteractionCount > prevInteractionCount) {
            scope.launch {
                listState.scrollToItem(lastAnchorIndex)
            }
        }
        prevHasPending = newHasPending
        prevInteractionCount = newInteractionCount
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
