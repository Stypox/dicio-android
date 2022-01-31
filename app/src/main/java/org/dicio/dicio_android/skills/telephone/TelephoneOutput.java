package org.dicio.dicio_android.skills.telephone;

import static org.dicio.dicio_android.Sentences_en.telephone;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import org.dicio.skill.SkillContext;
import org.dicio.skill.chain.OutputGenerator;
import org.dicio.skill.output.GraphicalOutputDevice;
import org.dicio.skill.output.SpeechOutputDevice;
import org.dicio.skill.standard.StandardResult;

import java.util.List;

public class TelephoneOutput implements OutputGenerator<StandardResult> {

    public void generate(final StandardResult data,
                         final SkillContext context,
                         final SpeechOutputDevice speechOutputDevice,
                         final GraphicalOutputDevice graphicalOutputDevice) {
        final String userContactName = data.getCapturingGroup(telephone.who).trim();
        final ContentResolver contentResolver = context.getAndroidContext().getContentResolver();
        final List<Contact> contacts = Contact.getSortedContacts(contentResolver, userContactName);

        LinearLayout output = null;
        int contactCount = 0;
        if (!contacts.isEmpty()) {
            output = GraphicalOutputUtils.buildVerticalLinearLayout(context.getAndroidContext(),
                    ResourcesCompat.getDrawable(context.getAndroidContext().getResources(),
                            R.drawable.divider_items, null));

            for (int i = 0; i < 5 && i < contacts.size(); ++i) {
                final Contact contact = contacts.get(i);
                final List<String> numbers = contact.getNumbers(contentResolver);
                if (numbers.isEmpty()) {
                    continue;
                }
                ++contactCount;

                for (int j = 0; j < contacts.size(); ++j) {
                    final View view;
                    if (j == 0) {
                        view = GraphicalOutputUtils.inflate(context.getAndroidContext(),
                                R.layout.skill_telephone_contactlist);
                        ((TextView) view.findViewById(R.id.title)).setText(contact.getName());
                    } else {
                        // a contact can have multiple associated numbers
                        view = GraphicalOutputUtils.inflate(context.getAndroidContext(),
                                R.layout.skill_telephone_contact_second_number);
                    }

                    final String number = numbers.get(i);
                    ((TextView) view.findViewById(R.id.description)).setText(number);
                    view.setOnClickListener(v -> call(context.getAndroidContext(), number));
                    output.addView(view);
                }
            }
        }

        if (contactCount == 0) {
            speechOutputDevice.speak(context.getAndroidContext().getString(
                    R.string.skill_telephone_unknown_contact));
        } else {
            speechOutputDevice.speak(context.getAndroidContext().getString(
                    R.string.skill_telephone_found_contacts, contactCount));
            graphicalOutputDevice.display(output);
        }
    }

    @Override
    public void cleanup() {
    }

    private static void call(final Context context, final String number) {
        final Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        context.startActivity(callIntent);
    }
}
