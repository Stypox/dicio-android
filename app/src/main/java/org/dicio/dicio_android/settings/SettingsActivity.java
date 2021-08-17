package org.dicio.dicio_android.settings;

import android.os.Bundle;
import android.view.Menu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.util.LocaleAwareActivity;

public class SettingsActivity extends LocaleAwareActivity
        implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private final String toolbarTitleKey = "toolbarTitle";

    // package-private: used in IOFragment to set title after changing language
    Toolbar toolbar;
    String toolbarTitle;

    private void setThemeFromPreferences() {
        final String preference = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.pref_key_theme), "");

        if (preference.equals(getString(R.string.pref_val_theme_dark))) {
            setTheme(R.style.SettingsDarkAppTheme);
        } else {
            setTheme(R.style.SettingsLightAppTheme);
        }
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        setThemeFromPreferences();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbarTitle = getString(R.string.settings);
        toolbar.setNavigationOnClickListener(v -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                finish();
            } else {
                getSupportFragmentManager().popBackStack();
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                toolbar.setTitle(R.string.settings);
            }
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_holder, new HeaderFragment())
                    .commit();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(toolbarTitleKey, toolbarTitle);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        toolbarTitle = savedInstanceState.getString(toolbarTitleKey);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        toolbar.setTitle(toolbarTitle);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPreferenceStartFragment(final PreferenceFragmentCompat caller,
                                             final Preference pref) {
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory()
                .instantiate(getClassLoader(), pref.getFragment());
        fragment.setArguments(caller.getArguments());
        fragment.setTargetFragment(caller, 0);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_holder, fragment)
                .addToBackStack(null)
                .commit();

        toolbarTitle = pref.getTitle().toString();
        toolbar.setTitle(pref.getTitle());
        return true;
    }
}
