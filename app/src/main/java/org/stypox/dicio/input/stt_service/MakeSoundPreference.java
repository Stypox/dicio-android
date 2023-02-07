package org.stypox.dicio.input.stt_service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import org.stypox.dicio.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.MultiSelectListPreference;

/**
 * a MultiSelectListPreference which uses R.string.pref_key_stt_onbegin_nosound_entries as entries
 * and entry values
 */
public class MakeSoundPreference extends MultiSelectListPreference {
    final SharedPreferences preferences;
    final String helperPrefKey;
    final String[] ownPackageName = new String[1];

    public MakeSoundPreference(@NonNull final Context context, @Nullable final AttributeSet attrs,
                               final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        helperPrefKey = context.getString(R.string.pref_key_stt_onlisten_sound_entries);
        ownPackageName[0] = context.getPackageName();
    }

    public MakeSoundPreference(@NonNull final Context context, @Nullable final AttributeSet attrs,
                               final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        helperPrefKey = context.getString(R.string.pref_key_stt_onlisten_sound_entries);
        ownPackageName[0] = context.getPackageName();
    }

    public MakeSoundPreference(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        helperPrefKey = context.getString(R.string.pref_key_stt_onlisten_sound_entries);
        ownPackageName[0] = context.getPackageName();
    }

    public MakeSoundPreference(@NonNull final Context context) {
        super(context);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        helperPrefKey = context.getString(R.string.pref_key_stt_onlisten_sound_entries);
        ownPackageName[0] = context.getPackageName();
    }

    @Override
    public CharSequence[] getEntries() {
        final Set<String> entries = preferences.getStringSet(helperPrefKey,
                new HashSet<>(Arrays.asList(ownPackageName)));
        final String[] back = new String[entries.size()];
        int i = 0;
        for (final String e: entries) {
            back[i] = e;
            i++;
        }
        return back;
    }

    @Override
    public CharSequence[] getEntryValues() {
        return getEntries();
    }
    //
//    protected void runtimePopulateEntries(Context context){
//        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
//        settings.getStringSet()
//        final List<CharSequence> entries = new ArrayList<>(Arrays.asList(getEntries()));
//        final List<CharSequence> entriesValues = new ArrayList<>(Arrays.asList(getEntries()));
//        setEntries(entries.toArray(new CharSequence[]{}));
//        setEntryValues(entriesValues.toArray(new CharSequence[]{}));
//    }
//
//    public void addEntry(CharSequence newEntry) {
//        final Set<CharSequence> entries = new HashSet<>(Arrays.asList(getEntries()));
//        entries.add(newEntry);
//        setEntries(entries.toArray(new CharSequence[]{}));
//    }
//    public void addEntryValue(CharSequence newEntry) {
//        final List<CharSequence> entryValues = new ArrayList<>(Arrays.asList(getEntries()));
//        entryValues.add(newEntry);
//        setEntryValues(entryValues.toArray(new CharSequence[]{}));
//    }
}
