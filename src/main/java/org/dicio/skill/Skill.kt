package org.dicio.skill

import androidx.annotation.CallSuper
import org.dicio.skill.chain.InputRecognizer
import org.dicio.skill.chain.InputRecognizer.Specificity
import org.dicio.skill.chain.IntermediateProcessor
import org.dicio.skill.chain.OutputGenerator
import org.dicio.skill.util.CleanableUp
import org.dicio.skill.util.NextSkills

/**
 * A skill is the component that scores input, processes it and finally generates output. Take a
 * look at [org.dicio.skill.chain.ChainSkill] for a class that separates these three things.
 */
abstract class Skill : SkillComponent(), NextSkills, CleanableUp {
    // no next skills by default
    private var nextSkills: List<Skill> = listOf()

    /**
     * @see InputRecognizer.specificity
     */
    abstract fun specificity(): Specificity

    /**
     * @see InputRecognizer.setInput
     */
    abstract fun setInput(
        input: String,
        inputWords: List<String>,
        normalizedWordKeys: List<String>
    )

    /**
     * @see InputRecognizer.score
     */
    abstract fun score(): Float

    /**
     * This will be called if this skill was deemed as the best one which could provide output for
     * what the user requested and it should therefore process the input previously received with
     * [.setInput], so that when [.generateOutput] is called
     * everything is ready.
     *
     * @see IntermediateProcessor.process
     */
    @Throws(Exception::class)
    abstract fun processInput()

    /**
     * @see OutputGenerator.generate
     */
    abstract fun generateOutput()

    /**
     * @see NextSkills.nextSkills
     */
    override fun nextSkills(): List<Skill> {
        val skills = nextSkills
        nextSkills = listOf()
        return skills
    }

    /**
     * @see NextSkills.setNextSkills
     */
    override fun setNextSkills(skills: List<Skill>) {
        nextSkills = skills
    }

    /**
     * Resets the last list of next skills passed to [.setNextSkills] to an empty list.
     * Remember to call super if you override.
     * @see CleanableUp.cleanup
     */
    @CallSuper
    override fun cleanup() {
        nextSkills = listOf()
    }
}
