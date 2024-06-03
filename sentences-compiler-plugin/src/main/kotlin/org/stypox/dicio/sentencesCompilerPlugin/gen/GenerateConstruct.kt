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
import org.stypox.dicio.sentencesCompilerPlugin.util.SentencesCompilerPluginException

fun generateConstruct(construct: Construct): CodeBlock {
    return when (construct) {
        is Word -> generateWord(construct)
        is WordWithVariations -> generateWordWithVariations(construct)
        is OrList -> generateOrList(construct)
        is OptionalConstruct -> generateOptionalConstruct()
        is CapturingGroup -> generateCapturingGroup(construct)
        is SentenceConstructList -> generateSentenceConstructList(construct)
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
        ClassName("org.dicio.skill.standard2.construct", "WordConstruct"),
        word.value,
        /* isRegex = */ false,
        word.isDiacriticsSensitive,
        1.0f // TODO allow specifying weight
    )
}

fun generateWordWithVariations(word: WordWithVariations): CodeBlock {
    return CodeBlock.of(
        "%T(%S, %L, %L, %Lf)",
        ClassName("org.dicio.skill.standard2.construct", "WordConstruct"),
        word.toJavaRegex(),
        /* isRegex = */ true,
        word.isDiacriticsSensitive,
        1.0f // TODO allow specifying weight
    )
}

fun generateOrList(orList: OrList): CodeBlock {
    return CodeBlock.of(
        "%T(listOf(${"%L,".repeat(orList.constructs.size)}))",
        ClassName("org.dicio.skill.standard2.construct", "OrConstruct"),
        *orList.constructs.map(::generateConstruct).toTypedArray(),
    )
}

fun generateOptionalConstruct(): CodeBlock {
    return CodeBlock.of(
        "%T()",
        ClassName("org.dicio.skill.standard2.construct", "OptionalConstruct")
    )
}

fun generateCapturingGroup(capturingGroup: CapturingGroup): CodeBlock {
    return CodeBlock.of(
        "%T(%S, %Lf)",
        ClassName("org.dicio.skill.standard2.construct", "CapturingConstruct"),
        capturingGroup.name,
        1.0f // TODO allow specifying weight
    )
}

fun generateSentenceConstructList(sentenceConstructList: SentenceConstructList): CodeBlock {
    return CodeBlock.of(
        "%T(listOf(${"%L,".repeat(sentenceConstructList.constructs.size)}))",
        ClassName("org.dicio.skill.standard2.construct", "CompositeConstruct"),
        *sentenceConstructList.constructs.map(::generateConstruct).toTypedArray(),
    )
}
