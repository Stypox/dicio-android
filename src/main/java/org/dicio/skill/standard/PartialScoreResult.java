package org.dicio.skill.standard;

import java.util.HashMap;
import java.util.Map;

class PartialScoreResult {

    static float dropAt0point75(final float x) {
        // similar to a sigmoid; it has low values in range [0,0.75) and high values otherwise
        return (171f * (x-.65f)/(.2f+Math.abs(x-.75f)) + 117f) / 250f;
    }

    static float dropAt0point6(final float x) {
        // similar to a sigmoid; it has low values in range [0,0.6) and high values otherwise
        return (28 * (x-.55f)/(.15f+Math.abs(x-.55f)) + 22f) / 43f;
    }


    private int matchedWords;
    private int skippedWords;
    private int skippedInputWordsSides;
    private int skippedInputWordsAmid;
    private int wordsInCapturingGroups;
    private boolean foundWordBeforeEnd;
    private Map<String, InputWordRange> capturingGroups;

    /**
     * Deep copy constructor
     */
    PartialScoreResult(final int skippedWordsEnd, final int skippedInputWordsEnd) {
        matchedWords = 0;
        skippedWords = skippedWordsEnd;
        skippedInputWordsSides = skippedInputWordsEnd;
        skippedInputWordsAmid = 0;
        wordsInCapturingGroups = 0;
        foundWordBeforeEnd = false;
        capturingGroups = new HashMap<>();
    }

    PartialScoreResult(final PartialScoreResult other) {
        matchedWords = other.matchedWords;
        skippedWords = other.skippedWords;
        skippedInputWordsSides = other.skippedInputWordsSides;
        skippedInputWordsAmid = other.skippedInputWordsAmid;
        wordsInCapturingGroups = other.wordsInCapturingGroups;
        foundWordBeforeEnd = other.foundWordBeforeEnd;

        capturingGroups = new HashMap<>();
        for (Map.Entry<String, InputWordRange> entry : other.capturingGroups.entrySet()) {
            capturingGroups.put(entry.getKey(), new InputWordRange(entry.getValue()));
        }
    }


    float value(final int inputWordCount) {
        if (inputWordCount == 0) {
            return 0.0f;
        }

        float calculatedScore = 1.0f;
        if (matchedWords != 0 || skippedWords != 0) {
            calculatedScore *= dropAt0point75(
                    (float) matchedWords / (matchedWords + skippedWords));
        }
        if (inputWordCount != wordsInCapturingGroups) {
            calculatedScore *= dropAt0point6((float) (inputWordCount
                    - wordsInCapturingGroups
                    - skippedInputWordsSides
                    - skippedInputWordsAmid) / (inputWordCount - wordsInCapturingGroups));
        }


        // eliminate floating point errors
        if (calculatedScore > 1.0f) {
            return 1.0f;
        } else if (calculatedScore < 0.0f) {
            return 0.0f;
        }
        return calculatedScore;
    }

    public Map<String, InputWordRange> getCapturingGroups() {
        return capturingGroups;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{matchedWords=");
        stringBuilder.append(matchedWords);
        stringBuilder.append(", skippedWords=");
        stringBuilder.append(skippedWords);
        stringBuilder.append(", skippedInputWordsSides=");
        stringBuilder.append(skippedInputWordsSides);
        stringBuilder.append(", skippedInputWordsAmid=");
        stringBuilder.append(skippedInputWordsAmid);
        stringBuilder.append(", wordsInCapturingGroups=");
        stringBuilder.append(wordsInCapturingGroups);

        stringBuilder.append(", capturingGroups=[");
        for (final Map.Entry<String, InputWordRange> capturingGroup : capturingGroups.entrySet()) {
            stringBuilder.append(capturingGroup.getKey());
            stringBuilder.append("=");
            stringBuilder.append(capturingGroup.getValue().toString());
            stringBuilder.append(";");
        }
        stringBuilder.append("]}");

        return stringBuilder.toString();
    }

    public StandardResult toStandardResult(final String sentenceId, final String input) {
        // assume bestResult has already been calculated
        return new StandardResult(sentenceId, input, getCapturingGroups());
    }


    PartialScoreResult skipInputWord(final boolean foundWordAfterStart) {
        if (foundWordBeforeEnd && foundWordAfterStart) {
            ++skippedInputWordsAmid;
        } else {
            ++skippedInputWordsSides;
        }
        return this;
    }

    PartialScoreResult matchWord() {
        foundWordBeforeEnd = true;
        ++matchedWords;
        return this;
    }

    PartialScoreResult skipWord() {
        ++skippedWords;
        return this;
    }

    PartialScoreResult skipCapturingGroup() {
        skippedWords += 2;
        return this;
    }

    PartialScoreResult setCapturingGroup(final String id, final InputWordRange range) {
        foundWordBeforeEnd = true;
        ++matchedWords;
        capturingGroups.put(id, range);
        wordsInCapturingGroups += range.to() - range.from();
        return this;
    }


    /**
     * In case of equality, {@code this} is preferred
     */
    PartialScoreResult keepBest(final PartialScoreResult other,
                                final int inputWordCount) {
        float thisValue = this.value(inputWordCount);
        float otherValue = other.value(inputWordCount);

        // boost matches with less words in capturing groups, but only if not skipped more words
        if (this.skippedWords == other.skippedWords) {
            int sumWordsInCapturingGroups =
                    this.wordsInCapturingGroups + other.wordsInCapturingGroups;
            if (sumWordsInCapturingGroups != 0) {
                thisValue += 0.025f * (1.0f
                        - ((float) this.wordsInCapturingGroups) / sumWordsInCapturingGroups);
                otherValue += 0.025f * (1.0f
                        - ((float) other.wordsInCapturingGroups) / sumWordsInCapturingGroups);
            }
        }

        return thisValue >= otherValue ? this : other;
    }
}
