package org.dicio.dicio_android.skills.search;

import static org.dicio.dicio_android.Sentences_en.search;

import android.os.Handler;
import android.os.Looper;

import org.dicio.dicio_android.util.ConnectionUtils;
import org.dicio.dicio_android.util.ShareUtils;
import org.dicio.dicio_android.util.StringUtils;
import org.dicio.skill.SkillContext;
import org.dicio.skill.chain.IntermediateProcessor;
import org.dicio.skill.standard.StandardResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DuckDuckGoProcessor
        implements IntermediateProcessor<StandardResult, List<SearchOutput.Data>> {

    private static final String duckDuckGoSearchUrl = "https://duckduckgo.com/html/?q=";


    @SuppressWarnings("ConstantConditions") // NullPointerExceptions are handled
    @Override
    public List<SearchOutput.Data> process(final StandardResult data, final SkillContext context)
            throws Exception {
        String queryToSearch = data.getCapturingGroup(search.what);
        if (queryToSearch != null) {
            queryToSearch = queryToSearch.trim();
        }
        if (StringUtils.isNullOrEmpty(queryToSearch)) {
            // empty capturing group, e.g. "search for" without anything else
            return null;
        }

        final String html = ConnectionUtils.getPage(
                duckDuckGoSearchUrl + ConnectionUtils.urlEncode(queryToSearch),
                new HashMap<String, String>() {{
                    put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:95.0) Gecko/20100101 Firefox/95.0");
                }}
        );
        new Handler(Looper.getMainLooper()).post(() ->
                ShareUtils.copyToClipboard(context.getAndroidContext(), html));
        final Document document = Jsoup.parse(html);
        final Elements elements = document.select("div[class=links_main links_deep result__body]");

        final List<SearchOutput.Data> result = new ArrayList<>();
        for (final Element element : elements) {
            try {
                final SearchOutput.Data searchResult = new SearchOutput.Data();

                searchResult.title = element.select("a[class=result__a]").first().html();
                searchResult.thumbnailUrl = "https:"
                        + element.select("img[class=result__icon__img]").first().attr("src");
                searchResult.url = element.select("a[class=result__a]").first().attr("href");
                searchResult.url = ConnectionUtils.urlDecode(searchResult.url.substring(15));
                searchResult.description
                        = element.select("a[class=result__snippet]").first().html();

                result.add(searchResult);
            } catch (NullPointerException ignored) {
            }
        }
        return result;
    }
}
