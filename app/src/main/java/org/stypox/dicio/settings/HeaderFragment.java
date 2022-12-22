package org.stypox.dicio.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import org.stypox.dicio.R;

public class HeaderFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        addPreferencesFromResource(R.xml.pref_header);
    }
}
