package org.dicio.dicio_android.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import org.dicio.dicio_android.R;

public class HeaderFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        addPreferencesFromResource(R.xml.pref_header);
    }
}
