package org.dicio.dicio_android.skills.telephone;

import static org.dicio.dicio_android.Sentences_en.telephone;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import org.dicio.dicio_android.R;
import org.dicio.dicio_android.output.graphical.GraphicalOutputUtils;
import org.dicio.dicio_android.util.StringUtils;
import org.dicio.skill.SkillContext;
import org.dicio.skill.chain.OutputGenerator;
import org.dicio.skill.output.GraphicalOutputDevice;
import org.dicio.skill.output.SpeechOutputDevice;
import org.dicio.skill.standard.StandardResult;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;

public class TelephoneOutput implements OutputGenerator<StandardResult>{
    @SuppressLint("Range")
    public void generate(final StandardResult data,
                         final SkillContext context,
                         final SpeechOutputDevice speechOutputDevice,
                         final GraphicalOutputDevice graphicalOutputDevice) {


        final String userContactName = data.getCapturingGroup(telephone.who).trim();
        if (ContextCompat.checkSelfPermission(context.getAndroidContext(), "android.permission.READ_CONTACTS") == PackageManager.PERMISSION_GRANTED&&ContextCompat.checkSelfPermission(context.getAndroidContext(), "android.permission.CALL_PHONE") == PackageManager.PERMISSION_GRANTED) {
            ContentResolver cr = context.getAndroidContext().getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            ArrayList<Contact> names = new ArrayList<>();
            while (cur.moveToNext()) {

                String name = cur.getString(cur.getColumnIndex(Contacts.DISPLAY_NAME));
                String id = cur.getString(cur.getColumnIndex(Contacts._ID));
                if (name != null) {
                    int distance = StringUtils.levenshteinDistance(name, userContactName);
                    names.add(new Contact(name, distance, id));
                }
            }
            cur.close();
            Collections.sort(names);
            int count = 0;

            if (names.isEmpty())
                Log.d("Telephone", "No matching contact found.");
            else {
                final LinearLayout output
                        = GraphicalOutputUtils.buildVerticalLinearLayout(context.getAndroidContext(),
                        ResourcesCompat.getDrawable(context.getAndroidContext().getResources(),
                                R.drawable.divider_items, null));
                for (int z = 0; z < 5 && z < names.size(); z++) {
                    Contact name = names.get(z);
                    final View view = GraphicalOutputUtils.inflate(context.getAndroidContext(),
                            R.layout.skill_telephone_contactlist);

                    ((TextView) view.findViewById(R.id.title))
                            .setText(name.name);
                    Log.d("ID_CONTACT", name.id + name.name);
                    Cursor cur2 = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.NUMBER + " IS NOT NULL AND " + ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{name.id}, null);
                    try {
                        while (cur2.moveToNext()) {
                            String num = cur2.getString(cur2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            if (cur2.getPosition() != 0) {
                                count++;
                                final View ViewMoreNumbers = GraphicalOutputUtils.inflate(context.getAndroidContext(),
                                        R.layout.skill_telephone_contact_second_number);
                                ((TextView) ViewMoreNumbers.findViewById(R.id.description))
                                        .setText(num);
                                ViewMoreNumbers.setOnClickListener(v -> call(num, context.getAndroidContext()));
                                output.addView(ViewMoreNumbers);
                            } else {

                                ((TextView) view.findViewById(R.id.description))
                                        .setText(num);
                                view.setOnClickListener(v -> call(num, context.getAndroidContext()));
                                output.addView(view);
                            }


                        }
                        cur2.close();
                    } catch (Exception E) {
                        E.printStackTrace();
                    }
                }
                if (count != 0) {
                    speechOutputDevice.speak(context.getAndroidContext().getString(R.string.skill_telephone_found_contacts, count + ""));
                    graphicalOutputDevice.display(output);
                } else {
                    speechOutputDevice.speak(context.getAndroidContext().getString(R.string.skill_telephone_unknown_contact));
                }
            }


        }
    }



    public void call(String tel, Context context) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+tel));
        context.startActivity(callIntent);
    }




    @Override
    public void cleanup() {
    }
}
