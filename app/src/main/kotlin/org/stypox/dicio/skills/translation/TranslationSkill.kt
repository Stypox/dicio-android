package org.stypox.dicio.skills.translation

import androidx.core.os.LocaleListCompat
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.StandardRecognizerSkill
import org.json.JSONObject
import org.stypox.dicio.sentences.Sentences.Translation
import org.stypox.dicio.sentences.Sentences.Translation.Translate
import org.stypox.dicio.util.ConnectionUtils
import org.stypox.dicio.util.LocaleUtils
import java.util.*

class TranslationSkill(correspondingSkillInfo: SkillInfo, data: StandardRecognizerData<Translation>)
    : StandardRecognizerSkill<Translation>(correspondingSkillInfo, data) {

    private fun determineCurrentLocale(ctx: SkillContext): String {
        var resolvedLocale: LocaleUtils.LocaleResolutionResult? = null
        try {
            resolvedLocale = LocaleUtils.resolveSupportedLocale(
                LocaleListCompat.create(ctx.locale),
                TRANSLATE_SUPPORTED_LOCALES
            )
        } catch (_: LocaleUtils.UnsupportedLocaleException) {
        }
        val locale = resolvedLocale?.supportedLocaleString ?: ""
        return locale
    }

    private val languageCodeMap: Map<String, String> by lazy {
        Locale.getISOLanguages().associate { languageCode ->
            val displayNameOfLocale = Locale(languageCode).displayLanguage
            displayNameOfLocale.lowercase() to languageCode
        }
    }

    private fun getLanguageCode(queryLanguage: String): String {
        return languageCodeMap[queryLanguage.trim().lowercase()].toString()
    }
    // This connects to lingva.ml which runs the Lingva Translate frontend to Google Translate
    // TODO: Add more servers in like Libre Translate or DeepL
    override suspend fun generateOutput(ctx: SkillContext, inputData: Translation): SkillOutput {
        val currentLocale = determineCurrentLocale(ctx)

        // Extract input data then convert the language's to language codes.
        val (target, source, query) = when (inputData) {
            is Translate -> {
                val target = if (inputData.target != null) {
                    getLanguageCode(inputData.target.toString())
                } else {
                    currentLocale
                }
                val source = if (inputData.source != null) {
                    getLanguageCode(inputData.source.toString())
                } else {
                    "auto"
                }
                val query = inputData.query ?: return TranslationOutput.EmptyQuery
                Triple(target, source, query)
            }
        }

        val encodedQuery = ConnectionUtils.urlEncode(query.trim())
        val translation: JSONObject = ConnectionUtils.getPageJson(
            "$TRANSLATE_URL$source/$target/$encodedQuery"
        )

        val targetLocale = Locale(target)
        val decodedTranslation = ConnectionUtils.urlDecode(
            translation.getString("translation")
        )

        return try {
            TranslationOutput.Success(
                translation = decodedTranslation,
                query = query.trim(),
                target = targetLocale.getDisplayLanguage(Locale.getDefault())
            )
        } catch (_: Exception) {
            TranslationOutput.Failed(
                target = targetLocale.getDisplayLanguage(Locale.getDefault())
            )
        }
    }

    companion object {
        private const val TRANSLATE_URL = "https://lingva.ml//api/v1/"
        val TRANSLATE_SUPPORTED_LOCALES = listOf(
            "af", "sq", "am", "ar", "hy", "as", "ay", "az", "bm", "eu",
            "be", "bn", "bho", "bs", "bg", "ca", "ceb", "ny", "zh", "zh_HANT",
            "co", "hr", "cs", "da", "dv", "doi", "nl", "en", "eo", "et", "ee",
            "tl", "fi", "fr", "fy", "gl", "ka", "de", "el", "gn", "gu", "ht",
            "ha", "haw", "iw", "hi", "hmn", "hu", "is", "ig", "ilo", "id", "ga",
            "it", "ja", "jw", "kn", "kk", "km", "rw", "gom", "ko", "kri", "ku",
            "ckb", "ky", "lo", "la", "lv", "ln", "lt", "lg", "lb", "mk", "mai",
            "mg", "ms", "ml", "mt", "mi", "mr", "mni-Mtei", "lus", "mn", "my",
            "ne", "no", "or", "om", "ps", "fa", "pl", "pt", "pa", "qu", "ro",
            "ru", "sm", "sa", "gd", "nso", "sr", "st", "sn", "sd", "si", "sk",
            "sl", "so", "es", "su", "sw", "sv", "tg", "ta", "tt", "te", "th",
            "ti", "ts", "tr", "tk", "ak", "uk", "ur", "ug", "uz", "vi", "cy",
            "xh", "yi", "yo", "zu"
        )
    }
}
