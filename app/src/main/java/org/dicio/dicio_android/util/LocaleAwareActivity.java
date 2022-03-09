package org.dicio.dicio_android.util;

import static org.dicio.dicio_android.util.LocaleUtils.getAvailableLocalesFromPreferences;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.LocaleListCompat;
import androidx.preference.PreferenceManager;

import org.dicio.dicio_android.R;
import org.dicio.dicio_android.Sections;
import org.dicio.dicio_android.skills.SkillHandler;

import java.util.Locale;
import java.util.Objects;

/**
 * A base for all of the activities that automatically recreates itself when the language (locale)
 * changes
 */
public abstract class LocaleAwareActivity extends AppCompatActivity {

    public static final String TAG = LocaleAwareActivity.class.getSimpleName();

    @Nullable
    private String currentLanguage = null;

    private String getLocaleFromPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.pref_key_language), null);
    }

    private void setLocale() {
        @NonNull Locale sectionsLocale;
        try {
            sectionsLocale = Sections.setLocale(getAvailableLocalesFromPreferences(this));

        } catch (final LocaleUtils.UnsupportedLocaleException e) {
            Log.w(TAG, "Current locale is not supported, defaulting to English", e);
            try {
                // TODO ask the user to manually choose a locale instead of defaulting to english
                sectionsLocale = Sections.setLocale(LocaleListCompat.create(Locale.ENGLISH));
            } catch (final LocaleUtils.UnsupportedLocaleException e1) {
                Log.wtf(TAG, "COULD NOT LOAD THE ENGLISH LOCALE SECTIONS, IMPOSSIBLE!", e1);
                return;
            }
        }

        Locale.setDefault(sectionsLocale);
        final Resources resources = getResources();
        final Configuration configuration = resources.getConfiguration();
        configuration.setLocale(sectionsLocale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }


    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        currentLanguage = getLocaleFromPreferences();
        setLocale();
        // setup each time the activity is (re)created, but only clear in MainActivity.onDestroy()
        SkillHandler.setSkillContextAndroidAndLocale(this);
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