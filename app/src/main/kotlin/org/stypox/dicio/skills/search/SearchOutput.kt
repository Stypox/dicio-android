package org.stypox.dicio.skills.search

import android.content.Context
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
import org.dicio.skill.chain.CaptureEverythingRecognizer
import org.dicio.skill.chain.ChainSkill
import org.dicio.skill.output.SkillOutput
import org.dicio.skill.standard.StandardRecognizer
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated
import org.stypox.dicio.output.graphical.Headline
import org.stypox.dicio.util.ShareUtils

class SearchOutput(
    context: Context,
    private val results: List<SearchGenerator.Data>?
) : SkillOutput {
    override val speechOutput = context.getString(
        if (results == null)
            R.string.skill_search_what_question
        else if (results.isEmpty())
            R.string.skill_search_no_results
        else
            R.string.skill_search_here_is_what_i_found
    )

    override val nextSkills =
        if (results.isNullOrEmpty())
            listOf(
                ChainSkill.Builder(StandardRecognizer(Sections.getSection(SectionsGenerated.search)))
                    .process(DuckDuckGoProcessor())
                    .output(SearchGenerator()),
                ChainSkill.Builder(CaptureEverythingRecognizer())
                    .process(DuckDuckGoProcessor())
                    .output(SearchGenerator())
            )
        else
            listOf()

    @Composable
    override fun GraphicalOutput() {
        if (results.isNullOrEmpty()) {
            Headline(text = speechOutput)
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
private fun SearchResult(data: SearchGenerator.Data) {
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
