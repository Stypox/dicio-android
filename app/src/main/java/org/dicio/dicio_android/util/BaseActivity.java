package org.dicio.dicio_android.util;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import org.dicio.dicio_android.R;

/**
 * A base for all of the activities that automatically recreates itself when the theme or the
 * language (locale) change
 */
abstract public class BaseActivity extends LocaleAwareActivity {

    private int currentTheme;
    private boolean isRecreating = false;

    private int getThemeFromPreferences() {
        final String preference = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.pref_key_theme), "");

        if (preference.equals(getString(R.string.pref_val_theme_dark))) {
            return R.style.DarkAppTheme;
        } else {
            return R.style.LightAppTheme;
        }
    }


    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        isRecreating = false;
        currentTheme = getThemeFromPreferences();
        setTheme(currentTheme);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void recreate() {
        isRecreating = true;
        super.recreate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentTheme != getThemeFromPreferences()) {
            recreate();
        }
    }

    protected boolean isRecreating() {
        return isRecreating;
    }
}
