package org.dicio.dicio_android.skills.navigation;

import static org.dicio.dicio_android.Sections.getSection;

import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.SectionsGenerated;
import org.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import org.dicio.dicio_android.util.StringUtils;
import org.dicio.skill.Skill;
import org.dicio.skill.chain.ChainSkill;
import org.dicio.skill.chain.InputRecognizer;
import org.dicio.skill.chain.OutputGenerator;
import org.dicio.skill.standard.StandardRecognizer;
import org.dicio.skill.standard.StandardResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class NavigationOutput extends OutputGenerator<String> {

    private boolean tryAgain = false;

    @Override
    public void generate(@Nullable final String data) {
        if (StringUtils.isNullOrEmpty(data)) {
            final String msg = ctx().android().getString(R.string.skill_navigation_specify_where);
            ctx().getSpeechOutputDevice().speak(msg);
            ctx().getGraphicalOutputDevice().display(GraphicalOutputUtils.buildSubHeader(
                    ctx().android(), msg));
            tryAgain = true;
            return;
        }
        tryAgain = false;

        final String msg = ctx().android().getString(R.string.skill_navigation_navigating_to, data);
        ctx().getSpeechOutputDevice().speak(msg);
        ctx().getGraphicalOutputDevice().display(GraphicalOutputUtils.buildSubHeader(
                ctx().android(), msg));

        final String uriGeoSimple = String.format(Locale.ENGLISH, "geo:0,0?q=%s", data);
        final Intent launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriGeoSimple));
        ctx().android().startActivity(launchIntent);
    }

    @Override
    public List<Skill> nextSkills() {
        if (!tryAgain) {
            return Collections.emptyList();
        }

        return Arrays.asList(
                new ChainSkill.Builder()
                        .recognize(new StandardRecognizer(getSection(SectionsGenerated.navigation)))
                        .process(new NavigationProcessor())
                        .output(new NavigationOutput()),
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
                        .process(new NavigationProcessor())
                        .output(new NavigationOutput()));
    }

    @Override
    public void cleanup() {
    }
}
