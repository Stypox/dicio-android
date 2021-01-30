package org.dicio.dicio_android.skills.lyrics;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import org.dicio.skill.chain.OutputGenerator;
import org.dicio.skill.output.GraphicalOutputDevice;
import org.dicio.skill.output.SpeechOutputDevice;

import java.util.Locale;

public class LyricsOutput
        implements OutputGenerator<LyricsOutput.Data> {

    public static class Data {
        public boolean failed = false;
        public String artist, title, lyrics;
    }


    @Override
    public void generate(final Data data,
                         final Context context,
                         final SharedPreferences preferences,
                         final Locale locale,
                         final SpeechOutputDevice speechOutputDevice,
                         final GraphicalOutputDevice graphicalOutputDevice) {

        if (data.failed) {
            final String message =
                    context.getString(R.string.skill_lyrics_song_not_found, data.title);
            speechOutputDevice.speak(message);
            graphicalOutputDevice.display(GraphicalOutputUtils.buildSubHeader(context, message));

        } else {
            speechOutputDevice.speak(
                    context.getString(R.string.skill_lyrics_found_song_by_artist,
                            data.title, data.artist));

            final TextView lyricsView = GraphicalOutputUtils.buildDescription(context, data.lyrics);
            lyricsView.setGravity(Gravity.START);
            lyricsView.setPadding(8, 0, 0, 0);

            graphicalOutputDevice.display(
                    GraphicalOutputUtils.buildVerticalLinearLayout(context,
                            ResourcesCompat.getDrawable(context.getResources(),
                                    R.drawable.divider_items, null),
                            GraphicalOutputUtils.buildHeader(context, data.title),
                            GraphicalOutputUtils.buildSubHeader(context, data.artist),
                            lyricsView));
        }
    }

    @Override
    public void cleanup() {
    }
}
