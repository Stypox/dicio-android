package org.dicio.dicio_android.components.weather;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import org.dicio.component.standard.StandardRecognizer;
import org.dicio.dicio_android.components.AssistanceComponent;
import org.dicio.dicio_android.components.AssistanceComponentInfo;
import org.dicio.dicio_android.components.ChainAssistanceComponent;

import static org.dicio.dicio_android.Sections.getSection;
import static org.dicio.dicio_android.SectionsGenerated.weather;

public class WeatherInfo implements AssistanceComponentInfo {

    @Override
    public AssistanceComponent build(final Context context, final SharedPreferences preferences) {
        return new ChainAssistanceComponent.Builder()
                .recognize(new StandardRecognizer(getSection(weather)))
                .process(new OpenWeatherMapProcessor())
                .output(new WeatherOutput());
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
