package org.stypox.dicio.skills.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.InteractionPlan
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.skill.Specificity
import org.stypox.dicio.R
import org.stypox.dicio.io.graphical.HeadlineSpeechSkillOutput
import org.stypox.dicio.sentences.Sentences
import org.stypox.dicio.skills.search.SearchOutput.Data
import org.stypox.dicio.util.RecognizeEverythingSkill
import org.stypox.dicio.util.ShareUtils
import org.stypox.dicio.util.getString

sealed interface SearchOutput : SkillOutput {

    data class Results(private val results: List<Data>) : SearchOutput {
        override fun getSpeechOutput(ctx: SkillContext): String =
            ctx.getString(R.string.skill_search_here_is_what_i_found)

        @Composable
        override fun GraphicalOutput(ctx: SkillContext) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                for (result in results) {
                    SearchResult(result)
                }
            }
        }
    }

    object NoSearchTerm : SearchOutput, HeadlineSpeechSkillOutput {
        override fun getSpeechOutput(ctx: SkillContext): String =
            ctx.getString(R.string.skill_search_what_question)

        override fun getInteractionPlan(ctx: SkillContext): InteractionPlan =
            getRetryInteractionPlan(ctx)
    }

    object NoResultAskAgain : SearchOutput, HeadlineSpeechSkillOutput {
        override fun getSpeechOutput(ctx: SkillContext): String =
            ctx.getString(R.string.skill_search_no_results)

        override fun getInteractionPlan(ctx: SkillContext): InteractionPlan =
            getRetryInteractionPlan(ctx)
    }

    object NoResultStop : SearchOutput, HeadlineSpeechSkillOutput {
        override fun getSpeechOutput(ctx: SkillContext): String =
            ctx.getString(R.string.skill_search_no_results_stop)
    }

    object RecaptchaRequested : SearchOutput, HeadlineSpeechSkillOutput {
        override fun getSpeechOutput(ctx: SkillContext): String =
            ctx.getString(R.string.skill_search_duckduckgo_recaptcha)
    }


    class Data (
        val title: String,
        val thumbnailUrl: String,
        val url: String,
        val description: String,
    )

    companion object {
        private fun getRetryInteractionPlan(ctx: SkillContext): InteractionPlan {
            val searchAnythingSkill = object : RecognizeEverythingSkill(SearchInfo) {
                override suspend fun generateOutput(
                    ctx: SkillContext,
                    inputData: String
                ): SkillOutput {
                    // if the search fails again, do not ask the user to retry, to avoid going
                    // on indefinitely
                    return searchOnDuckDuckGo(ctx, inputData, askAgainIfNoResult = false)
                }
            }

            return InteractionPlan.StartSubInteraction(
                reopenMicrophone = true,
                nextSkills = listOf(
                    SearchSkill(
                        correspondingSkillInfo = SearchInfo,
                        data = Sentences.Search[ctx.sentencesLanguage]!!,
                        // increase the specificity from LOW to MEDIUM on purpose, so that this has
                        // priority over searchAnythingSkill
                        specificity = Specificity.MEDIUM,
                        // if the search fails again, do not ask the user to retry, to avoid going
                        // on indefinitely
                        askAgainIfNoResult = false,
                    ),
                    searchAnythingSkill,
                ),
            )
        }
    }
}

@Composable
private fun SearchResult(data: Data) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.clickable { ShareUtils.openUrlInBrowser(context, data.url) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = data.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .width(16.dp)
                    .aspectRatio(1.0f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = data.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Text(
            text = data.description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
