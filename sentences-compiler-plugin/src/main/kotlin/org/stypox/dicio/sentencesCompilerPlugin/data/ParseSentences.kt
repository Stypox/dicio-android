package org.stypox.dicio.sentencesCompilerPlugin.data

import org.dicio.sentences_compiler.lexer.Tokenizer
import org.dicio.sentences_compiler.parser.Parser
import org.dicio.sentences_compiler.util.CompilerError
import org.gradle.api.GradleException
import org.stypox.dicio.sentencesCompilerPlugin.util.SentencesCompilerPluginException
import java.io.ByteArrayInputStream
import java.io.InputStreamReader

fun parseSentences(rawExtractedData: RawExtractedData): ParsedExtractedData {
    return ParsedExtractedData(
        skills = rawExtractedData.skills,
        languageToSentences = rawExtractedData.languageToSentences
            .mapValues { (_, sentences) ->
                sentences.map(::parseSentence)
            }
    )
}

fun parseSentence(rawSentence: RawSentence): ParsedSentence {
    val inputStreamName = rawSentence.file.absolutePath
    val constructs = try {
        val tokenizer = Tokenizer()
        tokenizer.tokenize(
            InputStreamReader(ByteArrayInputStream(rawSentence.rawConstructs.toByteArray())),
            inputStreamName,
        )
        val parser = Parser(tokenizer.tokenStream)
        parser.parseSentenceConstructList()

    } catch (e: CompilerError) {
        throw SentencesCompilerPluginException(
            "Could not parse sentence '${
                rawSentence.rawConstructs
            }' under id '${rawSentence.id}' in skill sentences file ${
                rawSentence.file.parentFile.name
            }/${rawSentence.file.name} because of '${
                e.message?.removePrefix(inputStreamName)
            }': ${rawSentence.file.absolutePath}",
            e
        )
    }

    return ParsedSentence(
        id = rawSentence.id,
        file = rawSentence.file,
        constructs = constructs,
    )
}
