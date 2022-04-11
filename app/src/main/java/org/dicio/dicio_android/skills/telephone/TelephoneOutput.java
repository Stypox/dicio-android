package org.dicio.dicio_android.skills.telephone;

import static org.dicio.dicio_android.Sections.getSection;
import static org.dicio.dicio_android.Sentences_en.telephone;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.SectionsGenerated;
import org.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import org.dicio.skill.Skill;
import org.dicio.skill.chain.ChainSkill;
import org.dicio.skill.chain.OutputGenerator;
import org.dicio.skill.standard.StandardRecognizer;
import org.dicio.skill.standard.StandardResult;

import java.util.Collections;
import java.util.List;

public class TelephoneOutput extends OutputGenerator<StandardResult> {

    @Nullable String numberToCallAfterConfirmation = null;


    public void generate(final StandardResult data) {
        final ContentResolver contentResolver = ctx().android().getContentResolver();
        final String userContactName = data.getCapturingGroup(telephone.who).trim();
        final List<Contact> contacts
                = Contact.getFilteredSortedContacts(contentResolver, userContactName);

        int contactCount = 0;
        final LinearLayout output = GraphicalOutputUtils.buildVerticalLinearLayout(ctx().android(),
                ResourcesCompat.getDrawable(ctx().android().getResources(),
                        R.drawable.divider_items, null));

        for (int i = 0; contactCount < 5 && i < contacts.size(); ++i) {
            final Contact contact = contacts.get(i);
            final List<String> numbers = contact.getNumbers(contentResolver);
            if (numbers.isEmpty()) {
                continue;
            }
            if (contactCount == 0 && contact.getDistance() < 3 && numbers.size() == 1) {
                // very close match with just one number: call it directly
                callAfterConfirmation(contact.getName(), numbers.get(0));
                return;
            }
            ++contactCount;

            addNumbersToOutput(contact, numbers, output, ctx().android());
        }

        // this point will not be reached if a very close match was found
        if (contactCount == 0) {
            ctx().getSpeechOutputDevice().speak(ctx().android().getString(
                    R.string.skill_telephone_unknown_contact));
        } else {
            ctx().getSpeechOutputDevice().speak(ctx().android().getString(
                    R.string.skill_telephone_found_contacts, contactCount));
            ctx().getGraphicalOutputDevice().display(output);
        }
    }

    @Override
    public List<Skill> nextSkills() {
        if (numberToCallAfterConfirmation == null) {
            return super.nextSkills();
        } else {
            final String number = numberToCallAfterConfirmation;
            return Collections.singletonList(new ChainSkill.Builder()
                    .recognize(new StandardRecognizer(getSection(SectionsGenerated.util_yes_no)))
                    .output(new OutputGenerator<StandardResult>() {
                        @Override
                        public void generate(final StandardResult data) {
                            final String message;
                            if (data.getSentenceId().equals("yes")) {
                                call(ctx().android(), number);
                                message = ctx().android()
                                        .getString(R.string.skill_telephone_calling, number);
                                // do not speak anything since a call has started
                            } else {
                                message = ctx().android()
                                        .getString(R.string.skill_telephone_not_calling);
                                ctx().getSpeechOutputDevice().speak(message);
                            }
                            ctx().getGraphicalOutputDevice().display(
                                    GraphicalOutputUtils.buildSubHeader(ctx().android(), message));
                        }

                        @Override
                        public void cleanup() {
                        }
                    }));
        }
    }

    @Override
    public void cleanup() {
        numberToCallAfterConfirmation = null;
    }

    private void callAfterConfirmation(final String name,
                                       final String number) {
        numberToCallAfterConfirmation = number;

        final String message = ctx().android()
                .getString(R.string.skill_telephone_confirm_call, name);
        ctx().getSpeechOutputDevice().speak(message);

        final LinearLayout output
                = GraphicalOutputUtils.buildVerticalLinearLayout(ctx().android(), null);
        output.addView(GraphicalOutputUtils.buildSubHeader(ctx().android(), message));
        output.addView(GraphicalOutputUtils.buildDescription(ctx().android(), number));
        ctx().getGraphicalOutputDevice().display(output);
    }

    private void addNumbersToOutput(final Contact contact,
                                    final List<String> numbers,
                                    final LinearLayout output,
                                    final Context context) {
        for (int j = 0; j < numbers.size(); ++j) {
            final View view;
            if (j == 0) {
                view = GraphicalOutputUtils.inflate(context, R.layout.skill_telephone_contact);
                ((TextView) view.findViewById(R.id.contact_name))
                        .setText(contact.getName());
            } else {
                // a contact can have multiple associated numbers
                view = GraphicalOutputUtils.inflate(context,
                        R.layout.skill_telephone_contact_secondary_number);
            }

            final String number = numbers.get(j);
            ((TextView) view.findViewById(R.id.contact_number)).setText(number);
            view.setOnClickListener(v -> call(context, number));
            output.addView(view);
        }
    }

    private static void call(final Context context, final String number) {
        final Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        context.startActivity(callIntent);
    }
}
