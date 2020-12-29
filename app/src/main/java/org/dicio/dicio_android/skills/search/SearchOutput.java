package org.dicio.dicio_android.skills.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.squareup.picasso.Picasso;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import org.dicio.dicio_android.util.ShareUtils;
import org.dicio.skill.chain.OutputGenerator;
import org.dicio.skill.output.GraphicalOutputDevice;
import org.dicio.skill.output.SpeechOutputDevice;

import java.util.List;
import java.util.Locale;

public class SearchOutput implements OutputGenerator<List<SearchOutput.Data>> {

    public static class Data {
        public String title, thumbnailUrl, url, description;
    }


    @Override
    public void generate(final List<Data> data,
                         final Context context,
                         final SharedPreferences preferences,
                         final Locale locale,
                         final SpeechOutputDevice speechOutputDevice,
                         final GraphicalOutputDevice graphicalOutputDevice) {

        final LinearLayout output = GraphicalOutputUtils.buildContainer(context, ResourcesCompat
                .getDrawable(context.getResources(), R.drawable.divider_items, null));
        for (Data item : data) {
            final View view =
                    GraphicalOutputUtils.inflate(context, R.layout.skill_search_result);

            ((TextView) view.findViewById(R.id.title))
                    .setText(Html.fromHtml(item.title));
            Picasso.get()
                    .load(item.thumbnailUrl).into((ImageView) view.findViewById(R.id.thumbnail));
            ((TextView) view.findViewById(R.id.description))
                    .setText(Html.fromHtml(item.description));

            view.setOnClickListener(v -> ShareUtils.view(context, item.url));
            output.addView(view);
        }

        speechOutputDevice.speak(context.getString(R.string.skill_search_here_is_what_i_found));
        graphicalOutputDevice.display(output);
    }
}
