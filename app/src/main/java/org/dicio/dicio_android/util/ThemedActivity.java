package org.dicio.dicio_android.util;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import org.dicio.dicio_android.R;

abstract public class ThemedActivity extends AppCompatActivity {

    private int currentTheme;


    private int preferenceToTheme(@Nullable String preference) {
        if (preference == null) {
            return R.style.LightAppTheme;
        }

        if (preference.equals(getString(R.string.settings_value_theme_dark))) {
            return R.style.DarkAppTheme;
        } else /*if (preference.equals(getString(R.string.settings_value_theme_light)))*/ {
            return R.style.LightAppTheme;
        }
    }

    private int getThemeFromPreferences() {
        String preference = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.settings_key_theme), null);
        return preferenceToTheme(preference);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        currentTheme = getThemeFromPreferences();
        setTheme(currentTheme);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentTheme != getThemeFromPreferences()) {
            recreate();
        }
    }
}
