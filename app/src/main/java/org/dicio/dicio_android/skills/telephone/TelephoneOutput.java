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
import org.dicio.skill.SkillContext;
import org.dicio.skill.chain.ChainSkill;
import org.dicio.skill.chain.OutputGenerator;
import org.dicio.skill.output.GraphicalOutputDevice;
import org.dicio.skill.output.SpeechOutputDevice;
import org.dicio.skill.standard.StandardRecognizer;
import org.dicio.skill.standard.StandardResult;

import java.util.Collections;
import java.util.List;

public class TelephoneOutput implements OutputGenerator<StandardResult> {

    @Nullable String numberToCallAfterConfirmation = null;

    public void generate(final StandardResult data,
                         final SkillContext skillContext,
                         final SpeechOutputDevice speechOutputDevice,
                         final GraphicalOutputDevice graphicalOutputDevice) {
        final Context context = skillContext.getAndroidContext();
        final ContentResolver contentResolver = context.getContentResolver();
        final String userContactName = data.getCapturingGroup(telephone.who).trim();
        final List<Contact> contacts
                = Contact.getFilteredSortedContacts(contentResolver, userContactName);

        int contactCount = 0;
        final LinearLayout output = GraphicalOutputUtils.buildVerticalLinearLayout(context,
                ResourcesCompat.getDrawable(skillContext.getAndroidContext().getResources(),
                        R.drawable.divider_items, null));

        for (int i = 0; contactCount < 5 && i < contacts.size(); ++i) {
            final Contact contact = contacts.get(i);
            final List<String> numbers = contact.getNumbers(contentResolver);
            if (numbers.isEmpty()) {
                continue;
            }
            if (contactCount == 0 && contact.getDistance() < 3 && numbers.size() == 1) {
                // very close match with just one number: call it directly
                callAfterConfirmation(contact.getName(), numbers.get(0), context,
                        speechOutputDevice, graphicalOutputDevice);
                return;
            }
            ++contactCount;

            addNumbersToOutput(contact, numbers, output, context);
        }

        // this point will not be reached if a very close match was found
        if (contactCount == 0) {
            speechOutputDevice.speak(skillContext.getAndroidContext().getString(
                    R.string.skill_telephone_unknown_contact));
        } else {
            speechOutputDevice.speak(skillContext.getAndroidContext().getString(
                    R.string.skill_telephone_found_contacts, contactCount));
            graphicalOutputDevice.display(output);
        }
    }

    @Override
    public List<Skill> nextSkills() {
        if (numberToCallAfterConfirmation == null) {
            return OutputGenerator.super.nextSkills();
        } else {
            final String number = numberToCallAfterConfirmation;
            return Collections.singletonList(new ChainSkill.Builder(null)
                    .recognize(new StandardRecognizer(getSection(SectionsGenerated.util_yes_no)))
                    .output(new OutputGenerator<StandardResult>() {
                        @Override
                        public void generate(final StandardResult data,
                                             final SkillContext skillContext,
                                             final SpeechOutputDevice speechOutputDevice,
                                             final GraphicalOutputDevice graphicalOutputDevice) {
                            final Context context = skillContext.getAndroidContext();
                            final String message;
                            if (data.getSentenceId().equals("yes")) {
                                call(context, number);
                                message = context.getString(
                                        R.string.skill_telephone_calling, number);
                                // do not speak anything since a call has started
                            } else {
                                message = context.getString(R.string.skill_telephone_not_calling);
                                speechOutputDevice.speak(message);
                            }
                            graphicalOutputDevice.display(
                                    GraphicalOutputUtils.buildSubHeader(context, message));
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
                                       final String number,
                                       final Context context,
                                       final SpeechOutputDevice speechOutputDevice,
                                       final GraphicalOutputDevice graphicalOutputDevice) {
        numberToCallAfterConfirmation = number;

        final String message = context.getString(R.string.skill_telephone_confirm_call, name);
        speechOutputDevice.speak(message);

        final LinearLayout output = GraphicalOutputUtils.buildVerticalLinearLayout(context, null);
        output.addView(GraphicalOutputUtils.buildSubHeader(context, message));
        output.addView(GraphicalOutputUtils.buildDescription(context, number));
        graphicalOutputDevice.display(output);
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
