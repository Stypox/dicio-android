package org.dicio.skill.old_standard_impl

import org.dicio.skill.old_standard_impl.word.BaseWord
import org.dicio.skill.old_standard_impl.word.CapturingGroup
import org.dicio.skill.old_standard_impl.word.StringWord

class Sentence(
    val sentenceId: String,
    private val startingWordIndices: IntArray,
    private vararg val words: BaseWord
) {
    private var inputWords: List<String>? = null
    private var normalizedInputWords: List<String>? = null
    private var memory: Array<Array<Array<PartialScoreResult?>>>? = null

    fun score(
        inputWords: List<String>,
        normalizedInputWords: List<String>
    ): PartialScoreResult {
        this.inputWords = inputWords
        this.normalizedInputWords = normalizedInputWords
        this.memory = Array(words.size) { Array(inputWords.size) { arrayOfNulls(2) } }
        val inputWordCount = inputWords.size

        var bestResult: PartialScoreResult = bestScore(startingWordIndices[0], 0, false)
        for (i in 1 until startingWordIndices.size) {
            bestResult = bestScore(startingWordIndices[i], 0, false)
                .keepBest(bestResult, inputWordCount)
        }

        // cleanup to prevent memory leaks
        this.inputWords = null
        this.normalizedInputWords = null
        this.memory = null
        return bestResult
    }

    ///////////
    // SCORE //
    ///////////
    /**
     * Dynamic programming implementation along two dimensions
     * Complexity: O(numSentenceWords * numInputWords)
     */
    fun bestScore(
        wordIndex: Int,
        inputWordIndex: Int,
        foundWordAfterStart: Boolean
    ): PartialScoreResult {
        if (wordIndex >= words.size) {
            return PartialScoreResult(
                0,
                if (inputWordIndex < inputWords!!.size) inputWords!!.size - inputWordIndex else 0
            )
        } else if (inputWordIndex >= inputWords!!.size) {
            return PartialScoreResult(words[wordIndex].minimumSkippedWordsToEnd, 0)
        }

        val foundWordAfterStartInt = if (foundWordAfterStart) 1 else 0
        memory!![wordIndex][inputWordIndex][foundWordAfterStartInt]?.let {
            return PartialScoreResult(it)
        }

        val result = if (words[wordIndex] is CapturingGroup) {
            bestScoreCapturingGroup(wordIndex, inputWordIndex, foundWordAfterStart)
        } else {
            bestScoreNormalWord(wordIndex, inputWordIndex, foundWordAfterStart)
        }

        memory!![wordIndex][inputWordIndex][foundWordAfterStartInt] = result
        return PartialScoreResult(result) // clone object to prevent edits
    }

    private fun bestScoreCapturingGroup(
        wordIndex: Int,
        inputWordIndex: Int,
        foundWordAfterStart: Boolean
    ): PartialScoreResult {
        // do not use recursion here, to make checking whether anything was captured simpler
        // first try to skip capturing group
        var result = bestScore(
            words[wordIndex].nextIndices[0],
            inputWordIndex, foundWordAfterStart
        )
            .skipCapturingGroup()
        for (i in 1 until words[wordIndex].nextIndices.size) {
            result = bestScore(
                words[wordIndex].nextIndices[i],
                inputWordIndex, foundWordAfterStart
            )
                .skipCapturingGroup()
                .keepBest(result, inputWords!!.size)
        }

        // then try various increasing lengths of capturing group
        for (i in inputWords!!.size downTo inputWordIndex + 1) {
            for (nextIndex in words[wordIndex].nextIndices) {
                // keepBest will keep the current (i.e. latest) result in case of equality
                // so smaller capturing groups are preferred (leading to more specific sentences)
                result = bestScore(nextIndex, i, true)
                    .setCapturingGroup(
                        (words[wordIndex] as CapturingGroup).name,
                        InputWordRange(inputWordIndex, i)
                    )
                    .keepBest(result, inputWords!!.size)
            }
        }

        return result
    }

    private fun bestScoreNormalWord(
        wordIndex: Int,
        inputWordIndex: Int,
        foundWordAfterStart: Boolean
    ): PartialScoreResult {
        var result = bestScore(wordIndex, inputWordIndex + 1, foundWordAfterStart)
            .skipInputWord(foundWordAfterStart)

        if ((words[wordIndex] as StringWord).matches(
                inputWords!![inputWordIndex], normalizedInputWords!![inputWordIndex]
            )
        ) {
            for (nextIndex in words[wordIndex].nextIndices) {
                result = bestScore(nextIndex, inputWordIndex + 1, true)
                    .matchWord()
                    .keepBest(result, inputWords!!.size)
            }
        } else {
            for (nextIndex in words[wordIndex].nextIndices) {
                result = bestScore(nextIndex, inputWordIndex, foundWordAfterStart)
                    .skipWord()
                    .keepBest(result, inputWords!!.size)
            }
        }

        return result
    }
}
