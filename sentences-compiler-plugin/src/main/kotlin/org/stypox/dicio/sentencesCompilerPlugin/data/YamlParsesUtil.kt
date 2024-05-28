package org.stypox.dicio.sentencesCompilerPlugin.data

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

val yamlObjectMapper = ObjectMapper(YAMLFactory()).apply {
    registerModule(
        KotlinModule.Builder()
            .withReflectionCacheSize(512)
            .configure(KotlinFeature.NullToEmptyCollection, false)
            .configure(KotlinFeature.NullToEmptyMap, false)
            .configure(KotlinFeature.NullIsSameAsDefault, false)
            .configure(KotlinFeature.SingletonSupport, true)
            .configure(KotlinFeature.StrictNullChecks, true)
            .build()
    )
    configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
}

inline fun <reified T> parseYamlFile(file: File): T {
    return BufferedReader(FileReader(file))
        .use { yamlObjectMapper.readValue(it, T::class.java) }
}
