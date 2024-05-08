package org.dicio.skill.chain

import org.dicio.skill.Skill
import org.dicio.skill.SkillContext
import org.dicio.skill.SkillInfo
import org.dicio.skill.chain.InputRecognizer.Specificity

@Suppress("UNCHECKED_CAST")
class ChainSkill(
    private val inputRecognizer: InputRecognizer<*>,
    private val intermediateProcessors: List<IntermediateProcessor<*, *>>,
    private val outputGenerator: OutputGenerator<*>,
) : Skill() {
    class Builder(
        private val inputRecognizer: InputRecognizer<*>
    ) {
        private val intermediateProcessors: MutableList<IntermediateProcessor<*, *>> =
            ArrayList()

        fun process(intermediateProcessor: IntermediateProcessor<*, *>): Builder {
            this.intermediateProcessors.add(intermediateProcessor)
            return this
        }

        fun output(outputGenerator: OutputGenerator<*>): ChainSkill {
            return ChainSkill(inputRecognizer, intermediateProcessors, outputGenerator)
        }
    }


    private var lastResult: Any? = null


    override fun specificity(): Specificity {
        return inputRecognizer.specificity()
    }

    override fun setInput(
        input: String,
        inputWords: List<String>,
        normalizedWordKeys: List<String>
    ) {
        inputRecognizer.setInput(input, inputWords, normalizedWordKeys)
    }

    override fun score(): Float {
        return inputRecognizer.score()
    }

    override fun cleanup() {
        super.cleanup()
        inputRecognizer.cleanup()
        outputGenerator.cleanup()
        lastResult = null
    }


    @Throws(Exception::class)
    override fun processInput() {
        lastResult = inputRecognizer.result
        for (intermediateProcessor in intermediateProcessors) {
            lastResult = (intermediateProcessor as IntermediateProcessor<Any?, Any?>)
                .process(lastResult)
        }
    }

    override fun generateOutput() {
        (outputGenerator as OutputGenerator<Any?>).generate(lastResult)
    }

    /**
     * @see OutputGenerator.nextSkills
     */
    override fun nextSkills(): List<Skill> {
        return outputGenerator.nextSkills()
    }

    /**
     * Also sets the context to all of this chain skill sub-components
     * @param context the [SkillContext] object this [Skill] is being created with
     */
    override fun setContext(context: SkillContext) {
        super.setContext(context)
        inputRecognizer.setContext(context)
        for (intermediateProcessor in intermediateProcessors) {
            intermediateProcessor.setContext(context)
        }
        outputGenerator.setContext(context)
    }

    override var skillInfo: SkillInfo?
        get() = super.skillInfo
        /**
         * Also sets the skill info to all of this chain skill sub-components
         * @param skillInfo the [SkillInfo] object this [Skill] is being created with (using
         * [SkillInfo.build]), or `null` if this skill is
         * not being built by a [SkillInfo]
         */
        set(skillInfo) {
            super.skillInfo = skillInfo
            inputRecognizer.skillInfo = skillInfo
            for (intermediateProcessor in intermediateProcessors) {
                intermediateProcessor.skillInfo = skillInfo
            }
            outputGenerator.skillInfo = skillInfo
        }
}
