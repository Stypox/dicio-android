package org.dicio.dicio_android.skills.search;

import static org.dicio.dicio_android.Sentences_en.search;

import androidx.core.os.LocaleListCompat;

import org.dicio.dicio_android.util.ConnectionUtils;
import org.dicio.dicio_android.util.LocaleUtils;
import org.dicio.dicio_android.util.StringUtils;
import org.dicio.skill.SkillContext;
import org.dicio.skill.chain.IntermediateProcessor;
import org.dicio.skill.standard.StandardResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DuckDuckGoProcessor
        implements IntermediateProcessor<StandardResult, List<SearchOutput.Data>> {

    private static final String duckDuckGoSearchUrl = "https://duckduckgo.com/html/?q=";

    private static final List<String> supportedLocales = Arrays.asList("ar-es", "au-en", "at-de",
            "be-fr", "be-nl", "br-pt", "bg-bg", "ca-en", "ca-fr", "ct-ca", "cl-es", "cn-zh",
            "co-es", "hr-hr", "cz-cs", "dk-da", "ee-et", "fi-fi", "fr-fr", "de-de", "gr-el",
            "hk-tz", "hu-hu", "in-en", "id-en", "ie-en", "il-en", "it-it", "jp-jp", "kr-kr",
            "lv-lv", "lt-lt", "my-en", "mx-es", "nl-nl", "nz-en", "no-no", "pk-en", "pe-es",
            "ph-en", "pl-pl", "pt-pt", "ro-ro", "ru-ru", "xa-ar", "sg-en", "sk-sk", "sl-sl",
            "za-en", "es-ca", "es-es", "se-sv", "ch-de", "ch-fr", "tw-tz", "th-en", "tr-tr",
            "us-en", "us-es", "ua-uk", "uk-en", "vn-en");

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

        // find the locale supported by DuckDuckGo that matches the user locale the most
        LocaleUtils.LocaleResolutionResult resolvedLocale = null;
        try {
            resolvedLocale = LocaleUtils.resolveSupportedLocale(
                    LocaleListCompat.create(context.getLocale()), supportedLocales);
        } catch (final LocaleUtils.UnsupportedLocaleException ignored) {
        }
        final String locale = resolvedLocale == null ? "" : resolvedLocale.supportedLocaleString;

        // make request using headers
        final String html = ConnectionUtils.getPage(
                duckDuckGoSearchUrl + ConnectionUtils.urlEncode(queryToSearch),
                new HashMap<String, String>() {{
                    put("User-Agent",
                            "Mozilla/5.0 (X11; Linux x86_64; rv:95.0) Gecko/20100101 Firefox/95.0");
                    put("Cookie", "kl=" + locale);
                }}
        );
        final Document document = Jsoup.parse(html);
        final Elements elements = document.select("div[class=links_main links_deep result__body]");

        final List<SearchOutput.Data> result = new ArrayList<>();
        for (final Element element : elements) {
            try {
                final SearchOutput.Data searchResult = new SearchOutput.Data();

                searchResult.title = element.select("a[class=result__a]").first().html();
                searchResult.thumbnailUrl = "https:"
                        + element.select("img[class=result__icon__img]").first().attr("src");
                searchResult.url = ConnectionUtils.urlDecode(
                        element.select("a[class=result__a]").first().attr("href"));
                searchResult.description
                        = element.select("a[class=result__snippet]").first().html();

                result.add(searchResult);
            } catch (final NullPointerException ignored) {
            }
        }
        return result;
    }
}
