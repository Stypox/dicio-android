package org.dicio.skill.old_standard

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.dicio.skill.old_standard_impl.InputWordRange
import org.dicio.skill.old_standard_impl.Sentence
import org.dicio.skill.old_standard_impl.word.BaseWord
import org.dicio.skill.old_standard_impl.word.CapturingGroup
import org.dicio.skill.old_standard_impl.word.DiacriticsInsensitiveWord
import org.dicio.skill.old_standard_impl.word.DiacriticsSensitiveWord
import org.dicio.skill.old_standard.WordExtractor.extractWords
import org.dicio.skill.old_standard.WordExtractor.normalizeWords
import org.dicio.skill.standard2.helper.nfkdNormalizeWord


private const val FLOAT_EQUALS_DELTA: Float = 0.0001f

class SentenceTest : StringSpec({
    "0 capturing groups" {
        val s = sent("hello how are you")

        assertSentence(s, "hello how are you", 1.0f, 1.0f, null, null)
        assertSentence(s, "hello how is you", 0.7f, 0.8f, null, null)
        assertSentence(s, "hello how are you bob", 0.9f, 1.0f, null, null)
        assertSentence(s, "mary", 0.0f, 0.0f, null, null)
        assertSentence(s, "", 0.0f, 0.0f, null, null)
    }


    "1 capturing groups" {
        val s = sent("hello", "how are you")

        assertSentence(s, "hello bob how are you", 1.0f, 1.0f, "bob", null)
        assertSentence(s, "hello bob and mary how is you", 0.7f, 0.8f, "bob and mary", null)
        assertSentence(s, "hello mary how are steaks inside you", 0.7f, 0.8f, "mary", null)
        assertSentence(s, "hello bob how are steaks doing inside you", 0.5f, 0.6f, "bob", null)
        assertSentence(s, "hi hello bob how are not you", 0.7f, 0.8f, "bob", null)
        assertSentence(s, "hi hello mary and bob how are not you", 0.7f, 0.8f, "mary and bob", null)
        assertSentence(s, "hello mary", 0.1f, 0.2f, "mary", null)
        assertSentence(s, "bob how are you", 0.8f, 0.9f, "bob", null)
        assertSentence(s, "mary and bob", 0.0f, 0.1f, "mary and bob", null)
        assertSentence(s, "hello how are you", 0.8f, 0.9f, "how", null)
        assertSentence(s, "", 0.0f, 0.0f, null, null)
        assertSentence(s, "a a a a hello b how are you", 0.8f, 0.9f, "a a a a hello b", null)
        assertSentence(s, "hello b how a a a a are you", 0.8f, 0.9f, "b how a a a a", null)
    }

    "1 capturing group, with nothing on the left" {
        val s = sent("", "how are you")

        assertSentence(s, "hello bob how are you", 1.0f, 1.0f, "hello bob", null)
        assertSentence(s, "hello bob and mary how is you", 0.6f, 0.7f, "hello bob and mary", null)
        assertSentence(s, "hello mary how are steaks inside you", 0.6f, 0.7f, "hello mary", null)
        assertSentence(s, "hello bob how are steaks doing inside you", 0.3f, 0.4f, "hello bob", null)
        assertSentence(s, "hi hello bob how are not you", 0.8f, 0.9f, "hi hello bob", null)
        assertSentence(s, "bob how are you", 1.0f, 1.0f, "bob", null)
        assertSentence(s, "mary and bob", 0.0f, 0.1f, "mary and bob", null)
        assertSentence(s, "how are you", 0.8f, 0.9f, "how", null)
        assertSentence(s, "", 0.0f, 0.0f, null, null)
    }

    "1 capturing group, with nothing on the right" {
        val s = sent("hello", "")

        assertSentence(s, "hello bob", 1.0f, 1.0f, "bob", null)
        assertSentence(s, "hello bob and mary", 1.0f, 1.0f, "bob and mary", null)
        assertSentence(s, "hi hello bob", 0.3f, 0.4f, "bob", null)
        assertSentence(s, "hi hi hello bob", 0.2f, 0.3f, "hi hi hello bob", null)
        assertSentence(s, "mary and bob", 0.2f, 0.3f, "mary and bob", null)
        assertSentence(s, "hello", 0.1f, 0.2f, null, null)
        assertSentence(s, "", 0.0f, 0.0f, null, null)
    }


    "2 capturing groups" {
        val s = sent("i want", "liters of", "please")

        assertSentence(s, "i want five liters of milk please", 1.0f, 1.0f, "five", "milk")
        assertSentence(s, "i want five and a half liters of soy milk please", 1.0f, 1.0f, "five and a half", "soy milk")
        assertSentence(s, "i want five liters of milk", 0.9f, 1.0f, "five", "milk")
        assertSentence(s, "five and a half liters of soy milk", 0.3f, 0.4f, "five and a half", "soy milk")
        assertSentence(s, "i want one liter of milk please", 0.9f, 1.0f, "one liter", "milk")
        assertSentence(s, "i want one liter milk please", 0.6f, 0.7f, "one", "liter milk")
        assertSentence(s, "i want one liter soy milk please", 0.6f, 0.7f, "one", "liter soy milk")
        assertSentence(s, "i want one liter of milk", 0.6f, 0.7f, "one liter", "milk")
        assertSentence(s, "one liter of soy milk", 0.1f, 0.2f, "one liter", "soy milk")
        assertSentence(s, "i want milk please", 0.3f, 0.4f, "milk", "please")
        assertSentence(s, "i want please", 0.1f, 0.2f, "please", null)
        assertSentence(s, "i do want please", 0.3f, 0.4f, "do", "want")
        assertSentence(s, "i want", 0.0f, 0.1f, null, null)
        assertSentence(s, "you want five liters of milk please", 0.8f, 0.9f, "five", "milk")
        assertSentence(s, "i need five liters of milk please", 0.9f, 1.0f, "need five", "milk")
        assertSentence(s, "you need five liters of milk please", 0.6f, 0.7f, "you need five", "milk")
        assertSentence(s, "i want five liters of soy milk", 0.9f, 1.0f, "five", "soy milk")
        assertSentence(s, "i need five liters of soy milk", 0.6f, 0.7f, "need five", "soy milk")
        assertSentence(s, "one soy milk", 0.0f, 0.1f, "one", "soy milk")
        assertSentence(s, "milk", 0.0f, 0.1f, "milk", null)
        assertSentence(s, "", 0.0f, 0.0f, null, null)
        assertSentence(s, "i a a a a want f liters of milk please", 0.9f, 1.0f, "a a a a want f", "milk")
        assertSentence(s, "i want five liters of m please a a a a", 0.5f, 0.6f, "five", "m")
    }

    "2 capturing groups, with nothing on the left" {
        val s = sent("", "liters of", "please")

        assertSentence(s, "five liters of milk please", 1.0f, 1.0f, "five", "milk")
        assertSentence(s, "five and a half liters of soy milk please", 1.0f, 1.0f, "five and a half", "soy milk")
        assertSentence(s, "five liters of milk", 0.8f, 0.9f, "five", "milk")
        assertSentence(s, "one liter of milk please", 0.8f, 0.9f, "one liter", "milk")
        assertSentence(s, "one liter soy milk please", 0.3f, 0.4f, "one", "liter soy milk")
        assertSentence(s, "one liter of milk", 0.3f, 0.4f, "one liter", "milk")
        assertSentence(s, "milk please", 0.1f, 0.2f, "milk", "please")
        assertSentence(s, "please", 0.0f, 0.1f, "please", null)
        assertSentence(s, "one soy milk", 0.1f, 0.2f, "one", "soy milk")
        assertSentence(s, "milk", 0.0f, 0.1f, "milk", null)
        assertSentence(s, "", 0.0f, 0.0f, null, null)
    }

    "2 capturing groups, with nothing on the right" {
        val s = sent("i want", "liters of", "")

        assertSentence(s, "i want five liters of milk", 1.0f, 1.0f, "five", "milk")
        assertSentence(s, "i want five and a half liters of soy milk", 1.0f, 1.0f, "five and a half", "soy milk")
        assertSentence(s, "five and a half liters of soy milk", 0.5f, 0.6f, "five and a half", "soy milk")
        assertSentence(s, "i want one liter of milk", 0.9f, 1.0f, "one liter", "milk")
        assertSentence(s, "i want one liter milk", 0.5f, 0.6f, "one", "liter milk")
        assertSentence(s, "one liter of soy milk", 0.2f, 0.3f, "one liter", "soy milk")
        assertSentence(s, "i want milk", 0.1f, 0.2f, "milk", null)
        assertSentence(s, "i want", 0.0f, 0.1f, null, null)
        assertSentence(s, "you want five liters of milk", 0.8f, 0.9f, "five", "milk")
        assertSentence(s, "i need five liters of milk", 0.9f, 1.0f, "need five", "milk")
        assertSentence(s, "you need five liters of milk", 0.5f, 0.6f, "you need five", "milk")
        assertSentence(s, "one soy milk", 0.1f, 0.2f, "one", "soy milk")
        assertSentence(s, "milk", 0.0f, 0.1f, "milk", null)
        assertSentence(s, "", 0.0f, 0.0f, null, null)
    }

    "2 capturing groups, with nothing on the left and right sides" {
        val s = sent("", "and", "")

        assertSentence(s, "bob and mary", 1.0f, 1.0f, "bob", "mary")
        assertSentence(s, "bob and mary and simon", 1.0f, 1.0f, "bob", "mary and simon")
        assertSentence(s, "bob mary", 0.5f, 0.6f, "bob", "mary")
        assertSentence(s, "and mary", 0.5f, 0.6f, "and", "mary")
        assertSentence(s, "bob and", 0.2f, 0.3f, "bob", null)
        assertSentence(s, "", 0.0f, 0.0f, null, null)
    }


    "duplicate word" {
        val s = sent("how do you do bob")

        assertSentence(s, "how do you do bob", 1.0f, 1.0f, null, null)
        assertSentence(s, "how does you do bob", 0.8f, 0.9f, null, null)
        assertSentence(s, "how does a you do bob", 0.6f, 0.7f, null, null)
    }

    "optional followed by capturing group" {
        val s = Sentence(
            "", intArrayOf(0),
            diw("open", 1, 1, 3),
            diw("the", 2, 2),
            dsw("application", 1, 3),
            capt("0", 0, 4)
        )
        assertSentence(s, "open newpipe", 1.0f, 1.0f, "newpipe", null)
        assertSentence(s, "open the application newpipe", 1.0f, 1.0f, "newpipe", null)
        assertSentence(s, "open the newest newpipe", 1.0f, 1.0f, "the newest newpipe", null)
    }

    "capturing group followed by optional" {
        val s = Sentence(
            "", intArrayOf(0),
            dsw("buy", 1, 1),
            capt("0", 0, 2, 3),
            diw("please", 0, 3)
        )

        assertSentence(s, "buy please", 1.0f, 1.0f, "please", null)
        assertSentence(s, "buy soy please", 1.0f, 1.0f, "soy", null)
        assertSentence(s, "buy soy milk", 1.0f, 1.0f, "soy milk", null)
    }

    "optional capturing group" {
        val s = Sentence(
            "", intArrayOf(0),
            diw("weather", 1, 1, 2),
            capt("0", 0, 2)
        )

        assertSentence(s, "weather", 1.0f, 1.0f, null, null)
        assertSentence(s, "weather new", 1.0f, 1.0f, "new", null)
        assertSentence(s, "weather new york", 1.0f, 1.0f, "new york", null)
    }
})

