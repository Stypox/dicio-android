package org.dicio.skill;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.dicio.numbers.NumberParserFormatter;

import java.util.Locale;

/**
 * A class that just wraps the Android context, the Android shared preferences and the user locale.
 * A skill is provided with this class whenever it is doing some calculations, so that it has the
 * resources it needs available.
 */
public final class SkillContext {
    @NonNull final Context androidContext;
    @NonNull final SharedPreferences preferences;
    @NonNull final Locale locale;
    @NonNull final NumberParserFormatter numberParserFormatter;

    /**
     * Constructs a new instance of {@link SkillContext} wrapping the provided arguments.
     *
     * @param androidContext the Android context, useful for example to get resources, etc.
     * @param preferences the Android shared preferences, useful for user customization, also see
     *                    {@link SkillInfo#hasPreferences()} and
     *                    {@link SkillInfo#getPreferenceFragment()}
     * @param locale the current user locale, useful for example to customize web requests to get
     *               the correct language or country
     * @param numberParserFormatter the number parser formatter for the current locale, useful for
     *                              example to format a number to show to the user or extract
     *                              numbers from an utterance. See {@link NumberParserFormatter}.
     */
    public SkillContext(@NonNull final Context androidContext,
                        @NonNull final SharedPreferences preferences,
                        @NonNull final Locale locale,
                        @NonNull final NumberParserFormatter numberParserFormatter) {
        this.androidContext = androidContext;
        this.preferences = preferences;
        this.locale = locale;
        this.numberParserFormatter = numberParserFormatter;
    }

    /**
     * @return the Android context, useful for example to get resources, etc.
     */
    @NonNull
    public Context getAndroidContext() {
        return androidContext;
    }

    /**
     * @return the Android shared preferences, useful for user customization, also see
     *         {@link SkillInfo#hasPreferences()} and {@link SkillInfo#getPreferenceFragment()}
     */
    @NonNull
    public SharedPreferences getPreferences() {
        return preferences;
    }

    /**
     * @return the current user locale, useful for example to customize web requests to get the
     *         correct language or country
     */
    @NonNull
    public Locale getLocale() {
        return locale;
    }

    /**
     * @return the number parser formatter for the current locale, useful for example to format a
     *         number to show to the user or extract numbers from an utterance
     * @see NumberParserFormatter
     */
    @NonNull
    public NumberParserFormatter getNumberParserFormatter() {
        return numberParserFormatter;
    }
}
