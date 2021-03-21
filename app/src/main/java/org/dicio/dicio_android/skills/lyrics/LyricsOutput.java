package org.dicio.dicio_android.skills.lyrics;

import android.view.Gravity;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import org.dicio.skill.SkillContext;
import org.dicio.skill.chain.OutputGenerator;
import org.dicio.skill.output.GraphicalOutputDevice;
import org.dicio.skill.output.SpeechOutputDevice;

public class LyricsOutput
        implements OutputGenerator<LyricsOutput.Data> {

    public static class Data {
        public boolean failed = false;
        public String artist, title, lyrics;
    }


    @Override
    public void generate(final Data data,
                         final SkillContext context,
                         final SpeechOutputDevice speechOutputDevice,
                         final GraphicalOutputDevice graphicalOutputDevice) {

        if (data.failed) {
            final String message = context.getAndroidContext().getString(
                    R.string.skill_lyrics_song_not_found, data.title);
            speechOutputDevice.speak(message);
            graphicalOutputDevice.display(GraphicalOutputUtils.buildSubHeader(
                    context.getAndroidContext(), message));

        } else {
            speechOutputDevice.speak(context.getAndroidContext().getString(
                    R.string.skill_lyrics_found_song_by_artist, data.title, data.artist));

            final TextView lyricsView = GraphicalOutputUtils.buildDescription(
                    context.getAndroidContext(), data.lyrics);
            lyricsView.setGravity(Gravity.START);
            lyricsView.setPadding(8, 0, 0, 0);

            graphicalOutputDevice.display(
                    GraphicalOutputUtils.buildVerticalLinearLayout(context.getAndroidContext(),
                            ResourcesCompat.getDrawable(context.getAndroidContext().getResources(),
                                    R.drawable.divider_items, null),
                            GraphicalOutputUtils.buildHeader(
                                    context.getAndroidContext(), data.title),
                            GraphicalOutputUtils.buildSubHeader(
                                    context.getAndroidContext(), data.artist),
                            lyricsView));
        }
    }

    @Override
    public void cleanup() {
    }
}
