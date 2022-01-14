package org.dicio.dicio_android.skills.telephone;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;

import org.dicio.dicio_android.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Contact {

    private static final String TAG = Contact.class.getSimpleName();
    private static final String NUMBERS_QUERY = ContactsContract.CommonDataKinds.Phone.NUMBER
            + " IS NOT NULL AND " + ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";

    private final String name;
    private final int distance;
    private final String id;

    public Contact(final String name, final int distance, final String id){
        this.name = name;
        this.distance = distance;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    @NonNull
    public List<String> getNumbers(final ContentResolver contentResolver) {
        final List<String> numbers = new ArrayList<>();
        try (Cursor phoneNumberCursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, NUMBERS_QUERY, new String[]{ id }, null)) {

            final int numberColumnIndex = phoneNumberCursor
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            while (phoneNumberCursor.moveToNext()) {
                final String number = phoneNumberCursor.getString(numberColumnIndex);
                if (!StringUtils.isNullOrEmpty(number)) {
                    numbers.add(number);
                }
            }
        } catch (final Exception e) {
            Log.w(TAG, "Could not get numbers for contact " + name, e);
        }
        return numbers;
    }

    public static List<Contact> getSortedContacts(final ContentResolver contentResolver,
                                                  final String userContactName) {
        final List<Contact> contacts = new ArrayList<>();
        try (Cursor contactCursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null)) {
            final int contactNameColumnIndex
                    = contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            final int contactIdColumnIndex
                    = contactCursor.getColumnIndex(ContactsContract.Contacts._ID);
            while (contactCursor.moveToNext()) {
                final String name = contactCursor.getString(contactNameColumnIndex);
                final String id = contactCursor.getString(contactIdColumnIndex);
                if (name != null) {
                    final int distance = StringUtils.levenshteinDistance(name, userContactName);
                    contacts.add(new Contact(name, distance, id));
                }
            }
        }

        //noinspection ComparatorCombinators
        Collections.sort(contacts, (c1, c2) -> Integer.compare(c1.distance, c2.distance));
        return contacts;
    }
}
