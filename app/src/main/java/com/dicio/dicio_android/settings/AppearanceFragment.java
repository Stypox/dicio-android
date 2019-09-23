package com.dicio.dicio_android.settings;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.dicio.dicio_android.R;

public class AppearanceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_appearance);

        Preference preference = findPreference("theme");
        assert preference != null;
        preference.setOnPreferenceChangeListener((preference1, newValue) -> {
            requireActivity().recreate();
            return true;
        });
    }
}