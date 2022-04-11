package org.dicio.dicio_android.settings;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.dicio.dicio_android.R;

public class AppearanceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        addPreferencesFromResource(R.xml.pref_appearance);

        final Preference preference = findPreference(getString(R.string.pref_key_theme));
        assert preference != null;
        preference.setOnPreferenceChangeListener((preference1, newValue) -> {
            requireActivity().recreate();
            return true;
        });
    }
}
