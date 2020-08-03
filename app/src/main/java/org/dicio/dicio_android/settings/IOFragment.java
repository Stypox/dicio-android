package org.dicio.dicio_android.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import org.dicio.dicio_android.R;

public class IOFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_io);
    }
}