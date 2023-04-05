package org.stypox.dicio.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.stypox.dicio.R

class AppearanceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_appearance)
        findPreference<Preference>(getString(R.string.pref_key_theme))
            ?.setOnPreferenceChangeListener { _, _ ->
                requireActivity().recreate()
                true
            }
    }
}