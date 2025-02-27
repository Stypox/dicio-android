package org.stypox.dicio.skills.search

import androidx.core.os.LocaleListCompat
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.StandardRecognizerSkill
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.stypox.dicio.sentences.Sentences.Search
import org.stypox.dicio.util.ConnectionUtils
import org.stypox.dicio.util.LocaleUtils

class SearchSkill(correspondingSkillInfo: SkillInfo, data: StandardRecognizerData<Search>)
    : StandardRecognizerSkill<Search>(correspondingSkillInfo, data) {
    override suspend fun generateOutput(ctx: SkillContext, inputData: Search): SkillOutput {
        val query = when (inputData) {
            is Search.Query -> inputData.what ?: return SearchOutput(null, true)
        }
        return SearchOutput(searchOnDuckDuckGo(ctx, query), true)
    }
}

private const val DUCK_DUCK_GO_SEARCH_URL = "https://duckduckgo.com/html/?q="

private val DUCK_DUCK_GO_SUPPORTED_LOCALES = listOf(
    "ar-es", "au-en", "at-de", "be-fr", "be-nl", "br-pt", "bg-bg", "ca-en", "ca-fr",
    "ct-ca", "cl-es", "cn-zh", "co-es", "hr-hr", "cz-cs", "dk-da", "ee-et", "fi-fi",
    "fr-fr", "de-de", "gr-el", "hk-tz", "hu-hu", "in-en", "id-en", "ie-en", "il-en",
    "it-it", "jp-jp", "kr-kr", "lv-lv", "lt-lt", "my-en", "mx-es", "nl-nl", "nz-en",
    "no-no", "pk-en", "pe-es", "ph-en", "pl-pl", "pt-pt", "ro-ro", "ru-ru", "xa-ar",
    "sg-en", "sk-sk", "sl-sl", "za-en", "es-ca", "es-es", "se-sv", "ch-de", "ch-fr",
    "tw-tz", "th-en", "tr-tr", "us-en", "us-es", "ua-uk", "uk-en", "vn-en"
)

internal fun searchOnDuckDuckGo(ctx: SkillContext, query: String): List<SearchOutput.Data> {
    // find the locale supported by DuckDuckGo that matches the user locale the most
    var resolvedLocale: LocaleUtils.LocaleResolutionResult? = null
    try {
        resolvedLocale = LocaleUtils.resolveSupportedLocale(
            LocaleListCompat.create(ctx.locale), DUCK_DUCK_GO_SUPPORTED_LOCALES
        )
    } catch (ignored: LocaleUtils.UnsupportedLocaleException) {
    }
    val locale = resolvedLocale?.supportedLocaleString ?: ""

    // make request using headers
    val html: String = ConnectionUtils.getPage(
        DUCK_DUCK_GO_SEARCH_URL + ConnectionUtils.urlEncode(query),
        object : HashMap<String?, String?>() {
            init {
                put(
                    "User-Agent",
                    "Mozilla/5.0 (X11; Linux x86_64; rv:135.0) Gecko/20100101 Firefox/135.0"
                )
                put(
                    "Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
                )
                put(
                    "Host",
                    "html.duckduckgo.com"
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
                    title = element.select("a[class=result__a]").first()!!.text(),
                    thumbnailUrl = "https:" + element.select("img[class=result__icon__img]")
                        .first()!!.attr("src"),
                    url = ConnectionUtils.urlDecode(
                        element.select("a[class=result__a]").first()!!.attr("href")
                    ),
                    description = element.select("a[class=result__snippet]").first()!!.text(),
                )
            )
        } catch (ignored: NullPointerException) {
        }
    }

    return result
}
