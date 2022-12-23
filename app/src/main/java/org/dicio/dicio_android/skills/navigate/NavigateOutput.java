package org.dicio.dicio_android.skills.navigate;

import android.content.Intent;
import android.net.Uri;

import org.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import org.dicio.skill.chain.OutputGenerator;

import java.util.Locale;

public class NavigateOutput extends OutputGenerator<NavigateOutput.Data> {

    public static class Data {
        public boolean failed = false;
        public String address;
    }

    @Override
    public void generate(final Data data) {
        if (data.failed) {
            final String message = "Specify where you want to navigate to";
            ctx().getSpeechOutputDevice().speak(message);
            ctx().getGraphicalOutputDevice().display(GraphicalOutputUtils.buildSubHeader(
                    ctx().android(), message));
            return;
        }

        final String message = "Navigating to " + data.address;
        ctx().getSpeechOutputDevice().speak(message);
        ctx().getGraphicalOutputDevice().display(GraphicalOutputUtils.buildSubHeader(
                ctx().android(), message));

        final String uriGeoSimple = String.format(Locale.ENGLISH,
                "geo:0,0?q=%s", data.address);

        final Intent launchIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(uriGeoSimple));
        ctx().android().startActivity(launchIntent);

    }

    @Override
    public void cleanup() {
    }
}
