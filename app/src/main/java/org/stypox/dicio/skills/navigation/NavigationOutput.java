package org.stypox.dicio.skills.navigation;

import static org.stypox.dicio.Sections.getSection;

import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;

import org.dicio.skill.chain.CaptureEverythingRecognizer;
import org.dicio.skill.chain.ChainSkill;
import org.dicio.skill.chain.OutputGenerator;
import org.dicio.skill.standard.StandardRecognizer;
import org.stypox.dicio.R;
import org.stypox.dicio.SectionsGenerated;
import org.stypox.dicio.output.graphical.GraphicalOutputUtils;
import org.stypox.dicio.util.StringUtils;

import java.util.Arrays;
import java.util.Locale;

public class NavigationOutput extends OutputGenerator<String> {

    @Override
    public void generate(@Nullable final String data) {
        final String message;
        if (StringUtils.isNullOrEmpty(data)) {
            message = ctx().android().getString(R.string.skill_navigation_specify_where);

            // try again
            setNextSkills(Arrays.asList(
                    new ChainSkill.Builder()
                            .recognize(new StandardRecognizer(
                                    getSection(SectionsGenerated.navigation)))
                            .process(new NavigationProcessor())
                            .output(new NavigationOutput()),
                    new ChainSkill.Builder()
                            .recognize(new CaptureEverythingRecognizer())
                            .process(new NavigationProcessor())
                            .output(new NavigationOutput())));

        } else {
            message = ctx().android().getString(R.string.skill_navigation_navigating_to, data);

            final String uriGeoSimple = String.format(Locale.ENGLISH, "geo:0,0?q=%s", data);
            final Intent launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriGeoSimple));
            ctx().android().startActivity(launchIntent);
        }

        ctx().getSpeechOutputDevice().speak(message);
        ctx().getGraphicalOutputDevice().display(GraphicalOutputUtils.buildSubHeader(
                ctx().android(), message));
    }
}
