package org.stypox.dicio.settings

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.widget.Toolbar
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.stypox.dicio.R
import org.stypox.dicio.util.BaseActivity

class SettingsActivity : BaseActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private val toolbarTitleKey = "toolbarTitle"

    // package-private: used in IOFragment to set title after changing language
    lateinit var toolbar: Toolbar
    var toolbarTitle: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbarTitle = getString(R.string.settings)
        toolbar.setNavigationOnClickListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                finish()
            } else {
                supportFragmentManager.popBackStack()
            }
        }
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                toolbar.setTitle(R.string.settings)
            }
        }
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_holder, HeaderFragment())
                .commit()
        }
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(toolbarTitleKey, toolbarTitle)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        toolbarTitle = savedInstanceState.getString(toolbarTitleKey)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        toolbar.title = toolbarTitle
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        val fragment = supportFragmentManager.fragmentFactory
            .instantiate(classLoader, pref.fragment!!)
        fragment.arguments = caller.arguments
        fragment.setTargetFragment(caller, 0)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_holder, fragment)
            .addToBackStack(null)
            .commit()
        toolbarTitle = pref.title.toString()
        toolbar.title = pref.title
        return true
    }
}