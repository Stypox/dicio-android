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
import org.dicio.skill.skill.Skill
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillOutput
import org.stypox.dicio.util.RecognizeEverythingSkill
import org.stypox.dicio.R
import org.stypox.dicio.io.graphical.Headline
import org.stypox.dicio.sentences.Sentences
import org.stypox.dicio.util.ShareUtils
import org.stypox.dicio.util.getString

class SearchOutput(
    private val results: List<Data>?
) : SkillOutput {
    class Data (
        val title: String,
        val thumbnailUrl: String,
        val url: String,
        val description: String,
    )

    override fun getSpeechOutput(ctx: SkillContext): String = ctx.getString(
        if (results == null)
            R.string.skill_search_what_question
        else if (results.isEmpty())
            R.string.skill_search_no_results
        else
            R.string.skill_search_here_is_what_i_found
    )

    override fun getNextSkills(ctx: SkillContext): List<Skill<*>> = if (results.isNullOrEmpty())
        listOf(
            SearchSkill(SearchInfo, Sentences.Search[ctx.locale.language]!!),
            object : RecognizeEverythingSkill(SearchInfo) {
                override suspend fun generateOutput(ctx: SkillContext, scoreResult: String): SkillOutput {
                    return SearchOutput(searchOnDuckDuckGo(ctx, scoreResult))
                }
            },
        )
    else
        listOf()

    @Composable
    override fun GraphicalOutput(ctx: SkillContext) {
        if (results.isNullOrEmpty()) {
            Headline(text = getSpeechOutput(ctx))
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                for (result in results) {
                    SearchResult(result)
                }
            }
        }
    }
}

@Composable
private fun SearchResult(data: SearchOutput.Data) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.clickable {
            ShareUtils.openUrlInBrowser(context, data.url)
        }
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
