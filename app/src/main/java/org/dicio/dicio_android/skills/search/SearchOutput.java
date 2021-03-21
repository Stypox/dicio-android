package org.dicio.dicio_android.skills.search;

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
import org.dicio.skill.SkillContext;
import org.dicio.skill.chain.OutputGenerator;
import org.dicio.skill.output.GraphicalOutputDevice;
import org.dicio.skill.output.SpeechOutputDevice;

import java.util.List;

public class SearchOutput implements OutputGenerator<List<SearchOutput.Data>> {

    public static class Data {
        public String title, thumbnailUrl, url, description;
    }


    @Override
    public void generate(final List<Data> data,
                         final SkillContext context,
                         final SpeechOutputDevice speechOutputDevice,
                         final GraphicalOutputDevice graphicalOutputDevice) {

        final LinearLayout output
                = GraphicalOutputUtils.buildVerticalLinearLayout(context.getAndroidContext(),
                ResourcesCompat.getDrawable(context.getAndroidContext().getResources(),
                        R.drawable.divider_items, null));
        for (final Data item : data) {
            final View view = GraphicalOutputUtils.inflate(context.getAndroidContext(),
                    R.layout.skill_search_result);

            ((TextView) view.findViewById(R.id.title))
                    .setText(Html.fromHtml(item.title));
            Picasso.get()
                    .load(item.thumbnailUrl).into((ImageView) view.findViewById(R.id.thumbnail));
            ((TextView) view.findViewById(R.id.description))
                    .setText(Html.fromHtml(item.description));

            view.setOnClickListener(v -> ShareUtils.view(context.getAndroidContext(), item.url));
            output.addView(view);
        }

        speechOutputDevice.speak(context.getAndroidContext().getString(
                R.string.skill_search_here_is_what_i_found));
        graphicalOutputDevice.display(output);
    }

    @Override
    public void cleanup() {
    }
}
