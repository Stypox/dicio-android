package org.stypox.dicio.eval

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.dicio.skill.Skill
import org.dicio.skill.output.GraphicalOutputDevice
import org.dicio.skill.output.SpeechOutputDevice
import org.dicio.skill.util.CleanableUp
import org.dicio.skill.util.WordExtractor
import org.stypox.dicio.MainActivity
import org.stypox.dicio.R
import org.stypox.dicio.error.ExceptionUtils
import org.stypox.dicio.input.InputDevice
import org.stypox.dicio.input.SpeechInputDevice.UnableToAccessMicrophoneException
import org.stypox.dicio.input.ToolbarInputDevice
import org.stypox.dicio.output.graphical.GraphicalOutputUtils
import org.stypox.dicio.skills.SkillHandler
import org.stypox.dicio.util.PermissionUtils
import java.util.LinkedList
import java.util.Queue

class SkillEvaluator(
    private val skillRanker: SkillRanker,
    val primaryInputDevice: InputDevice,
    val secondaryInputDevice: ToolbarInputDevice?,
    private val speechOutputDevice: SpeechOutputDevice,
    private val graphicalOutputDevice: GraphicalOutputDevice,
    private var activity: Activity
) : CleanableUp {

    private var currentlyProcessingInput = false
    private val queuedInputs: Queue<List<String>> = LinkedList()
    private var partialInputView: View? = null
    private var hasAddedPartialInputView = false
    private var evaluationDisposable: Disposable? = null
    private var skillNeedingPermissions: Skill? = null

    init {
        setupInputDeviceListeners()
        setupOutputDevices()
    }

    ////////////////////
    // Public methods //
    ////////////////////
    fun showInitialPanel() {
        val initialPanel = GraphicalOutputUtils.inflate(activity, R.layout.initial_panel)
        val skillItemsLayout = initialPanel.findViewById<LinearLayout>(R.id.skillItemsLayout)
        for (skillInfo in SkillHandler.enabledSkillInfoListShuffled) {
            val skillInfoItem =
                GraphicalOutputUtils.inflate(activity, R.layout.initial_panel_skill_item)
            (skillInfoItem.findViewById<View>(R.id.skillIconImageView) as AppCompatImageView)
                .setImageResource(SkillHandler.getSkillIconResource(skillInfo))
            (skillInfoItem.findViewById<View>(R.id.skillName) as AppCompatTextView)
                .setText(skillInfo.nameResource)
            (skillInfoItem.findViewById<View>(R.id.skillSentenceExample) as AppCompatTextView)
                .setText(skillInfo.sentenceExampleResource)
            skillItemsLayout.addView(skillInfoItem)
        }
        graphicalOutputDevice.displayTemporary(initialPanel)
    }

    override fun cleanup() {
        cancelGettingInput()
        skillRanker.cleanup()
        primaryInputDevice.cleanup()
        secondaryInputDevice?.cleanup()
        speechOutputDevice.cleanup()
        graphicalOutputDevice.cleanup()
        evaluationDisposable?.dispose()
        skillNeedingPermissions = null
        queuedInputs.clear()
        partialInputView = null
    }

    fun cancelGettingInput() {
        primaryInputDevice.cancelGettingInput()
        secondaryInputDevice?.cancelGettingInput()
    }

    /**
     * to be called from the main thread
     */
    fun onSkillRequestPermissionsResult(grantResults: IntArray) {
        val skill: Skill = skillNeedingPermissions ?: return // return should be unreachable
        // make sure this skill is not reprocessed (also should never happen, but who knows)
        skillNeedingPermissions = null
        if (PermissionUtils.areAllPermissionsGranted(*grantResults)) {
            evaluationDisposable = Single.fromCallable {
                skill.processInput()
                skill
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::generateOutput, ::onError)
        } else {
            // permissions were not granted, show a message
            val skillInfo = skill.skillInfo
            if (skillInfo != null) {
                // skill info will always be non-null, but stay on the safe side and add a check
                val message = activity.getString(
                    R.string.eval_missing_permissions,
                    activity.getString(skillInfo.nameResource),
                    PermissionUtils.getCommaJoinedPermissions(activity, skillInfo)
                )
                speechOutputDevice.speak(message)
                graphicalOutputDevice.display(
                    GraphicalOutputUtils.buildDescription(activity, message)
                )
            }
            graphicalOutputDevice.addDivider()
            finishedProcessingInput()
        }
    }

    ///////////
    // Setup //
    ///////////
    private fun setupInputDeviceListeners() {
        primaryInputDevice.setInputDeviceListener(object : InputDevice.InputDeviceListener {
            override fun onTryingToGetInput() {
                speechOutputDevice.stopSpeaking()
                secondaryInputDevice?.cancelGettingInput()
            }

            override fun onPartialInputReceived(input: String) {
                displayPartialUserInput(input)
            }

            override fun onInputReceived(input: List<String>) {
                processInput(input)
            }

            override fun onNoInputReceived() {
                handleNoInput()
            }

            override fun onError(e: Throwable) {
                this@SkillEvaluator.onError(e)
            }
        })

        secondaryInputDevice?.setInputDeviceListener(object : InputDevice.InputDeviceListener {
            override fun onTryingToGetInput() {
                speechOutputDevice.stopSpeaking()
                primaryInputDevice.cancelGettingInput()
            }

            override fun onPartialInputReceived(input: String) {
                displayPartialUserInput(input)
            }

            override fun onInputReceived(input: List<String>) {
                processInput(input)
            }

            override fun onNoInputReceived() {
                handleNoInput()
            }

            override fun onError(e: Throwable) {
                this@SkillEvaluator.onError(e)
            }
        })
    }

    private fun setupOutputDevices() {
        // this adds a divider only if there are already some output views (can happen when
        // reloading the skill evaluator)
        graphicalOutputDevice.addDivider()
    }

    /////////////////////////////////////////
    // Partial input and no input handling //
    /////////////////////////////////////////
    private fun displayPartialUserInput(input: String) {
        hasAddedPartialInputView = true
        val partialInputView = partialInputView ?:
            GraphicalOutputUtils.inflate(activity, R.layout.user_input_partial)
                .also { partialInputView = it }

        val textView = partialInputView.findViewById<TextView>(R.id.userInput)
        textView.text = input
        graphicalOutputDevice.displayTemporary(partialInputView)
    }

    private fun handleNoInput() {
        if (hasAddedPartialInputView) {
            // remove temporary partial input view: no input was provided
            graphicalOutputDevice.removeTemporary()
            hasAddedPartialInputView = false
        }
    }

    //////////////////////
    // Input processing //
    //////////////////////
    private fun processInput(input: List<String>) {
        hasAddedPartialInputView = false
        queuedInputs.add(input)
        tryToProcessQueuedInput()
    }

    private fun finishedProcessingInput() {
        queuedInputs.poll() // current input has finished processing
        currentlyProcessingInput = false
        tryToProcessQueuedInput() // try to process next input, if present
    }

    private fun tryToProcessQueuedInput() {
        if (currentlyProcessingInput) {
            return
        }
        queuedInputs.peek()?.let {
            currentlyProcessingInput = true
            evaluateMatchingSkill(it)
        }
    }

    private fun displayUserInput(input: String) {
        val userInputView = GraphicalOutputUtils.inflate(activity, R.layout.user_input)
        val inputEditText = userInputView.findViewById<AppCompatEditText>(R.id.userInput)
        inputEditText.setText(input)
        inputEditText.addTextChangedListener(object : TextWatcher {
            // `count` characters beginning at `start` replaced old text that had length `before`
            var startIndex = 0
            var countBefore = 0
            var countAfter = 0
            var ignore = false
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (ignore) {
                    return
                }
                startIndex = start
                countBefore = before
                countAfter = count
            }

            override fun afterTextChanged(s: Editable) {
                if (ignore) {
                    return
                }
                if (countBefore == 0 && countAfter == 1) {
                    // a single character has just been inserted, most probably from the keyboard,
                    // so check if it is the Enter key (i.e. the newline '\n')
                    if (s.length > startIndex && s[startIndex] == '\n') {
                        s.delete(startIndex, startIndex + 1)
                        if (s.toString().trim { it <= ' ' } != input.trim { it <= ' ' }) {
                            processInput(listOf(s.toString()))
                            inputEditText.setText(input) // restore original input
                            inputEditText.clearFocus() // prevent focus problems
                        }
                    }
                } else {
                    // text was copy-pasted, so replace all newlines with spaces
                    if (s.length < startIndex + countAfter) {
                        return  // should be impossible, but just to be sure
                    }

                    // do all at once for performance and otherwise `s.replace` indices get messed
                    val chars = CharArray(countAfter)
                    s.getChars(startIndex, startIndex + countAfter, chars, 0)
                    for (i in 0 until countAfter) {
                        if (chars[i] == '\n') {
                            chars[i] = ' '
                        }
                    }
                    ignore = true // ignore the calls made to this listener by `s.replace`
                    s.replace(startIndex, startIndex + countAfter, String(chars))
                    ignore = false
                }
            }
        })
        userInputView.setOnClickListener {
            // focus test pointer after last character
            inputEditText.requestFocus()
            val text = inputEditText.text
            inputEditText.setSelection(text?.length ?: 0)

            // open keyboard
            val inputMethodManager =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(inputEditText, InputMethodManager.SHOW_FORCED)
        }
        graphicalOutputDevice.display(userInputView)
    }

    //////////////////////
    // Skill evaluation //
    //////////////////////
    private class InputSkillPair constructor(val input: String, val skill: Skill) {
        var permissionsToRequest: Array<String>? = null
    }
    private class SkillProcessInputError(val choseInputSkill: InputSkillPair, cause: Throwable)
        : Exception(cause)

    private fun evaluateMatchingSkill(inputs: List<String>) {
        evaluationDisposable?.dispose()
        evaluationDisposable = Single.fromCallable {
            val chosen: InputSkillPair =
                inputs.firstNotNullOfOrNull { input: String ->
                    val inputWords = WordExtractor.extractWords(input)
                    val normalizedWords = WordExtractor.normalizeWords(inputWords)
                    skillRanker.getBest(input, inputWords, normalizedWords)
                        ?.let { InputSkillPair(input, it) }
                } ?: run {
                    val inputWords = WordExtractor.extractWords(inputs[0])
                    val normalizedWords = WordExtractor.normalizeWords(inputWords)
                    InputSkillPair(
                        inputs[0],
                        skillRanker.getFallbackSkill(inputs[0], inputWords, normalizedWords)
                    )
                }

            try {
                val permissions = chosen.skill.skillInfo?.neededPermissions?.toTypedArray() ?: arrayOf()
                if (PermissionUtils.checkPermissions(activity, *permissions)) {
                    // skill's output will be generated below, so process input now
                    chosen.skill.processInput()
                } else {
                    // before executing this skill needs some permissions, don't process input now
                    chosen.permissionsToRequest = permissions
                }
            } catch (t: Throwable) {
                // this allows displaying the error on the main thread in onError
                throw SkillProcessInputError(chosen, t)
            }

            chosen
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::onChosenSkill, ::onError)
    }

    @UiThread
    private fun onChosenSkill(chosen: InputSkillPair) {
        displayUserInput(chosen.input)
        chosen.permissionsToRequest?.also { permissionsToRequest ->
            // request permissions; when done process input in onSkillRequestPermissionsResult
            // note: need to do this here on main thread
            ActivityCompat.requestPermissions(
                activity, permissionsToRequest, MainActivity.SKILL_PERMISSIONS_REQUEST_CODE
            )
            skillNeedingPermissions = chosen.skill
        } ?: generateOutput(chosen.skill)
    }

    @UiThread
    private fun generateOutput(skill: Skill) {
        val nextSkills: List<Skill>?
        try {
            skill.generateOutput()
            nextSkills = skill.nextSkills()
            skill.cleanup() // cleanup the input that was set
        } catch (t: Throwable) {
            onError(t)
            return
        }

        if (nextSkills.isNullOrEmpty()) {
            // current conversation has ended, reset to the default batch of skills
            skillRanker.removeAllBatches()
            graphicalOutputDevice.addDivider()
        } else {
            skillRanker.addBatchToTop(nextSkills)
            speechOutputDevice.runWhenFinishedSpeaking {
                activity.runOnUiThread { primaryInputDevice.tryToGetInput(false) }
            }
        }

        finishedProcessingInput()
    }

    ///////////
    // Error //
    ///////////
    private fun onError(wrappedThrowable: Throwable) {
        wrappedThrowable.printStackTrace()

        val t = if (wrappedThrowable is SkillProcessInputError) {
            displayUserInput(wrappedThrowable.choseInputSkill.input)
            wrappedThrowable.cause!!
        } else {
            wrappedThrowable
        }

        if (ExceptionUtils.hasAssignableCause(t, UnableToAccessMicrophoneException::class.java)) {
            val message = activity.getString(R.string.microphone_error)
            speechOutputDevice.speak(message)
            graphicalOutputDevice.display(
                GraphicalOutputUtils.buildDescription(activity, message)
            )
        } else if (ExceptionUtils.isNetworkError(t)) {
            speechOutputDevice.speak(activity.getString(R.string.eval_network_error_description))
            graphicalOutputDevice.display(GraphicalOutputUtils.buildNetworkErrorMessage(activity))
        } else {
            skillRanker.removeAllBatches()
            speechOutputDevice.speak(activity.getString(R.string.eval_fatal_error))
            graphicalOutputDevice.display(GraphicalOutputUtils.buildErrorMessage(activity, t))
        }
        graphicalOutputDevice.addDivider()
        finishedProcessingInput()
    }
}