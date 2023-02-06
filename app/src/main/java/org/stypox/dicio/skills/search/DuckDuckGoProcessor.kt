package org.stypox.dicio.skills.search

import androidx.core.os.LocaleListCompat
import org.dicio.skill.chain.IntermediateProcessor
import org.dicio.skill.standard.StandardResult
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.stypox.dicio.Sentences_en.search
import org.stypox.dicio.util.ConnectionUtils
import org.stypox.dicio.util.LocaleUtils

class DuckDuckGoProcessor : IntermediateProcessor<StandardResult, List<SearchOutput.Data>?>() {
    @Throws(Exception::class)
    override fun process(data: StandardResult): List<SearchOutput.Data>? {
        val queryToSearch: String? = data.getCapturingGroup(search.what)
        if (queryToSearch.isNullOrBlank()) {
            // empty capturing group, e.g. "search for" without anything else
            return null
        }

        // find the locale supported by DuckDuckGo that matches the user locale the most
        var resolvedLocale: LocaleUtils.LocaleResolutionResult? = null
        try {
            resolvedLocale = LocaleUtils.resolveSupportedLocale(
                LocaleListCompat.create(ctx().locale), SUPPORTED_LOCALES
            )
        } catch (ignored: LocaleUtils.UnsupportedLocaleException) {
        }
        val locale = resolvedLocale?.supportedLocaleString ?: ""

        // make request using headers
        val html: String = ConnectionUtils.getPage(
            DUCK_DUCK_GO_SEARCH_URL + ConnectionUtils.urlEncode(queryToSearch),
            object : HashMap<String?, String?>() {
                init {
                    put(
                        "User-Agent",
                        "Mozilla/5.0 (X11; Linux x86_64; rv:95.0) Gecko/20100101 Firefox/95.0"
                    )
                    put("Cookie", "kl=$locale")
                }
            }
        )

        val document: Document = Jsoup.parse(html)
        val elements = document.select("div[class=links_main links_deep result__body]")
        val result: MutableList<SearchOutput.Data> = ArrayList()
        for (element in elements) {
            try {
                result.add(
                    SearchOutput.Data(
                        title = element.select("a[class=result__a]").first()!!.html(),
                        thumbnailUrl = "https:" + element.select("img[class=result__icon__img]")
                            .first()!!.attr("src"),
                        url = ConnectionUtils.urlDecode(
                            element.select("a[class=result__a]").first()!!.attr("href")
                        ),
                        description = element.select("a[class=result__snippet]").first()!!.html(),
                    )
                )
            } catch (ignored: NullPointerException) {
            }
        }
        return result
    }

    companion object {
        private const val DUCK_DUCK_GO_SEARCH_URL = "https://duckduckgo.com/html/?q="
        private val SUPPORTED_LOCALES = listOf(
            "ar-es", "au-en", "at-de", "be-fr", "be-nl", "br-pt", "bg-bg", "ca-en", "ca-fr",
            "ct-ca", "cl-es", "cn-zh", "co-es", "hr-hr", "cz-cs", "dk-da", "ee-et", "fi-fi",
            "fr-fr", "de-de", "gr-el", "hk-tz", "hu-hu", "in-en", "id-en", "ie-en", "il-en",
            "it-it", "jp-jp", "kr-kr", "lv-lv", "lt-lt", "my-en", "mx-es", "nl-nl", "nz-en",
            "no-no", "pk-en", "pe-es", "ph-en", "pl-pl", "pt-pt", "ro-ro", "ru-ru", "xa-ar",
            "sg-en", "sk-sk", "sl-sl", "za-en", "es-ca", "es-es", "se-sv", "ch-de", "ch-fr",
            "tw-tz", "th-en", "tr-tr", "us-en", "us-es", "ua-uk", "uk-en", "vn-en"
        )
    }
}