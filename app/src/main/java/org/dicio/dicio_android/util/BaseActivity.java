package org.dicio.dicio_android.util;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.Sections;

import java.util.Locale;
import java.util.Objects;

import static org.dicio.dicio_android.util.LocaleUtils.getAvailableLocalesFromPreferences;

abstract public class BaseActivity extends AppCompatActivity {

    private int currentTheme;
    @Nullable private String currentLanguage = null;

    private int getThemeFromPreferences() {
        final String preference = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.settings_key_theme), null);

        if (preference == null) {
            return R.style.LightAppTheme;
        }

        if (preference.equals(getString(R.string.settings_value_theme_dark))) {
            return R.style.DarkAppTheme;
        } else /*if (preference.equals(getString(R.string.settings_value_theme_light)))*/ {
            return R.style.LightAppTheme;
        }
    }

    private String getLocaleFromPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.settings_key_language), null);
    }

    private void setLocale() {
        try {
            @NonNull final Locale sectionsLocale = Sections.setLocale(
                    getAvailableLocalesFromPreferences(this));

            Locale.setDefault(sectionsLocale);
            final Resources resources = getResources();
            final Configuration configuration = resources.getConfiguration();
            configuration.setLocale(sectionsLocale);
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        } catch (LocaleUtils.UnsupportedLocaleException e) {
            e.printStackTrace(); //TODO ask the user to manually choose a locale
        }
    }


    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        currentTheme = getThemeFromPreferences();
        currentLanguage = getLocaleFromPreferences();
        setTheme(currentTheme);
        setLocale();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentTheme != getThemeFromPreferences()
                || !Objects.equals(currentLanguage, getLocaleFromPreferences())) {
            recreate();
        }
    }
}
