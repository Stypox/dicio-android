package org.dicio.dicio_android.util;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.ConfigurationCompat;
import androidx.core.os.LocaleListCompat;
import androidx.preference.PreferenceManager;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.sentences.Sections;

import java.util.Locale;
import java.util.Objects;

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

    private void setLocale(@Nullable final String language) {
        try {
            @NonNull final Locale sectionsLocale;
            if (language == null || language.trim().isEmpty()) {
                sectionsLocale = Sections.setLocale(
                        ConfigurationCompat.getLocales(getResources().getConfiguration()));
            } else {
                sectionsLocale = Sections.setLocale(
                        LocaleListCompat.create(new Locale(language)));
            }

            Locale.setDefault(sectionsLocale);
            final Resources resources = getResources();
            final Configuration configuration = resources.getConfiguration();
            configuration.setLocale(sectionsLocale);
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        } catch (Sections.UnsupportedLocaleException e) {
            e.printStackTrace(); //TODO ask the user to manually choose a locale
        }
    }


    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        currentTheme = getThemeFromPreferences();
        currentLanguage = getLocaleFromPreferences();
        setTheme(currentTheme);
        setLocale(currentLanguage);
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
