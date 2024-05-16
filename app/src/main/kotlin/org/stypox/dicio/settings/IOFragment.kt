package org.stypox.dicio.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.stypox.dicio.R
import org.stypox.dicio.io.input.VoskInputDevice

class IOFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_io)
        findPreference<Preference>(getString(R.string.pref_key_language))
            ?.setOnPreferenceChangeListener { _, _ ->
                //VoskInputDevice.deleteCurrentModel(requireContext())
                activity?.recreate()
                true
            }
        findPreference<Preference>(getString(R.string.pref_key_input_method))
            ?.setOnPreferenceChangeListener { _, _ ->
                VoskInputDevice.deleteCurrentModel(requireContext())
                true
            }
    }

    override fun onResume() {
        super.onResume()
        (activity as? SettingsActivity)?.apply {
            // make sure correct title is used even after
            toolbarTitle = getString(R.string.pref_io)
            toolbar.setTitle(R.string.pref_io)
        }
    }
}
