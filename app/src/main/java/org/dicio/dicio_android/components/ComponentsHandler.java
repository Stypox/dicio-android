package org.dicio.dicio_android.components;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.dicio.dicio_android.components.fallback.text.TextFallbackInfo;
import org.dicio.dicio_android.components.lyrics.LyricsInfo;
import org.dicio.dicio_android.components.open.OpenInfo;
import org.dicio.dicio_android.components.search.SearchInfo;
import org.dicio.dicio_android.components.weather.WeatherInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ComponentsHandler {

    private static final Map<String, AssistanceComponentInfo> assistanceComponentInfoMap =
            new HashMap<String, AssistanceComponentInfo>() {{
                put("weather", new WeatherInfo());
                put("search", new SearchInfo());
                put("lyrics", new LyricsInfo());
                put("open", new OpenInfo());
            }};

    private static final Map<String, AssistanceComponentInfo> fallbackAssistanceComponentInfoMap =
            new HashMap<String, AssistanceComponentInfo>() {{
                put("text", new TextFallbackInfo());
            }};


    public static String getIsEnabledPreferenceKey(final String assistanceComponentId) {
        return "components_handler_is_enabled_" + assistanceComponentId;
    }

    public static List<AssistanceComponent> getStandardAssistanceComponentBatch(
            final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final List<AssistanceComponent> result = new ArrayList<>();

        for (Map.Entry<String, AssistanceComponentInfo> entry
                : assistanceComponentInfoMap.entrySet()) {
            if (prefs.getBoolean(getIsEnabledPreferenceKey(entry.getKey()), true)) {
                result.add(entry.getValue().build(context, prefs));
            }
        }

        return result;
    }

    public static AssistanceComponent getFallbackAssistanceComponent(final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Objects.requireNonNull(fallbackAssistanceComponentInfoMap.get("text"))
                .build(context, prefs);
    }
}
