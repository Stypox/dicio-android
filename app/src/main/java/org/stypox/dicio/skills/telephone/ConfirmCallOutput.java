package org.stypox.dicio.skills.telephone;

import static org.stypox.dicio.Sections.getSection;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.LinearLayout;

import org.dicio.skill.SkillComponent;
import org.dicio.skill.chain.ChainSkill;
import org.dicio.skill.chain.OutputGenerator;
import org.dicio.skill.standard.StandardRecognizer;
import org.dicio.skill.standard.StandardResult;
import org.dicio.skill.util.NextSkills;
import org.stypox.dicio.R;
import org.stypox.dicio.SectionsGenerated;
import org.stypox.dicio.output.graphical.GraphicalOutputUtils;

import java.util.Collections;

public final class ConfirmCallOutput extends OutputGenerator<StandardResult> {
    private final String number;

    private ConfirmCallOutput(final String number) {
        this.number = number;
    }

    @Override
    public void generate(final StandardResult data) {
        final String message;
        if (data.getSentenceId().equals("yes")) {
            call(ctx().android(), number);
            message = ctx().android()
                    .getString(R.string.skill_telephone_calling, number);
            // do not speak anything since a call has just started
        } else {
            message = ctx().android()
                    .getString(R.string.skill_telephone_not_calling);
            ctx().getSpeechOutputDevice().speak(message);
        }
        ctx().getGraphicalOutputDevice().display(
                GraphicalOutputUtils.buildSubHeader(ctx().android(), message));
    }

    public static void call(final Context context, final String number) {
        final Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        context.startActivity(callIntent);
    }

    /**
     * Writes output to the outputGenerator to ask for confirmation about calling the provided name,
     * and sets the outputGenerator's next skills to ensure that, if the user answers affirmatively,
     * the provided number is called.
     */
    public static <T extends SkillComponent & NextSkills> void callAfterConfirmation(
            final T outputGenerator, final String name, final String number) {
        final Context context = outputGenerator.ctx().android();
        final String message = context.getString(R.string.skill_telephone_confirm_call, name);
        outputGenerator.ctx().getSpeechOutputDevice().speak(message);

        final LinearLayout output
                = GraphicalOutputUtils.buildVerticalLinearLayout(context, null);
        output.addView(GraphicalOutputUtils.buildSubHeader(context, message));
        output.addView(GraphicalOutputUtils.buildDescription(context, number));
        outputGenerator.ctx().getGraphicalOutputDevice().display(output);

        // ask for confirmation using the util_yes_no section
        outputGenerator.setNextSkills(Collections.singletonList(new ChainSkill.Builder()
                .recognize(new StandardRecognizer(getSection(SectionsGenerated.util_yes_no)))
                .output(new ConfirmCallOutput(number))));
    }
}
