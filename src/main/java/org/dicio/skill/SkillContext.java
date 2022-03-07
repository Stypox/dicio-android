package org.dicio.skill;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dicio.numbers.NumberParserFormatter;
import org.dicio.skill.output.GraphicalOutputDevice;
import org.dicio.skill.output.SpeechOutputDevice;

import java.util.Locale;
import java.util.Objects;

/**
 * A class that just wraps the Android context, the Android shared preferences, the user locale, the
 * graphical output device and the speech output device. This class extends {@link Context}, so it
 * can be used as an Android context, but it also has some more methods, namely {@link
 * #getPreferences()}, {@link #getLocale()}, {@link #getNumberParserFormatter()}, {@link
 * #requireNumberParserFormatter()}, {@link #getGraphicalOutputDevice()} and {@link
 * #getSpeechOutputDevice()}. The setter methods should not be used by skills.
 */
public final class SkillContext {
    Context androidContext;
    SharedPreferences preferences;
    Locale locale;
    @Nullable NumberParserFormatter numberParserFormatter;

    GraphicalOutputDevice graphicalOutputDevice;
    SpeechOutputDevice speechOutputDevice;

    /**
     * @return the Android context, useful for example to get resources, etc.
     * @implNote the name is like this because getAndroidContext would be too long
     */
    public Context android() {
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
     *         number to show to the user or extract numbers from an utterance. Returns null if
     *         the current user language is not supported by any {@link NumberParserFormatter}.
     * @see #requireNumberParserFormatter()
     * @see NumberParserFormatter
     */
    @Nullable
    public NumberParserFormatter getNumberParserFormatter() {
        return numberParserFormatter;
    }

    /**
     * @return the number parser formatter for the current locale, useful for example to format a
     *         number to show to the user or extract numbers from an utterance. Throws a null
     *         pointer exception if the current user language is not supported by any
     *         {@link NumberParserFormatter}.
     * @see #getNumberParserFormatter()
     * @see NumberParserFormatter
     */
    @NonNull
    public NumberParserFormatter requireNumberParserFormatter() {
        return Objects.requireNonNull(numberParserFormatter);
    }

    /**
     * @return the {@link GraphicalOutputDevice} that should be used for skill graphical output
     */
    @NonNull
    public GraphicalOutputDevice getGraphicalOutputDevice() {
        return graphicalOutputDevice;
    }

    /**
     * @return the {@link SpeechOutputDevice} that should be used for skill speech output
     */
    @NonNull
    public SpeechOutputDevice getSpeechOutputDevice() {
        return speechOutputDevice;
    }


    /**
     * @apiNote not intended for usage inside skills, but only for constructing and maintaining a
     *          skill context
     * @param androidContext the Android context, useful for example to get resources, etc.
     */
    public void setAndroidContext(@NonNull final Context androidContext) {
        this.androidContext = androidContext;
    }

    /**
     * @apiNote not intended for usage inside skills, but only for constructing and maintaining a
     *          skill context
     * @param preferences the Android shared preferences, useful for user customization, also see
     *                    {@link SkillInfo#hasPreferences()} and
     *                    {@link SkillInfo#getPreferenceFragment()}
     */
    public void setPreferences(@NonNull final SharedPreferences preferences) {
        this.preferences = preferences;
    }

    /**
     * @apiNote not intended for usage inside skills, but only for constructing and maintaining a
     *          skill context
     * @param locale the current user locale, useful for example to customize web requests to get
     *               the correct language or country
     */
    public void setLocale(@NonNull final Locale locale) {
        this.locale = locale;
    }

    /**
     * @apiNote not intended for usage inside skills, but only for constructing and maintaining a
     *          skill context
     * @param numberParserFormatter the number parser formatter for the current locale, useful for
     *                              example to format a number to show to the user or extract
     *                              numbers from an utterance. See {@link NumberParserFormatter}.
     *                              Pass {@code null} if the current user language is not supported
     *                              by any {@link NumberParserFormatter}.
     */
    public void setNumberParserFormatter(
            @Nullable final NumberParserFormatter numberParserFormatter) {
        this.numberParserFormatter = numberParserFormatter;
    }

    /**
     * @apiNote not intended for usage inside skills, but only for constructing and maintaining a
     *          skill context
     * @param graphicalOutputDevice the {@link GraphicalOutputDevice} that skills will use for
     *                              graphical output
     */
    public void setGraphicalOutputDevice(GraphicalOutputDevice graphicalOutputDevice) {
        this.graphicalOutputDevice = graphicalOutputDevice;
    }

    /**
     * @apiNote not intended for usage inside skills, but only for constructing and maintaining a
     *          skill context
     * @param speechOutputDevice the {@link SpeechOutputDevice} that skills will use for speech
     *                           output
     */
    public void setSpeechOutputDevice(SpeechOutputDevice speechOutputDevice) {
        this.speechOutputDevice = speechOutputDevice;
    }
}
