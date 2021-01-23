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

/**
 * A base for all of the activities that automatically recreates itself when the language (locale)
 * changes
 */
public class LocaleAwareActivity extends AppCompatActivity {

    @Nullable
    private String currentLanguage = null;

    private String getLocaleFromPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.pref_key_language), null);
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
        currentLanguage = getLocaleFromPreferences();
        setLocale();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Objects.equals(currentLanguage, getLocaleFromPreferences())) {
            recreate();
        }
    }
}