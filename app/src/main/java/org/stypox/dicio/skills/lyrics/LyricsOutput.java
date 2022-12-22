package org.stypox.dicio.skills.lyrics;

import android.view.Gravity;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import org.stypox.dicio.R;
import org.stypox.dicio.output.graphical.GraphicalOutputUtils;
import org.dicio.skill.chain.OutputGenerator;

public class LyricsOutput extends OutputGenerator<LyricsOutput.Data> {

    public static class Data {
        public boolean failed = false;
        public String artist;
        public String title;
        public String lyrics;
    }


    @Override
    public void generate(final Data data) {

        if (data.failed) {
            final String message = ctx().android().getString(
                    R.string.skill_lyrics_song_not_found, data.title);
            ctx().getSpeechOutputDevice().speak(message);
            ctx().getGraphicalOutputDevice().display(GraphicalOutputUtils.buildSubHeader(
                    ctx().android(), message));

        } else {
            ctx().getSpeechOutputDevice().speak(ctx().android().getString(
                    R.string.skill_lyrics_found_song_by_artist, data.title, data.artist));

            final TextView lyricsView = GraphicalOutputUtils.buildDescription(
                    ctx().android(), data.lyrics);
            lyricsView.setGravity(Gravity.START);
            lyricsView.setPadding(8, 0, 0, 0);

            ctx().getGraphicalOutputDevice().display(
                    GraphicalOutputUtils.buildVerticalLinearLayout(ctx().android(),
                            ResourcesCompat.getDrawable(ctx().android().getResources(),
                                    R.drawable.divider_items, null),
                            GraphicalOutputUtils.buildHeader(
                                    ctx().android(), data.title),
                            GraphicalOutputUtils.buildSubHeader(
                                    ctx().android(), data.artist),
                            lyricsView));
        }
    }

    @Override
    public void cleanup() {
    }
}
