package org.stypox.dicio.sentencesCompilerPlugin.gen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import org.dicio.sentences_compiler.construct.CapturingGroup
import org.dicio.sentences_compiler.construct.Construct
import org.dicio.sentences_compiler.construct.OptionalConstruct
import org.dicio.sentences_compiler.construct.OrList
import org.dicio.sentences_compiler.construct.SentenceConstructList
import org.dicio.sentences_compiler.construct.Word
import org.dicio.sentences_compiler.construct.WordWithVariations
import org.stypox.dicio.sentencesCompilerPlugin.data.CaptureDefinition
import org.stypox.dicio.sentencesCompilerPlugin.data.CaptureType
import org.stypox.dicio.sentencesCompilerPlugin.util.SentencesCompilerPluginException

fun generateConstruct(
    construct: Construct,
    captureDefinitions: List<CaptureDefinition>,
): CodeBlock {
    return when (construct) {
        is Word -> generateWord(construct)
        is WordWithVariations -> generateWordWithVariations(construct)
        is OrList -> generateOrList(construct, captureDefinitions)
        is OptionalConstruct -> generateOptionalConstruct()
        is CapturingGroup -> generateCapturingGroup(construct, captureDefinitions)
        is SentenceConstructList -> generateSentenceConstructList(construct, captureDefinitions)
        else -> throw SentencesCompilerPluginException(
            "Unexpected construct obtained from sentences compiler: type=${
                construct::class.simpleName
            }, value=\"$construct\""
        )
    }
}

fun generateWord(word: Word): CodeBlock {
    return CodeBlock.of(
        "%T(%S, %L, %L, %Lf)",
        ClassName("org.dicio.skill.standard.construct", "WordConstruct"),
        word.normalizedValue,
        /* isRegex = */ false,
        word.isDiacriticsSensitive,
        1.0f // TODO allow specifying weight
    )
}

fun generateWordWithVariations(word: WordWithVariations): CodeBlock {
    return CodeBlock.of(
        "%T(%S, %L, %L, %Lf)",
        ClassName("org.dicio.skill.standard.construct", "WordConstruct"),
        word.toJavaRegex(),
        /* isRegex = */ true,
        word.isDiacriticsSensitive,
        1.0f // TODO allow specifying weight
    )
}

fun generateOrList(
    orList: OrList,
    captureDefinitions: List<CaptureDefinition>,
): CodeBlock {
    return CodeBlock.of(
        "%T(listOf(${"%L,".repeat(orList.constructs.size)}))",
        ClassName("org.dicio.skill.standard.construct", "OrConstruct"),
        *orList.constructs.map {
            generateConstruct(it, captureDefinitions)
        }.toTypedArray(),
    )
}

fun generateOptionalConstruct(): CodeBlock {
    return CodeBlock.of(
        "%T()",
        ClassName("org.dicio.skill.standard.construct", "OptionalConstruct")
    )
}

fun generateCapturingGroup(
    capturingGroup: CapturingGroup,
    captureDefinitions: List<CaptureDefinition>,
): CodeBlock {
    return CodeBlock.of(
        "%T(%S, %Lf)",
        captureTypeToConstruct(captureDefinitions.first { it.id == capturingGroup.name }.type),
        capturingGroup.name,
        0.0f // TODO allow specifying weight
    )
}

fun generateSentenceConstructList(
    sentenceConstructList: SentenceConstructList,
    captureDefinitions: List<CaptureDefinition>,
): CodeBlock {
    return CodeBlock.of(
        "%T(listOf(${"%L,".repeat(sentenceConstructList.constructs.size)}))",
        ClassName("org.dicio.skill.standard.construct", "CompositeConstruct"),
        *sentenceConstructList.constructs.map {
            generateConstruct(it, captureDefinitions)
        }.toTypedArray(),
    )
}

private fun captureTypeToConstruct(captureType: CaptureType): ClassName {
    return when (captureType) {
        CaptureType.STRING -> ClassName("org.dicio.skill.standard.construct", "CapturingConstruct")
        CaptureType.DURATION -> ClassName("org.dicio.skill.standard.construct", "CapturingConstruct") // TODO still unimplemented
        CaptureType.LANGUAGE_NAME -> ClassName("org.dicio.skill.standard.construct", "LanguageNameCapturingConstruct")
    }
}
