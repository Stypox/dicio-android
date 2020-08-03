package org.dicio.dicio_android.components.output;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.output.OutputGenerator;
import org.dicio.dicio_android.output.graphical.GraphicalOutputDevice;
import org.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import org.dicio.dicio_android.output.speech.SpeechOutputDevice;
import org.dicio.dicio_android.util.ShareUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SearchOutput implements OutputGenerator<List<SearchOutput.Data>> {

    public static class Data {
        public String title, thumbnailUrl, url, description;
    }


    @Override
    public void generate(List<Data> data,
                         Context context,
                         SpeechOutputDevice speechOutputDevice,
                         GraphicalOutputDevice graphicalOutputDevice) {

        LinearLayout output = GraphicalOutputUtils.buildContainer(context,
                context.getResources().getDrawable(R.drawable.divider_items));
        for (Data item : data) {
            View view = GraphicalOutputUtils.inflate(context, R.layout.component_search_result);

            ((TextView) view.findViewById(R.id.title))
                    .setText(Html.fromHtml(item.title));
            Picasso.get()
                    .load(item.thumbnailUrl).into((ImageView) view.findViewById(R.id.thumbnail));
            ((TextView) view.findViewById(R.id.description))
                    .setText(Html.fromHtml(item.description));

            view.setOnClickListener(v -> ShareUtils.view(context, item.url));
            output.addView(view);
        }

        speechOutputDevice.speak("Here is what I have found");
        graphicalOutputDevice.display(output);
    }
}
