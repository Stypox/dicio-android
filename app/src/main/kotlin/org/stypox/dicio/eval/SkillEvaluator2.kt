package org.stypox.dicio.eval

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dicio.skill.SkillContext
import org.dicio.skill.output.SkillOutput
import org.dicio.skill.util.WordExtractor
import org.stypox.dicio.io.graphical.ErrorSkillOutput
import org.stypox.dicio.io.graphical.MissingPermissionsSkillOutput
import org.stypox.dicio.io.input.InputEvent
import org.stypox.dicio.io.input.InputEventsModule
import org.stypox.dicio.skills.SkillHandler
import org.stypox.dicio.ui.main.Interaction
import org.stypox.dicio.ui.main.InteractionLog
import org.stypox.dicio.ui.main.PendingQuestion
import javax.inject.Inject

class SkillEvaluator2 @Inject constructor(
    private val inputEventsModule: InputEventsModule,
    private val skillContext: SkillContext,
    private val requestPermissions: suspend (Array<String>) -> Boolean,
) {

    private val scope = CoroutineScope(Dispatchers.Default)

    private val _state = MutableStateFlow(
        InteractionLog(
            interactions = listOf(),
            pendingQuestion = null,
        )
    )
    val state: StateFlow<InteractionLog> = _state

    private val skillRanker: SkillRanker = SkillRanker(
        SkillHandler.standardSkillBatch,
        SkillHandler.fallbackSkill,
    )

    init {
        scope.launch {
            // receive input events
            inputEventsModule.events.collect(::processInputEvent)
        }
    }

    private suspend fun processInputEvent(event: InputEvent) {
        when (event) {
            is InputEvent.Error -> {
                addErrorInteractionFromPending(event.throwable)
            }
            is InputEvent.Final -> {
                _state.value = _state.value.copy(
                    pendingQuestion = PendingQuestion(
                        userInput = event.utterances[0],
                        continuesLastInteraction = skillRanker.hasAnyBatches(),
                        skillBeingEvaluated = null,
                    )
                )
                evaluateMatchingSkill(event.utterances)
            }
            InputEvent.None -> {
                _state.value = _state.value.copy(pendingQuestion = null)
            }
            is InputEvent.Partial -> {
                _state.value = _state.value.copy(
                    pendingQuestion = PendingQuestion(
                        userInput = event.utterance,
                        continuesLastInteraction = skillRanker.hasAnyBatches(),
                        skillBeingEvaluated = null,
                    )
                )
            }
        }
    }

    private suspend fun evaluateMatchingSkill(utterances: List<String>) {
        val (chosenInput, chosenSkill) = try {
            utterances.firstNotNullOfOrNull { input: String ->
                val inputWords = WordExtractor.extractWords(input)
                val normalizedWords = WordExtractor.normalizeWords(inputWords)
                skillRanker.getBest(input, inputWords, normalizedWords)?.let { Pair(input, it) }
            } ?: run {
                val inputWords = WordExtractor.extractWords(utterances[0])
                val normalizedWords = WordExtractor.normalizeWords(inputWords)
                Pair(
                    utterances[0],
                    skillRanker.getFallbackSkill(utterances[0], inputWords, normalizedWords)
                )
            }
        } catch (throwable: Throwable) {
            addErrorInteractionFromPending(throwable)
            return
        }
        val skillInfo = chosenSkill.correspondingSkillInfo

        val prevLog = _state.value
        val continuesLastInteraction = prevLog.pendingQuestion?.continuesLastInteraction == true &&
                prevLog.interactions.lastOrNull()?.skill === skillInfo
        _state.value = prevLog.copy(
            pendingQuestion = PendingQuestion(
                userInput = chosenInput,
                continuesLastInteraction = continuesLastInteraction,
                skillBeingEvaluated = chosenSkill.correspondingSkillInfo,
            )
        )

        try {
            val permissions = skillInfo.neededPermissions.toTypedArray()
            if (!requestPermissions(permissions)) {
                // permissions were not granted, show message
                addInteractionFromPending(MissingPermissionsSkillOutput(skillInfo))
                return
            }

            chosenSkill.processInput()
            val output = withContext (Dispatchers.Main) {
                // call generateOutput() on the main thread
                chosenSkill.generateOutput()
            }

            addInteractionFromPending(output)
            // TODO speak speech result

            val nextSkills = output.getNextSkills(skillContext)
            if (nextSkills.isEmpty()) {
                // current conversation has ended, reset to the default batch of skills
                skillRanker.removeAllBatches()
            } else {
                skillRanker.addBatchToTop(nextSkills)
                // TODO try to get input
            }

        } catch (throwable: Throwable) {
            addErrorInteractionFromPending(throwable)
            return
        } finally {
            chosenSkill.cleanup()
        }
    }

    private fun addErrorInteractionFromPending(throwable: Throwable) {
        addInteractionFromPending(ErrorSkillOutput(throwable))
    }

    private fun addInteractionFromPending(skillOutput: SkillOutput) {
        val log = _state.value
        val pendingUserInput = log.pendingQuestion?.userInput ?: ""
        val pendingContinuesLastInteraction = log.pendingQuestion?.continuesLastInteraction
            ?: skillRanker.hasAnyBatches()
        val pendingSkill = log.pendingQuestion?.skillBeingEvaluated
        val questionAnswer = Pair(pendingUserInput, skillOutput)

        _state.value = log.copy(
            interactions = log.interactions.toMutableList().also { inters ->
                if (pendingContinuesLastInteraction && inters.isNotEmpty()) {
                    inters[inters.size - 1] = inters[inters.size - 1].let { i -> i.copy(
                        questionsAnswers = i.questionsAnswers.toMutableList()
                            .apply { add(questionAnswer) }
                    ) }
                } else {
                    inters.add(
                        Interaction(
                            skill = pendingSkill,
                            questionsAnswers = listOf(questionAnswer)
                        )
                    )
                }
            },
            pendingQuestion = null,
        )
    }
}
