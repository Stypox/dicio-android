package org.dicio.skill.chain

import org.dicio.skill.Skill
import org.dicio.skill.SkillContext
import org.dicio.skill.SkillInfo
import org.dicio.skill.output.SkillOutput

@Suppress("UNCHECKED_CAST")
class ChainSkill(
    correspondingSkillInfo: SkillInfo,
    private val inputRecognizer: InputRecognizer<*>,
    private val intermediateProcessors: List<IntermediateProcessor<*, *>>,
    private val outputGenerator: OutputGenerator<*>,
) : Skill(correspondingSkillInfo, inputRecognizer.specificity) {
    class Builder(
        private val correspondingSkillInfo: SkillInfo,
        private val inputRecognizer: InputRecognizer<*>
    ) {
        private val intermediateProcessors: MutableList<IntermediateProcessor<*, *>> =
            ArrayList()

        fun process(intermediateProcessor: IntermediateProcessor<*, *>): Builder {
            this.intermediateProcessors.add(intermediateProcessor)
            return this
        }

        fun output(outputGenerator: OutputGenerator<*>): ChainSkill {
            return ChainSkill(
                correspondingSkillInfo,
                inputRecognizer,
                intermediateProcessors,
                outputGenerator,
            )
        }
    }


    private var lastResult: Any? = null


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

    override fun generateOutput(): SkillOutput {
        return (outputGenerator as OutputGenerator<Any?>).generate(lastResult)
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
}
