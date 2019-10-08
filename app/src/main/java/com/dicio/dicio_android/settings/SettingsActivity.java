package com.dicio.dicio_android.settings;

import android.os.Bundle;
import android.view.Menu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.dicio.dicio_android.R;
import com.dicio.dicio_android.util.ThemedActivity;

public class SettingsActivity extends ThemedActivity
        implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private final String toolbarTitleKey = "toolbarTitle";

    Toolbar toolbar;
    String toolbarTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(toolbarTitleKey, toolbarTitle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        toolbarTitle = savedInstanceState.getString(toolbarTitleKey);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        toolbar.setTitle(toolbarTitle);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        Fragment fragment = getSupportFragmentManager().getFragmentFactory()
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
