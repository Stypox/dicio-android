package org.dicio.dicio_android.skills.search;

import org.dicio.dicio_android.util.ConnectionUtils;
import org.dicio.skill.SkillContext;
import org.dicio.skill.chain.IntermediateProcessor;
import org.dicio.skill.standard.StandardResult;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.dicio.dicio_android.Sentences_en.search;
import static org.dicio.dicio_android.util.ConnectionUtils.getPageJson;

public class QwantProcessor
        implements IntermediateProcessor<StandardResult, List<SearchOutput.Data>> {

    private static final String qwantSearchUrl = "https://api.qwant.com/api/search/web";

    // Qwant uses only some specific locale names, taken from the page JS
    private static final Map<String, String> languageCountryMap = new HashMap<String, String>() {{
        put("fr", "fr");
        put("en", "gb");
        put("de", "de");
        put("it", "it");
        put("br", "fr");
        put("ca", "es");
        put("co", "fr");
        put("es", "es");
        put("eu", "es");
        put("nl", "nl");
        put("pl", "pl");
        put("pt", "pt");
        put("ru", "ru");
    }};

    private String getLocaleString(final Locale locale) {
        final String language = locale.getLanguage().toLowerCase();
        if (languageCountryMap.containsKey(language)) {
            return language + "_" + languageCountryMap.get(language);
        } else {
            return "en_gb"; // default to UK if unable to determine country
        }
    }

    @Override
    public List<SearchOutput.Data> process(final StandardResult data, final SkillContext context)
            throws Exception {

        final JSONObject json = getPageJson(qwantSearchUrl
                + "?count=10&offset=20&t=dicio&uiv=1&locale=" + getLocaleString(context.getLocale())
                + "&q=" + ConnectionUtils.urlEncode(data.getCapturingGroup(search.what).trim()));
        final JSONArray items =
                json.getJSONObject("data").getJSONObject("result").getJSONArray("items");

        final List<SearchOutput.Data> result = new ArrayList<>();
        for (int i = 0; i < items.length(); ++i) {
            SearchOutput.Data searchResult = new SearchOutput.Data();
            final JSONObject item = items.getJSONObject(i);

            searchResult.title = item.getString("title");
            searchResult.thumbnailUrl = "https:" + item.getString("favicon");
            searchResult.url = item.getString("url");
            searchResult.description = item.getString("desc");
            result.add(searchResult);
        }

        return result;
    }
}
