package org.dicio.dicio_android.skills.search;

import static org.dicio.dicio_android.Sections.getSection;
import static org.dicio.dicio_android.util.ShareUtils.openUrlInBrowser;

import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.squareup.picasso.Picasso;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.SectionsGenerated;
import org.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import org.dicio.skill.Skill;
import org.dicio.skill.SkillContext;
import org.dicio.skill.SkillInfo;
import org.dicio.skill.chain.ChainSkill;
import org.dicio.skill.chain.InputRecognizer;
import org.dicio.skill.chain.OutputGenerator;
import org.dicio.skill.standard.StandardRecognizer;
import org.dicio.skill.standard.StandardResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SearchOutput extends OutputGenerator<List<SearchOutput.Data>> {

    public static class Data {
        public String title, thumbnailUrl, url, description;
    }

    private boolean tryAgain = false;


    public SearchOutput(SkillContext context, @Nullable SkillInfo skillInfo) {
        super(context, skillInfo);
    }

    @Override
    public void generate(final List<Data> data) {
        if (data == null || data.isEmpty()) {
            // empty capturing group, e.g. "search for" without anything else

            final String message = ctx().android().getString(data == null
                    ? R.string.skill_search_what_question : R.string.skill_search_no_results);
            ctx().getSpeechOutputDevice().speak(message);
            ctx().getGraphicalOutputDevice().display(GraphicalOutputUtils.buildSubHeader(
                    ctx().android(), message));

            tryAgain = true;
            return;
        }
        tryAgain = false;

        final LinearLayout output
                = GraphicalOutputUtils.buildVerticalLinearLayout(ctx().android(),
                ResourcesCompat.getDrawable(ctx().android().getResources(),
                        R.drawable.divider_items, null));
        for (final Data item : data) {
            final View view = GraphicalOutputUtils.inflate(ctx().android(),
                    R.layout.skill_search_result);

            ((TextView) view.findViewById(R.id.title))
                    .setText(Html.fromHtml(item.title));
            Picasso.get()
                    .load(item.thumbnailUrl).into((ImageView) view.findViewById(R.id.thumbnail));
            ((TextView) view.findViewById(R.id.description))
                    .setText(Html.fromHtml(item.description));

            view.setOnClickListener(v -> openUrlInBrowser(ctx().android(), item.url));
            output.addView(view);
        }

        ctx().getSpeechOutputDevice().speak(ctx().android().getString(
                R.string.skill_search_here_is_what_i_found));
        ctx().getGraphicalOutputDevice().display(output);
    }

    @Override
    public List<Skill> nextSkills() {
        if (!tryAgain) {
            return Collections.emptyList();
        }

        return Arrays.asList(
                new ChainSkill.Builder(ctx(), null)
                        .recognize(new StandardRecognizer(ctx(), null,
                                getSection(SectionsGenerated.search)))
                        .process(new DuckDuckGoProcessor(ctx(), null))
                        .output(new SearchOutput(ctx(), null)),
                new ChainSkill.Builder(ctx(), null)
                        .recognize(new InputRecognizer<StandardResult>(ctx(), null) {
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
                        .process(new DuckDuckGoProcessor(ctx(), null))
                        .output(new SearchOutput(ctx(), null)));
    }

    @Override
    public void cleanup() {
    }
}
