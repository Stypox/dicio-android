package org.stypox.dicio.settings;

import android.os.Bundle;

import org.stypox.dicio.R;
import org.stypox.dicio.input.VoskInputDevice;

import androidx.preference.PreferenceFragmentCompat;

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
//TODO Discuss whether this is needed. At least for debugging commented
//        findPreference(getString(R.string.pref_key_input_method))
//                .setOnPreferenceChangeListener((preference, newValue) -> {
//                    VoskInputDevice.deleteCurrentModel(requireContext());
//                    return true;
//                });
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
