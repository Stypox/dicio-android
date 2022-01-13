package org.dicio.dicio_android.eval;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dicio.dicio_android.skills.SkillHandler;
import org.dicio.skill.Skill;
import org.dicio.skill.util.CleanableUp;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class SkillRanker implements CleanableUp {

    // various thresholds for different specificity categories (high, medium and low)
    private static final float
            // first round
            highThreshold1   = 0.85f,
            // second round
            mediumThreshold2 = 0.90f,
            highThreshold2   = 0.80f,
            // third round
            lowThreshold3    = 0.90f,
            mediumThreshold3 = 0.80f,
            highThreshold3   = 0.70f;


    private static class SkillScoreResult implements CleanableUp {
        @Nullable final Skill skill;
        final float score;

        SkillScoreResult(@Nullable final Skill skill, final float score) {
            this.skill = skill;
            this.score = score;
        }

        @Override
        public void cleanup() {
            if (skill != null) {
                skill.cleanup();
            }
        }
    }

    private static class SkillBatch {
        // all of the skills by specificity category (high, medium and low)
        private final List<Skill> highSkills;
        private final List<Skill> mediumSkills;
        private final List<Skill> lowSkills;

        SkillBatch(final List<Skill> skills) {
            highSkills = new ArrayList<>();
            mediumSkills = new ArrayList<>();
            lowSkills = new ArrayList<>();

            for (final Skill skill : skills) {
                switch (skill.specificity()) {
                    case high:
                        highSkills.add(skill);
                        break;
                    case medium:
                        mediumSkills.add(skill);
                        break;
                    case low:
                        lowSkills.add(skill);
                        break;
                }
            }
        }

        private static SkillScoreResult getFirstAboveThresholdOrBest(
                final List<Skill> skills,
                final String input,
                final List<String> inputWords,
                final List<String> normalizedWordKeys,
                final float threshold) {
            // this ensures that if `skills` is empty and null skill is returned,
            // nothing bad happens since its score cannot be higher than any other float value.
            float bestScoreSoFar = Float.MIN_VALUE;
            Skill bestSkillSoFar = null;

            for (Skill skill : skills) {
                skill.setInput(input, inputWords, normalizedWordKeys);
                float score = skill.score();

                if (score > bestScoreSoFar) {
                    if (bestSkillSoFar != null) {
                        bestSkillSoFar.cleanup();
                    }

                    bestScoreSoFar = score;
                    bestSkillSoFar = skill;
                    if (score > threshold) {
                        break;
                    }
                } else {
                    skill.cleanup();
                }
            }

            return new SkillScoreResult(bestSkillSoFar, bestScoreSoFar);
        }

        @Nullable
        Skill getBest(final String input,
                      final List<String> inputWords,
                      final List<String> normalizedWordKeys) {
            // first round: considering only high-priority skills
            final SkillScoreResult bestHigh = getFirstAboveThresholdOrBest(
                    highSkills, input, inputWords, normalizedWordKeys, highThreshold1);
            if (bestHigh.score > highThreshold1) {
                return bestHigh.skill;
            }

            // second round: considering both medium- and high-priority skills
            final SkillScoreResult bestMedium = getFirstAboveThresholdOrBest(
                    mediumSkills, input, inputWords, normalizedWordKeys, mediumThreshold2);
            if (bestMedium.score > mediumThreshold2) {
                bestHigh.cleanup();
                return bestMedium.skill;
            } else if (bestHigh.score > highThreshold2) {
                bestMedium.cleanup();
                return bestHigh.skill;
            }

            // third round: all skills are considered
            final SkillScoreResult bestLow = getFirstAboveThresholdOrBest(
                    lowSkills, input, inputWords, normalizedWordKeys, lowThreshold3);
            if (bestLow.score > lowThreshold3) {
                bestHigh.cleanup();
                bestMedium.cleanup();
                return bestLow.skill;
            } else if (bestMedium.score > mediumThreshold3) {
                bestHigh.cleanup();
                bestLow.cleanup();
                return bestMedium.skill;
            } else if (bestHigh.score > highThreshold3) {
                bestMedium.cleanup();
                bestLow.cleanup();
                return bestHigh.skill;
            }

            // nothing was matched
            bestHigh.cleanup();
            bestMedium.cleanup();
            bestLow.cleanup();
            return null;
        }
    }

    private SkillBatch defaultBatch;
    private Skill fallbackSkill;
    @NonNull
    private final Stack<SkillBatch> batches;

    public SkillRanker(final List<Skill> defaultSkillBatch,
                       @NonNull final Skill fallbackSkill) {
        this.defaultBatch = new SkillBatch(defaultSkillBatch);
        this.fallbackSkill = fallbackSkill;
        this.batches = new Stack<>();
    }

    public void addBatchToTop(final List<Skill> skillBatch) {
        for (final Skill skill : skillBatch) {
            // set the context to the enqueued skills
            skill.setContext(SkillHandler.getSkillContext());
        }
        batches.push(new SkillBatch(skillBatch));
    }

    public void removeTopBatch() {
        batches.pop();
    }

    public void removeAllBatches() {
        batches.removeAllElements();
    }

    public Skill getBest(final String input,
                         final List<String> inputWords,
                         final List<String> normalizedWordKeys) {
        for(int i = batches.size() - 1; i >= 0; --i) {
            final Skill skillFromBatch
                    = batches.get(i).getBest(input, inputWords, normalizedWordKeys);
            if (skillFromBatch != null) {
                return skillFromBatch;
            }
        }

        final Skill skillFromDefault
                = defaultBatch.getBest(input, inputWords, normalizedWordKeys);
        return skillFromDefault;
    }
    public Skill getFallbackSkill(final String input,
                     final List<String> inputWords,
                     final List<String> normalizedWordKeys) {
        fallbackSkill.setInput(input, inputWords, normalizedWordKeys);
        return fallbackSkill;
    }

    @Override
    public void cleanup() {
        defaultBatch = null;
        fallbackSkill = null;
        batches.clear();
    }
}
