package org.dicio.dicio_android.skills.search;

import static org.dicio.dicio_android.Sections.getSection;
import static org.dicio.dicio_android.Sentences_en.search;
import static org.dicio.dicio_android.util.ShareUtils.openUrlInBrowser;

import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.squareup.picasso.Picasso;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.SectionsGenerated;
import org.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import org.dicio.dicio_android.util.ShareUtils;
import org.dicio.skill.Skill;
import org.dicio.skill.SkillContext;
import org.dicio.skill.chain.ChainSkill;
import org.dicio.skill.chain.InputRecognizer;
import org.dicio.skill.chain.OutputGenerator;
import org.dicio.skill.output.GraphicalOutputDevice;
import org.dicio.skill.output.SpeechOutputDevice;
import org.dicio.skill.standard.InputWordRange;
import org.dicio.skill.standard.StandardRecognizer;
import org.dicio.skill.standard.StandardResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SearchOutput implements OutputGenerator<List<SearchOutput.Data>> {

    public static class Data {
        public String title, thumbnailUrl, url, description;
    }


    private boolean tryAgain = false;

    @Override
    public void generate(final List<Data> data,
                         final SkillContext context,
                         final SpeechOutputDevice speechOutputDevice,
                         final GraphicalOutputDevice graphicalOutputDevice) {
        if (data == null || data.isEmpty()) {
            // empty capturing group, e.g. "search for" without anything else

            final String message = context.getAndroidContext().getString(data == null
                    ? R.string.skill_search_what_question : R.string.skill_search_no_results);
            speechOutputDevice.speak(message);
            graphicalOutputDevice.displayTemporary(GraphicalOutputUtils.buildSubHeader(
                    context.getAndroidContext(), message));

            tryAgain = true;
            return;
        }
        tryAgain = false;

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

            view.setOnClickListener(v -> openUrlInBrowser(context.getAndroidContext(), item.url));
            output.addView(view);
        }

        speechOutputDevice.speak(context.getAndroidContext().getString(
                R.string.skill_search_here_is_what_i_found));
        graphicalOutputDevice.display(output);
    }

    @Override
    public List<Skill> nextSkills() {
        if (!tryAgain) {
            return Collections.emptyList();
        }

        return Arrays.asList(
                new ChainSkill.Builder()
                        .recognize(new StandardRecognizer(getSection(SectionsGenerated.search)))
                        .process(new DuckDuckGoProcessor())
                        .output(new SearchOutput()),
                new ChainSkill.Builder()
                        .recognize(new InputRecognizer<StandardResult>() {
                            private String input;

                            @Override
                            public Specificity specificity() {
                                return Specificity.low;
                            }

                            @Override
                            public void setInput(final String input,
                                                 final List<String> inputWords,
                                                 final List<String> normalizedInputWords) {
                                this.input = input;
                            }

                            @Override
                            public float score() {
                                return 1.0f;
                            }

                            @Override
                            public StandardResult getResult() {
                                return new StandardResult("", input, null) {
                                    @Override
                                    public String getCapturingGroup(final String name) {
                                        return input;
                                    }
                                };
                            }

                            @Override
                            public void cleanup() {
                            }
                        })
                        .process(new DuckDuckGoProcessor())
                        .output(new SearchOutput()));
    }

    @Override
    public void cleanup() {
    }
}
