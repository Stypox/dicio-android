package org.dicio.dicio_android.settings;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.input.VoskInputDevice;

public class IOFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_io);

        final Preference.OnPreferenceChangeListener deleteVoskModel = (preference, newValue) -> {
            VoskInputDevice.deleteCurrentModel(requireContext());
            return true;
        };

        findPreference(getString(R.string.settings_key_language))
                .setOnPreferenceChangeListener(deleteVoskModel);
        findPreference(getString(R.string.settings_key_input_method))
                .setOnPreferenceChangeListener(deleteVoskModel);
    }
}