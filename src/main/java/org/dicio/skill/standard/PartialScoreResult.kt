package org.dicio.skill.standard

import kotlin.math.abs

class PartialScoreResult {
    private var matchedWords: Int
    private var skippedWords: Int
    private var skippedInputWordsSides: Int
    private var skippedInputWordsAmid: Int
    var wordsInCapturingGroups: Int
        private set
    private var foundWordBeforeEnd: Boolean
    private val capturingGroups: MutableMap<String, InputWordRange>

    /**
     * Deep copy constructor
     */
    constructor(skippedWordsEnd: Int, skippedInputWordsEnd: Int) {
        matchedWords = 0
        skippedWords = skippedWordsEnd
        skippedInputWordsSides = skippedInputWordsEnd
        skippedInputWordsAmid = 0
        wordsInCapturingGroups = 0
        foundWordBeforeEnd = false
        capturingGroups = HashMap()
    }

    constructor(other: PartialScoreResult) {
        matchedWords = other.matchedWords
        skippedWords = other.skippedWords
        skippedInputWordsSides = other.skippedInputWordsSides
        skippedInputWordsAmid = other.skippedInputWordsAmid
        wordsInCapturingGroups = other.wordsInCapturingGroups
        foundWordBeforeEnd = other.foundWordBeforeEnd

        capturingGroups = HashMap()
        for ((key, value) in other.capturingGroups) {
            capturingGroups[key] = InputWordRange(value)
        }
    }


    fun value(inputWordCount: Int): Float {
        if (inputWordCount == 0) {
            return 0.0f
        }

        var calculatedScore = 1.0f
        if (matchedWords != 0 || skippedWords != 0) {
            calculatedScore *= dropAt0point75(
                matchedWords.toFloat() / (matchedWords + skippedWords)
            )
        }
        if (inputWordCount != wordsInCapturingGroups) {
            calculatedScore *= dropAt0point6(
                (inputWordCount
                        - wordsInCapturingGroups
                        - skippedInputWordsSides
                        - skippedInputWordsAmid).toFloat() / (inputWordCount - wordsInCapturingGroups)
            )
        }


        // eliminate floating point errors
        if (calculatedScore > 1.0f) {
            return 1.0f
        } else if (calculatedScore < 0.0f) {
            return 0.0f
        }
        return calculatedScore
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("{matchedWords=")
        stringBuilder.append(matchedWords)
        stringBuilder.append(", skippedWords=")
        stringBuilder.append(skippedWords)
        stringBuilder.append(", skippedInputWordsSides=")
        stringBuilder.append(skippedInputWordsSides)
        stringBuilder.append(", skippedInputWordsAmid=")
        stringBuilder.append(skippedInputWordsAmid)
        stringBuilder.append(", wordsInCapturingGroups=")
        stringBuilder.append(wordsInCapturingGroups)

        stringBuilder.append(", capturingGroups=[")
        for ((key, value) in capturingGroups) {
            stringBuilder.append(key)
            stringBuilder.append("=")
            stringBuilder.append(value.toString())
            stringBuilder.append(";")
        }
        stringBuilder.append("]}")

        return stringBuilder.toString()
    }

    fun toStandardResult(sentenceId: String, input: String): StandardResult {
        // assume bestResult has already been calculated
        return StandardResult(sentenceId, input, capturingGroups)
    }


    fun skipInputWord(foundWordAfterStart: Boolean): PartialScoreResult {
        if (foundWordBeforeEnd && foundWordAfterStart) {
            ++skippedInputWordsAmid
        } else {
            ++skippedInputWordsSides
        }
        return this
    }

    fun matchWord(): PartialScoreResult {
        foundWordBeforeEnd = true
        ++matchedWords
        return this
    }

    fun skipWord(): PartialScoreResult {
        ++skippedWords
        return this
    }

    fun skipCapturingGroup(): PartialScoreResult {
        skippedWords += 2
        return this
    }

    fun setCapturingGroup(id: String, range: InputWordRange): PartialScoreResult {
        foundWordBeforeEnd = true
        ++matchedWords
        capturingGroups[id] = range
        wordsInCapturingGroups += range.to() - range.from()
        return this
    }


    /**
     * In case of equality, `this` is preferred
     */
    fun keepBest(
        other: PartialScoreResult,
        inputWordCount: Int
    ): PartialScoreResult {
        var thisValue = this.value(inputWordCount)
        var otherValue = other.value(inputWordCount)

        // boost matches with less words in capturing groups, but only if not skipped more words
        if (this.skippedWords == other.skippedWords) {
            val sumWordsInCapturingGroups =
                this.wordsInCapturingGroups + other.wordsInCapturingGroups
            if (sumWordsInCapturingGroups != 0) {
                thisValue += 0.025f * (1.0f
                        - (wordsInCapturingGroups.toFloat()) / sumWordsInCapturingGroups)
                otherValue += 0.025f * (1.0f
                        - (other.wordsInCapturingGroups.toFloat()) / sumWordsInCapturingGroups)
            }
        }

        return if (thisValue >= otherValue) this else other
    }

    companion object {
        fun dropAt0point75(x: Float): Float {
            // similar to a sigmoid; it has LOW values in range [0,0.75) and HIGH values otherwise
            return ((171f * (x - .65f) / (.2f + abs((x - .75f).toDouble())) + 117f) / 250f).toFloat()
        }

        fun dropAt0point6(x: Float): Float {
            // similar to a sigmoid; it has LOW values in range [0,0.6) and HIGH values otherwise
            return ((28 * (x - .55f) / (.15f + abs((x - .55f).toDouble())) + 22f) / 43f).toFloat()
        }
    }
}
