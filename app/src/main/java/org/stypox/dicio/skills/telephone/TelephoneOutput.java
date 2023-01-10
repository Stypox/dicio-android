package org.stypox.dicio.skills.telephone;

import static org.stypox.dicio.Sentences_en.telephone;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import org.dicio.skill.Skill;
import org.dicio.skill.chain.OutputGenerator;
import org.dicio.skill.standard.StandardResult;
import org.stypox.dicio.R;
import org.stypox.dicio.output.graphical.GraphicalOutputUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TelephoneOutput extends OutputGenerator<StandardResult> {

    public void generate(final StandardResult data) {
        final ContentResolver contentResolver = ctx().android().getContentResolver();
        final String userContactName = data.getCapturingGroup(telephone.who).trim();
        final List<Contact> contacts
                = Contact.getFilteredSortedContacts(contentResolver, userContactName);

        final List<Pair<String, List<String>>> validContacts = new ArrayList<>();
        final LinearLayout output = GraphicalOutputUtils.buildVerticalLinearLayout(ctx().android(),
                ResourcesCompat.getDrawable(ctx().android().getResources(),
                        R.drawable.divider_items, null));

        for (int i = 0; validContacts.size() < 5 && i < contacts.size(); ++i) {
            final Contact contact = contacts.get(i);
            final List<String> numbers = contact.getNumbers(contentResolver);
            if (numbers.isEmpty()) {
                continue;
            }

            if (validContacts.isEmpty() // the first (i.e. lowest-distance) valid contact
                    && contact.getDistance() < 3 // a low enough distance
                    && numbers.size() == 1 // it has just one number
                    && (contacts.size() <= i + 1 // the next contact (if any) has a higher distance
                    || contacts.get(i + 1).getDistance() > contact.getDistance())) {
                // very close match with just one number and without distance ties: call it directly
                ConfirmCallOutput.callAfterConfirmation(this, contact.getName(), numbers.get(0));
                return;
            }

            validContacts.add(new Pair<>(contact.getName(), numbers));
            addNumbersToOutput(contact, numbers, output, ctx().android());
        }

        // this point will not be reached if a very close match was found
        if (validContacts.isEmpty()) {
            ctx().getSpeechOutputDevice().speak(ctx().android().getString(
                    R.string.skill_telephone_unknown_contact));
        } else {
            ctx().getSpeechOutputDevice().speak(ctx().android().getString(
                    R.string.skill_telephone_found_contacts, validContacts.size()));
            ctx().getGraphicalOutputDevice().display(output);

            if (validContacts.size() == 1 // there is exactly one valid contact and ...
                    // ... either it has exactly one number, or we would be forced to use
                    // RecognizeNameIO in any case, which only uses the first number anyway
                    && (validContacts.get(0).second.size() == 1
                    || ctx().getNumberParserFormatter() == null)) {
                // not a good enough match, but since we have only this, call it directly
                final Pair<String, List<String>> contact = validContacts.get(0);
                ConfirmCallOutput.callAfterConfirmation(this, contact.first, contact.second.get(0));

            } else {
                // ask the user which one to call (assumes items in validContacts are in the same
                // order as those displayed by addNumbersToOutput)
                final List<Skill> nextSkills = new ArrayList<>();
                final List<NameNumberPair> recognizeNameContacts = validContacts.stream()
                        // when saying the name, there is no way to distinguish between
                        // different numbers, so just use the first one
                        .map(pair -> new NameNumberPair(pair.first, pair.second.get(0)))
                        .collect(Collectors.toList());
                nextSkills.add(new ContactChooserName(recognizeNameContacts));

                if (ctx().getNumberParserFormatter() != null) {
                    final List<NameNumberPair> recognizeIndexContacts = validContacts.stream()
                            .flatMap(pair -> pair.second.stream()
                                    .map(number -> new NameNumberPair(pair.first, number)))
                            .collect(Collectors.toList());
                    nextSkills.add(new ContactChooserIndex(recognizeIndexContacts));
                }

                setNextSkills(nextSkills);
            }
        }
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
            view.setOnClickListener(v -> ConfirmCallOutput.call(context, number));
            output.addView(view);
        }
    }
}
