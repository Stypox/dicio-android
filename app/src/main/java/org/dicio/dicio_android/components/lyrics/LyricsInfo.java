package org.dicio.dicio_android.components.lyrics;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import org.dicio.component.standard.StandardRecognizer;
import org.dicio.dicio_android.components.AssistanceComponent;
import org.dicio.dicio_android.components.AssistanceComponentInfo;
import org.dicio.dicio_android.components.ChainAssistanceComponent;
import org.dicio.dicio_android.components.weather.OpenWeatherMapProcessor;
import org.dicio.dicio_android.components.weather.WeatherOutput;

import static org.dicio.dicio_android.Sections.getSection;
import static org.dicio.dicio_android.SectionsGenerated.lyrics;
import static org.dicio.dicio_android.SectionsGenerated.weather;

public class LyricsInfo implements AssistanceComponentInfo {

    @Override
    public AssistanceComponent build(final Context context, final SharedPreferences preferences) {
        return new ChainAssistanceComponent.Builder()
                .recognize(new StandardRecognizer(getSection(lyrics)))
                .process(new GeniusProcessor())
                .output(new LyricsOutput());
    }

    @Override
    public boolean hasPreferences() {
        return false;
    }

    @Nullable
    @Override
    public PreferenceFragmentCompat getPreferenceFragment() {
        return null;
    }
}
