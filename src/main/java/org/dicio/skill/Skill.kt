package org.dicio.skill

import androidx.annotation.CallSuper
import org.dicio.skill.chain.InputRecognizer
import org.dicio.skill.chain.InputRecognizer.Specificity
import org.dicio.skill.chain.IntermediateProcessor
import org.dicio.skill.chain.OutputGenerator
import org.dicio.skill.output.SkillOutput
import org.dicio.skill.util.CleanableUp

/**
 * A skill is the component that scores input, processes it and finally generates output. Take a
 * look at [org.dicio.skill.chain.ChainSkill] for a class that separates these three things.
 */
abstract class Skill : SkillComponent(), CleanableUp {
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
    abstract fun generateOutput(): SkillOutput

    /**
     * @see CleanableUp.cleanup
     */
    @CallSuper
    override fun cleanup() {}
}
