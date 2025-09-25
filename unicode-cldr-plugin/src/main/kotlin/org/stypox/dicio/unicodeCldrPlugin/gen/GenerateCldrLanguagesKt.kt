package org.stypox.dicio.unicodeCldrPlugin.gen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import org.dicio.sentences_compiler.util.StringNormalizer.nfkdNormalize
import org.stypox.dicio.unicodeCldrPlugin.util.CLASS_NAME
import org.stypox.dicio.unicodeCldrPlugin.util.FILE_COMMENT
import org.stypox.dicio.unicodeCldrPlugin.util.PACKAGE_NAME
import java.io.File

fun generateSkillSentencesKt(parsedData: List<Pair<String, List<Pair<String, String>>>>, outputDirFile: File) {
    val baseObj = TypeSpec.Companion.objectBuilder(CLASS_NAME)
        .addKdoc("This class contains the names of (basically all) languages in the world (from " +
                         "Unicode CLDR) translated in all languages supported by Dicio. You can " +
                         "access the translation in a language supported by Dicio by indexing " +
                         "this class with `[]`. The resulting array is sorted so that relevant " +
                         "translations come before alternative ones for the same locale code.")

    val localeAndTranslationClassName = ClassName.bestGuess( "LocaleAndTranslation")
    baseObj.addType(generateLocaleAndTranslationClass(localeAndTranslationClassName))
    baseObj.addProperty(generateLanguageToDataProperty(parsedData, localeAndTranslationClassName))
    baseObj.addFunction(generateGetOperator(localeAndTranslationClassName))

    FileSpec.builder(PACKAGE_NAME, CLASS_NAME)
        .addFileComment(FILE_COMMENT)
        .addType(baseObj.build())
        .build()
        .writeTo(outputDirFile)
}

private fun generateLocaleAndTranslationClass(localeAndTranslationClassName: ClassName): TypeSpec {
    return TypeSpec.classBuilder(localeAndTranslationClassName)
        .addKdoc("A data class holding a locale code along with the corresponding translated " +
                         "language name")
        .addModifiers(KModifier.DATA)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter("locale", String::class)
                .addParameter("translation", String::class)
                .addParameter("translationNormalized", String::class)
                .build()
        )
        .addProperty(
            PropertySpec.builder("locale", String::class)
                .addKdoc("The locale code for the language, lowercase and with underscores " +
                                 "separating variants (e.g. \"en\" or \"zh_hans\")")
                .initializer("locale")
                .build()
        )
        .addProperty(
            PropertySpec.builder("translation", String::class)
                .addKdoc("The translated name of this language")
                .initializer("translation")
                .build()
        )
        .addProperty(
            PropertySpec.builder("translationNormalized", String::class)
                .addKdoc("The translated name of this language (NFKD-normalized)")
                .initializer("translationNormalized")
                .build()
        )
        .build()
}

private fun generateLanguageToDataProperty(
    parsedData: List<Pair<String, List<Pair<String, String>>>>,
    localeAndTranslationClassName: ClassName,
): PropertySpec {
    val dataProp = PropertySpec
        .builder(
            "languageToData",
            Map::class.asTypeName().parameterizedBy(
                String::class.asTypeName(),
                Lazy::class.asTypeName().parameterizedBy(
                    List::class.asTypeName().parameterizedBy(
                        localeAndTranslationClassName
                    )
                )
            )
        )
        .addModifiers(KModifier.PRIVATE)
        .initializer(
            "mapOf(${"%S to lazy { %L },".repeat(parsedData.size)})",
            *parsedData.flatMap { (languageFrom, translations) ->
                sequenceOf(
                    languageFrom,
                    CodeBlock.of(
                        "listOf(${"%T(%S, %S, %S),".repeat(translations.size)})",
                        *translations.flatMap { (locale, translation) ->
                            sequenceOf(
                                localeAndTranslationClassName,
                                locale,
                                translation,
                                nfkdNormalize(translation),
                            )
                        }.toTypedArray()
                    )
                )
            }.toTypedArray()
        )

    return dataProp.build()
}

private fun generateGetOperator(localeAndTranslationClassName: ClassName): FunSpec {
    return FunSpec.builder("get")
        .addModifiers(KModifier.OPERATOR)
        .addParameter("language", String::class)
        .returns(
            List::class.asTypeName()
                .parameterizedBy(localeAndTranslationClassName)
                .copy(nullable = true)
        )
        .addCode("return languageToData[language]?.value")
        .build()
}
