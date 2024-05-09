package org.stypox.dicio.skills.lyrics

import android.view.Gravity
import androidx.core.content.res.ResourcesCompat
import org.dicio.skill.chain.OutputGenerator
import org.stypox.dicio.R
import org.stypox.dicio.output.graphical.GraphicalOutputUtils

class LyricsOutput : OutputGenerator<LyricsOutput.Data>() {
    class Data (val title: String, val artist: String?, val lyrics: String?)

    override fun generate(data: Data) {
        if (data.lyrics == null) {
            val message = ctx().android!!.getString(
                R.string.skill_lyrics_song_not_found, data.title
            )
            ctx().speechOutputDevice!!.speak(message)
            ctx().graphicalOutputDevice!!.display(
                GraphicalOutputUtils.buildSubHeader(
                    ctx().android!!, message
                )
            )
        } else {
            ctx().speechOutputDevice!!.speak(
                ctx().android!!.getString(
                    R.string.skill_lyrics_found_song_by_artist, data.title, data.artist!!
                )
            )
            val lyricsView = GraphicalOutputUtils.buildDescription(
                ctx().android!!, data.lyrics
            )
            lyricsView.gravity = Gravity.START
            lyricsView.setPadding(8, 0, 0, 0)
            ctx().graphicalOutputDevice!!.display(
                GraphicalOutputUtils.buildVerticalLinearLayout(
                    ctx().android!!,
                    ResourcesCompat.getDrawable(
                        ctx().android!!.resources,
                        R.drawable.divider_items, null
                    ),
                    GraphicalOutputUtils.buildHeader(
                        ctx().android!!, data.title
                    ),
                    GraphicalOutputUtils.buildSubHeader(
                        ctx().android!!, data.artist
                    ),
                    lyricsView
                )
            )
        }
    }
}
