package org.stypox.dicio.unicodeCldrPlugin.data

import org.stypox.dicio.unicodeCldrPlugin.util.UnicodeCldrPluginException
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

fun parseLanguages(
    dicioLanguagesFile: File,
    cldrLanguagesDir: File,
): List<Pair<String, List<Pair<String, String>>>> {
    // make this a sorted set so the computation below is deterministic
    val supportedFromCodes = dicioLanguagesFile
        .readLines()
        .filter { line -> "LANGUAGE_" in line && "SYSTEM" !in line }
        .map { line -> line.split("_", limit = 2)[1].split(" ")[0].lowercase() }
        .toSortedSet()
    println("Languages supported by Dicio: $supportedFromCodes")

    // make this a sorted set so the computation below is deterministic
    val supportedToCodes = (
            getLanguageElements(File(cldrLanguagesDir, "en.xml"))
                .map { it.getAttribute("type").lowercase() } +
            supportedFromCodes
        ).toSortedSet()
    println("Languages for which there is an English translation: ${supportedToCodes.size}")

    // locale_code -> { language_code -> set[(is_alternative, language_name_translation)] }
    val data = mutableMapOf<String, MutableMap<String, MutableSet<Pair<Boolean, String>>>>()
    for (lfrom in supportedFromCodes) {
        data[lfrom] = mutableMapOf<String, MutableSet<Pair<Boolean, String>>>().also {
            for (lto in supportedToCodes) {
                it[lto] = mutableSetOf()
            }
        }
    }

    // sorted so that general languages (e.g. "en") come before specific ones (e.g. "en_IN"),
    // and also so that the computation below is deterministic
    val files = cldrLanguagesDir.listFiles()
        ?.filter { it -> it.extension == "xml" }
        ?.sortedDescending()
        ?: throw UnicodeCldrPluginException("Could not list XML files in $cldrLanguagesDir")

    // go through each XML file and collect translations
    for (filename in files) {
        val fromCode = filename.nameWithoutExtension.lowercase()
        val fromCodes = supportedFromCodes.filter {
            it == fromCode || it.startsWith("${fromCode}_")
        }
        if (fromCodes.isEmpty()) {
            continue
        }

        for (lang in getLanguageElements(filename)) {
            val toCode = lang.getAttribute("type").lowercase()
            if (toCode !in supportedToCodes) {
                continue
            }

            var texts: List<Pair<Boolean, String>>? = null
            val text = lang.textContent
            if (text == "↑↑↑" && "_" in fromCode) {
                texts = data[fromCode.substringBefore("_")]
                    ?.get(toCode)
                    ?.map { (_, code) -> Pair(/* isAlternative = */ true, code) }
                    ?.toList()
            } else if (text.isNotBlank() && text != "↑↑↑") {
                texts = listOf(Pair(
                    /* isAlternative = */ lang.hasAttribute("alt") || lang.hasAttribute("menu"),
                    text,
                ))
            }
            if (texts == null) {
                continue
            }

            for (fc in fromCodes) {
                data[fc]!![toCode]!!.addAll(texts)
            }
        }
    }

    val sizeBytes = data.values.sumOf { a -> a.values.sumOf { b -> b.size } }
    println("Size of language translations matrix: $sizeBytes bytes")

    // ensure everything is sorted so we know that we will deterministically obtain the same result
    // every time for reproducible builds
    return data.map { (fromCode, tos) ->
        Pair(
            fromCode,
            tos.flatMap { (toCode, translations) ->
                translations.map { translation -> Triple(toCode, translation.first, translation.second) }
            }
                .sortedWith { a, b ->
                    // compare by locale, then by whether it's the main translation or just an
                    // alternative, and finally by the translation (so the output is deterministic)
                    val firstCompare = a.first.compareTo(b.first)
                    if (firstCompare != 0) return@sortedWith firstCompare
                    val secondCompare = a.second.compareTo(b.second)
                    if (secondCompare != 0) return@sortedWith secondCompare
                    return@sortedWith a.third.compareTo(b.third)
                }
                .map { (code, _, translation) -> Pair(code, translation) }
        )
    }
        .sortedBy { it.first }
}

fun getLanguageElements(file: File): List<Element> {
    val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
    val nodes = doc.getElementsByTagName("language")
    val result = mutableListOf<Element>()
    for (i in 0 until nodes.length) {
        val el = nodes.item(i)
        if (el is Element) {
            result.add(el)
        }
    }
    return result
}