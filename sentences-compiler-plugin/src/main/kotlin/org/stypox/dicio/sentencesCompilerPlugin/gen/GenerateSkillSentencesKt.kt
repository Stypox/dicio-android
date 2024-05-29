package org.stypox.dicio.sentencesCompilerPlugin.gen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import org.stypox.dicio.sentencesCompilerPlugin.data.ParsedData
import org.stypox.dicio.sentencesCompilerPlugin.data.ParsedSkill
import org.stypox.dicio.sentencesCompilerPlugin.util.CLASS_NAME
import org.stypox.dicio.sentencesCompilerPlugin.util.FILE_COMMENT
import org.stypox.dicio.sentencesCompilerPlugin.util.PACKAGE_NAME
import java.io.File

fun generateSkillSentencesKt(parsedData: ParsedData, outputDirFile: File) {
    val baseObj = TypeSpec.objectBuilder(CLASS_NAME)

    // add languages property
    baseObj.addProperty(
        PropertySpec
            .builder(
                "languages",
                List::class.asClassName().parameterizedBy(String::class.asClassName())
            )
            .initializer(
                 "listOf(${"%S,".repeat(parsedData.languages.size)})",
                 args = parsedData.languages.toTypedArray()
             )
             .build()
    )

    // add all skill objects
    for (skill in parsedData.skills) {
        baseObj.addType(generateSkillObject(skill))
    }

    FileSpec.builder(PACKAGE_NAME, CLASS_NAME)
        .addFileComment(FILE_COMMENT)
        .addType(baseObj.build())
        .build()
        .writeTo(outputDirFile)
}

private fun generateSkillObject(skill: ParsedSkill): TypeSpec {
    return TypeSpec.objectBuilder(skill.id.toPascalCase())
        .addType(generateResultInterface(skill))
        .addFunction(generateResultFromMatchFunction(skill))
        .addProperty(generateDataProperty(skill))
        .build()
}

private fun generateResultInterface(skill: ParsedSkill): TypeSpec {
    val nullableStringType = String::class.asTypeName().copy(nullable = true)
    val resultInterf = TypeSpec.interfaceBuilder("Result")
        .addModifiers(KModifier.SEALED)

    for (definition in skill.sentenceDefinitions) {
        val definitionResultCls = if (definition.captures.isEmpty()) {
            TypeSpec.objectBuilder(definition.id.toPascalCase())
        } else {
            TypeSpec.classBuilder(definition.id.toPascalCase())
                .addModifiers(KModifier.DATA)
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameters(definition.captures.map {
                            ParameterSpec.builder(it.id.toCamelCase(), nullableStringType).build()
                        })
                        .build()
                )
                .addProperties(definition.captures.map {
                    PropertySpec
                        .builder(it.id.toCamelCase(), nullableStringType)
                        .initializer(it.id.toCamelCase())
                        .build()
                })
        }

        definitionResultCls
            .addSuperinterface(ClassName("", "Result"))

        resultInterf.addType(definitionResultCls.build())
    }

    return resultInterf.build()
}

private fun generateResultFromMatchFunction(skill: ParsedSkill): FunSpec {
    val resultClassName = ClassName("", "Result")
    val fromStandardResultFun = FunSpec.builder("fromStandardResult")
        .addParameter("input", String::class)
        .addParameter("sentenceId", String::class)
        .addParameter("matchResult", ClassName("org.dicio.skill.standard2", "StandardMatchResult"))
        .returns(resultClassName)
        .beginControlFlow("return when(sentenceId)")

    for (definition in skill.sentenceDefinitions) {
        val definitionClassName = definition.id.toPascalCase()
        if (definition.captures.isEmpty()) {
            fromStandardResultFun.addStatement(
                "%S -> %T.$definitionClassName",
                resultClassName,
                definition.id
            )
        } else {
            fromStandardResultFun.addStatement(
                "%S -> %T.$definitionClassName(${
                    "matchResult.getCapturingGroup<%T>(input, %S),"
                        .repeat(definition.captures.size)
                })",
                definition.id,
                resultClassName,
                *definition.captures.flatMap { sequenceOf(String::class, it.id) }.toTypedArray()
            )
        }
    }

    fromStandardResultFun.addStatement("else -> throw IllegalArgumentException(\"Unknown sentence id \$sentenceId\")")
    fromStandardResultFun.endControlFlow()
    return fromStandardResultFun.build()
}

private fun generateDataProperty(skill: ParsedSkill): PropertySpec {
    val standardRecognizerDataClassName =
        ClassName("org.dicio.skill.standard2", "StandardRecognizerData")
            .parameterizedBy(ClassName("", "Result"))
    val dataProp = PropertySpec
        .builder(
            "data",
            Map::class.asTypeName().parameterizedBy(
                String::class.asTypeName(),
                standardRecognizerDataClassName
            )
        )
        .initializer(
            "mapOf(${"%S to %L,".repeat(skill.languageToSentences.size)})",
            *skill.languageToSentences.flatMap { (language, sentences) ->
                sequenceOf(
                    language,
                    CodeBlock.of(
                        "%T(%T.%L, %L, listOf(${"Pair(%S, %L),".repeat(sentences.size)}))",
                        standardRecognizerDataClassName,
                        ClassName("org.dicio.skill.skill", "Specificity"),
                        skill.specificity.name,
                        "::fromStandardResult",
                        *sentences.flatMap { sentence ->
                            sequenceOf(
                                sentence.id,
                                generateConstruct(sentence.constructs),
                            )
                        }.toTypedArray()
                    )
                )
            }.toTypedArray()
        )

    return dataProp.build()
}

/**
 * e.g. "current_time" would turn into "CurrentTime"
 */
private fun String.toPascalCase(): String {
    return split("_").joinToString { it.uppercaseFirstChar() }
}

/**
 * e.g. "time_or_duration" would turn into "timeOrDuration"
 */
private fun String.toCamelCase(): String {
    return toPascalCase().replaceFirstChar { it.lowercaseChar() }
}