private fun dsw(
    value: String,
    minimumSkippedWordsToEnd: Int,
    vararg nextIndices: Int
): DiacriticsSensitiveWord {
    return DiacriticsSensitiveWord(value, minimumSkippedWordsToEnd, *nextIndices)
}

private fun diw(
    value: String,
    minimumSkippedWordsToEnd: Int,
    vararg nextIndices: Int
): DiacriticsInsensitiveWord {
    return DiacriticsInsensitiveWord(
        nfkdNormalizeWord(value),
        minimumSkippedWordsToEnd, *nextIndices
    )
}

private fun capt(
    name: String,
    minimumSkippedWordsToEnd: Int,
    vararg nextIndices: Int
): CapturingGroup {
    return CapturingGroup(name, minimumSkippedWordsToEnd, *nextIndices)
}

private fun addAllWords(
    packWords: List<String>,
    words: MutableList<BaseWord>,
    minimumSkippedWordsToEnd: Int
) {
    for (i in packWords.indices) {
        words.add(
            dsw(
                packWords[i],
                minimumSkippedWordsToEnd + packWords.size - i, words.size + 1
            )
        )
    }
}

private fun addCapturingGroup(
    index: Int,
    words: MutableList<BaseWord>,
    minimumSkippedWordsToEnd: Int
) {
    words.add(
        capt(
            index.toString(),
            minimumSkippedWordsToEnd + 2, words.size + 1
        )
    )
}


