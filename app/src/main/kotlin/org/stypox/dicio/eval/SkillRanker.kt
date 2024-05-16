package org.stypox.dicio.eval

import org.dicio.skill.Skill
import org.dicio.skill.SkillContext
import org.dicio.skill.chain.InputRecognizer.Specificity
import org.dicio.skill.util.CleanableUp
import org.stypox.dicio.skills.SkillHandler
import java.util.Stack

class SkillRanker(
    defaultSkillBatch: List<Skill>,
    private var fallbackSkill: Skill
) : CleanableUp {
    private class SkillScoreResult(val skill: Skill?, val score: Float) :
        CleanableUp {
        override fun cleanup() {
            skill?.cleanup()
        }
    }

    private class SkillBatch(skills: List<Skill>) {
        // all of the skills by specificity category (high, medium and low)
        private val highSkills: MutableList<Skill> = ArrayList()
        private val mediumSkills: MutableList<Skill> = ArrayList()
        private val lowSkills: MutableList<Skill> = ArrayList()

        init {
            for (skill in skills) {
                when (skill.specificity) {
                    Specificity.HIGH -> highSkills.add(skill)
                    Specificity.MEDIUM -> mediumSkills.add(skill)
                    Specificity.LOW -> lowSkills.add(skill)
                }
            }
        }

        fun getBest(
            input: String,
            inputWords: List<String>,
            normalizedWordKeys: List<String>
        ): Skill? {
            // first round: considering only high-priority skills
            val bestHigh = getFirstAboveThresholdOrBest(
                highSkills, input, inputWords, normalizedWordKeys, HIGH_THRESHOLD_1
            )
            if (bestHigh.score > HIGH_THRESHOLD_1) {
                return bestHigh.skill
            }

            // second round: considering both medium- and high-priority skills
            val bestMedium = getFirstAboveThresholdOrBest(
                mediumSkills, input, inputWords, normalizedWordKeys, MEDIUM_THRESHOLD_2
            )
            if (bestMedium.score > MEDIUM_THRESHOLD_2) {
                bestHigh.cleanup()
                return bestMedium.skill
            } else if (bestHigh.score > HIGH_THRESHOLD_2) {
                bestMedium.cleanup()
                return bestHigh.skill
            }

            // third round: all skills are considered
            val bestLow = getFirstAboveThresholdOrBest(
                lowSkills, input, inputWords, normalizedWordKeys, LOW_THRESHOLD_3
            )
            if (bestLow.score > LOW_THRESHOLD_3) {
                bestHigh.cleanup()
                bestMedium.cleanup()
                return bestLow.skill
            } else if (bestMedium.score > MEDIUM_THRESHOLD_3) {
                bestHigh.cleanup()
                bestLow.cleanup()
                return bestMedium.skill
            } else if (bestHigh.score > HIGH_THRESHOLD_3) {
                bestMedium.cleanup()
                bestLow.cleanup()
                return bestHigh.skill
            }

            // nothing was matched
            bestHigh.cleanup()
            bestMedium.cleanup()
            bestLow.cleanup()
            return null
        }

        companion object {
            private fun getFirstAboveThresholdOrBest(
                skills: List<Skill>,
                input: String,
                inputWords: List<String>,
                normalizedWordKeys: List<String>,
                threshold: Float
            ): SkillScoreResult {
                // this ensures that if `skills` is empty and null skill is returned,
                // nothing bad happens since its score cannot be higher than any other float value.
                var bestScoreSoFar = Float.MIN_VALUE
                var bestSkillSoFar: Skill? = null
                for (skill in skills) {
                    skill.setInput(input, inputWords, normalizedWordKeys)
                    val score = skill.score()
                    if (score > bestScoreSoFar) {
                        bestSkillSoFar?.cleanup()
                        bestScoreSoFar = score
                        bestSkillSoFar = skill
                        if (score > threshold) {
                            break
                        }
                    } else {
                        skill.cleanup()
                    }
                }
                return SkillScoreResult(bestSkillSoFar, bestScoreSoFar)
            }
        }
    }

    private var defaultBatch: SkillBatch = SkillBatch(defaultSkillBatch)
    private val batches: Stack<SkillBatch> = Stack()

    fun addBatchToTop(skillContext: SkillContext, skillBatch: List<Skill>) {
        for (skill in skillBatch) {
            // set the context to the enqueued skills
            skill.setContext(skillContext)
        }
        batches.push(SkillBatch(skillBatch))
    }

    fun hasAnyBatches(): Boolean {
        return batches.isNotEmpty()
    }

    fun removeTopBatch() {
        batches.pop()
    }

    fun removeAllBatches() {
        batches.removeAllElements()
    }

    fun getBest(
        input: String,
        inputWords: List<String>,
        normalizedWordKeys: List<String>
    ): Skill? {
        for (i in batches.indices.reversed()) {
            val skillFromBatch = batches[i].getBest(input, inputWords, normalizedWordKeys)
            if (skillFromBatch != null) {
                // found a matching skill: remove all skills in batch above it
                for (j in i + 1 until batches.size) {
                    removeTopBatch()
                }
                return skillFromBatch
            }
        }
        return defaultBatch.getBest(input, inputWords, normalizedWordKeys)
    }

    fun getFallbackSkill(
        input: String,
        inputWords: List<String>,
        normalizedWordKeys: List<String>
    ): Skill {
        fallbackSkill.setInput(input, inputWords, normalizedWordKeys)
        return fallbackSkill
    }

    override fun cleanup() {
        fallbackSkill.cleanup()
        batches.clear()
    }

    companion object {
        // various thresholds for different specificity categories (high, medium and low)
        // first round
        private const val HIGH_THRESHOLD_1 = 0.85f

        // second round
        private const val MEDIUM_THRESHOLD_2 = 0.90f
        private const val HIGH_THRESHOLD_2 = 0.80f

        // third round
        private const val LOW_THRESHOLD_3 = 0.90f
        private const val MEDIUM_THRESHOLD_3 = 0.80f
        private const val HIGH_THRESHOLD_3 = 0.70f
    }
}
