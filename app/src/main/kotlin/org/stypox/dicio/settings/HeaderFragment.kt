package org.stypox.dicio.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import org.stypox.dicio.R

class HeaderFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_header)
    }
}