private fun sent(pack1: String): Sentence {
    val words: MutableList<BaseWord> = ArrayList()
    val pack1Words = extractWords(pack1)

    addAllWords(pack1Words, words, 0)
    return Sentence("", intArrayOf(0), *words.toTypedArray<BaseWord>())
}

private fun sent(pack1: String, pack2: String): Sentence {
    val words: MutableList<BaseWord> = ArrayList()
    val pack1Words = extractWords(pack1)
    val pack2Words = extractWords(pack2)

    addAllWords(pack1Words, words, 2 + pack2Words.size)
    addCapturingGroup(0, words, pack2Words.size)
    addAllWords(pack2Words, words, 0)
    return Sentence("", intArrayOf(0), *words.toTypedArray<BaseWord>())
}

private fun sent(pack1: String, pack2: String, pack3: String): Sentence {
    val words: MutableList<BaseWord> = ArrayList()
    val pack1Words = extractWords(pack1)
    val pack2Words = extractWords(pack2)
    val pack3Words = extractWords(pack3)

    addAllWords(pack1Words, words, 4 + pack2Words.size + pack3Words.size)
    addCapturingGroup(0, words, 2 + pack2Words.size + pack3Words.size)
    addAllWords(pack2Words, words, 2 + pack3Words.size)
    addCapturingGroup(1, words, pack3Words.size)
    addAllWords(pack3Words, words, 0)
    return Sentence("", intArrayOf(0), *words.toTypedArray<BaseWord>())
}


