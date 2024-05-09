package org.stypox.dicio.skills.search

import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.squareup.picasso.Picasso
import org.dicio.skill.chain.CaptureEverythingRecognizer
import org.dicio.skill.chain.ChainSkill
import org.dicio.skill.chain.OutputGenerator
import org.dicio.skill.standard.StandardRecognizer
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.SectionsGenerated
import org.stypox.dicio.output.graphical.GraphicalOutputUtils
import org.stypox.dicio.util.ShareUtils.openUrlInBrowser

class SearchOutput : OutputGenerator<List<SearchOutput.Data>?>() {
    class Data (
        val title: String,
        val thumbnailUrl: String,
        val url: String,
        val description: String,
    )

    override fun generate(data: List<Data>?) {
        if (data == null || data.isEmpty()) {
            // empty capturing group, e.g. "search for" without anything else
            val message: String = ctx().android!!
                .getString(
                    if (data == null)
                        R.string.skill_search_what_question
                    else
                        R.string.skill_search_no_results
                )

            ctx().speechOutputDevice!!.speak(message)
            ctx().graphicalOutputDevice!!.display(
                GraphicalOutputUtils.buildSubHeader(
                    ctx().android!!, message
                )
            )

            // try again
            setNextSkills(
                listOf(
                    ChainSkill.Builder(StandardRecognizer(Sections.getSection(SectionsGenerated.search)))
                        .process(DuckDuckGoProcessor())
                        .output(SearchOutput()),
                    ChainSkill.Builder(CaptureEverythingRecognizer())
                        .process(DuckDuckGoProcessor())
                        .output(SearchOutput())
                )
            )
            return
        }
        val output: LinearLayout = GraphicalOutputUtils.buildVerticalLinearLayout(
            ctx().android!!,
            ResourcesCompat.getDrawable(
                ctx().android!!.resources,
                R.drawable.divider_items, null
            )
        )
        for (item in data) {
            val view: View = GraphicalOutputUtils.inflate(
                ctx().android!!,
                R.layout.skill_search_result
            )
            view.findViewById<TextView>(R.id.title).text = Html.fromHtml(item.title)
            Picasso.get().load(item.thumbnailUrl).into(view.findViewById<ImageView>(R.id.thumbnail))
            view.findViewById<TextView>(R.id.description).text = Html.fromHtml(item.description)
            view.setOnClickListener { openUrlInBrowser(ctx().android!!, item.url) }
            output.addView(view)
        }
        ctx().speechOutputDevice!!.speak(
            ctx().android!!.getString(
                R.string.skill_search_here_is_what_i_found
            )
        )
        ctx().graphicalOutputDevice!!.display(output)
    }
}
