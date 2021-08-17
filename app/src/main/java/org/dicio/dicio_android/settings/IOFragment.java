package org.dicio.dicio_android.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.input.VoskInputDevice;

public class IOFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        addPreferencesFromResource(R.xml.pref_io);

        findPreference(getString(R.string.pref_key_language))
                .setOnPreferenceChangeListener((preference, newValue) -> {
                    VoskInputDevice.deleteCurrentModel(requireContext());
                    if (getActivity() != null) {
                        getActivity().recreate();
                    }
                    return true;
                });
        findPreference(getString(R.string.pref_key_input_method))
                .setOnPreferenceChangeListener((preference, newValue) -> {
                    VoskInputDevice.deleteCurrentModel(requireContext());
                    return true;
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof SettingsActivity) {
            // make sure correct title is used even after
            final SettingsActivity settingsActivity = (SettingsActivity) getActivity();
            settingsActivity.toolbarTitle = getString(R.string.pref_io);
            settingsActivity.toolbar.setTitle(R.string.pref_io);
        }
    }
}