fun assertCapturingGroup(
    inputWords: List<String>,
    range: InputWordRange?,
    captGr: String?
) {
    if (captGr == null) {
        range.shouldBeNull()
        return
    }

    val captGrWords = extractWords(captGr)
    val actualCaptGrWords: MutableList<String> = ArrayList()
    for (i in range!!.from() until range.to()) {
        actualCaptGrWords.add(inputWords[i])
    }

    actualCaptGrWords shouldBe captGrWords
}

private fun assertSentence(
    s: Sentence, input: String,
    a: Float, b: Float,
    captGr0: String?, captGr1: String?
) {
    val inputWords = extractWords(input)
    val normalizedInputWords = normalizeWords(inputWords)
    val scoreResult = s.score(inputWords, normalizedInputWords)
    val score = scoreResult.value(inputWords.size)

    if (a == b) {
        score shouldBe a.plusOrMinus(FLOAT_EQUALS_DELTA)
    } else {
        withClue("Score $score $scoreResult is not in range [$a, $b]") {
            (score in a..b).shouldBeTrue()
        }
    }

    val r = scoreResult.toStandardResult(s.sentenceId, input)
    r.capturingGroupRanges.shouldHaveSize(
        (if (captGr0 != null) 1 else 0) + (if (captGr1 != null) 1 else 0))
    assertCapturingGroup(inputWords, r.capturingGroupRanges["0"], captGr0)
    assertCapturingGroup(inputWords, r.capturingGroupRanges["1"], captGr1)
}
