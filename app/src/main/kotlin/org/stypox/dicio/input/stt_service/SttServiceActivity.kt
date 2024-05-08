package org.stypox.dicio.input.stt_service

import android.app.PendingIntent
import android.app.PendingIntent.CanceledException
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDialog
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.stypox.dicio.R
import org.stypox.dicio.databinding.DialogSttServiceBinding
import org.stypox.dicio.error.ErrorInfo
import org.stypox.dicio.error.ErrorUtils
import org.stypox.dicio.error.UserAction
import org.stypox.dicio.input.InputDevice
import org.stypox.dicio.input.SpeechInputDevice
import org.stypox.dicio.input.VoskInputDevice
import org.stypox.dicio.util.BaseActivity
import org.stypox.dicio.util.ShareUtils
import org.stypox.dicio.util.ShareUtils.shareText
import org.stypox.dicio.util.ThemeUtils

class SttServiceActivity : BaseActivity() {
    private var dialog: AppCompatDialog? = null
    private var speechInputDevice: SpeechInputDevice? = null
    private var binding: DialogSttServiceBinding? = null
    private var startedForSpeechResult = false
    private var speechExtras: Bundle? = null
    private var userInputHint: String? = null

    override val themeFromPreferences: Int
        get() = ThemeUtils.chooseThemeBasedOnPreferences(
            this,
            R.style.SttServiceLightAppTheme, R.style.SttServiceLightAppTheme
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        if (intent != null && RecognizerIntent.ACTION_RECOGNIZE_SPEECH == intent.action) {
            startedForSpeechResult = true
            speechExtras = intent.extras
            speechExtras?.also { bundle ->
                userInputHint = bundle.getString(
                    RecognizerIntent.EXTRA_PROMPT,
                    getString(R.string.stt_say_something)
                )
            }
        }
        if (userInputHint == null) {
            userInputHint = getString(R.string.stt_say_something)
        }
    }

    override fun onStart() {
        super.onStart()
        val wrappedContext: Context = ContextThemeWrapper(
            this,
            ThemeUtils.chooseThemeBasedOnPreferences(
                this,
                R.style.LightAppTheme, R.style.DarkAppTheme
            )
        )

        val layoutInflater = LayoutInflater.from(wrappedContext)
        binding = DialogSttServiceBinding.inflate(layoutInflater).apply {
            userInput.hint = userInputHint
        }.also {
            dialog = BottomSheetDialog(wrappedContext).apply {
                setCancelable(true)
                setOnDismissListener { finish() }
                setContentView(it.root)
                create()
                show()
            }
        }

        setupSpeechInputDevice()
        setupButtons()
    }

    override fun onStop() {
        super.onStop()
        dialog?.apply {
            dismiss()
            dialog = null
        }
        binding = null
        speechInputDevice?.cleanup()
    }

    private fun setupSpeechInputDevice() {
        //TODO Extras which may be also useful for speech recognition, sorted by priority:
        // EXTRA_LANGUAGE_MODEL (abel with vosk?), EXTRA_BIASING_STRINGS, EXTRA_LANGUAGE,
        // EXTRA_AUDIO_SOURCE, DETAILS_META_DATA(?)
        speechInputDevice = VoskInputDevice(this).apply {
            binding?.also { setVoiceViews(it.voiceFab, it.voiceLoading) }
            tryToGetInput(false)

            setInputDeviceListener(object : InputDevice.InputDeviceListener {
                override fun onTryingToGetInput() {
                    binding?.userInput?.hint = userInputHint
                    binding?.userInput?.isEnabled = false
                }

                override fun onPartialInputReceived(input: String) {
                    showUserInput(input)
                }

                override fun onInputReceived(input: List<String>) {
                    showUserInput(input[0])
                    binding?.userInput?.isEnabled = true
                    if (startedForSpeechResult && PreferenceManager
                            .getDefaultSharedPreferences(this@SttServiceActivity)
                            .getBoolean(getString(R.string.pref_key_stt_auto_finish), true)
                    ) {
                        // only send the speech result directly if the user allows it
                        sendSpeechResult()
                        dialog?.dismiss()
                    }
                }

                override fun onNoInputReceived() {
                    binding?.userInput?.setHint(R.string.stt_did_not_understand)
                    binding?.userInput?.isEnabled = true
                }

                override fun onError(e: Throwable) {
                    ErrorUtils.createNotification(
                        this@SttServiceActivity,
                        ErrorInfo(e, UserAction.STT_SERVICE_SPEECH_TO_TEXT)
                    )
                    binding?.userInput?.isEnabled = true
                }
            })
        }
    }

    private fun setupButtons() {
        val binding = binding ?: return
        binding.copyButton.setOnClickListener {
            ShareUtils.copyToClipboard(this, userInput)
            dialog?.dismiss()
        }
        val context = binding.root.context
        if (startedForSpeechResult) {
            binding.doneOrShareButton.setImageResource(
                ThemeUtils.resolveResourceIdFromAttr(context, R.attr.iconDone)
            )
            binding.doneOrShareButton.setOnClickListener {
                sendSpeechResult()
                dialog?.dismiss()
            }
        } else {
            binding.doneOrShareButton.setImageResource(
                ThemeUtils.resolveResourceIdFromAttr(context, R.attr.iconShare)
            )
            binding.doneOrShareButton.setOnClickListener {
                shareText(this, "", userInput)
                dialog?.dismiss()
            }
        }
    }

    private fun showUserInput(userInput: String) {
        binding?.userInput?.setText(userInput.trim { it <= ' ' })
    }

    private val userInput: String
        get() = binding?.userInput?.text?.toString() ?: ""

    fun sendSpeechResult() {
        // get results from recognizer and prepare for reporting
        val foundTexts = ArrayList<String>()
        foundTexts.add(userInput)

        // Because there is currently just one result, it gets 1.0 TODO it is possible to get more
        // results + confidence from vosk. When extending number of results,
        // implement EXTRA_MAX_RESULTS
        val confidenceScore = ArrayList<Float>()
        confidenceScore.add(1.0f)

        // Prepare Result Intent with Extras
        val intent = Intent()
        intent.putExtra(RecognizerIntent.EXTRA_RESULTS, foundTexts)
        intent.putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, confidenceScore)

        // This is for some apps, who use the Android SearchManager (e.g. ebay)
        // (in my eyes probably wrong implemented by them, however without it's not working...)
        intent.putExtra(SearchManager.QUERY, userInput)
        setResult(RESULT_OK, intent)

        val speechExtras = speechExtras ?: return
        if (speechExtras.containsKey(RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT)) {
            if (speechExtras.containsKey(RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT_BUNDLE)) {
                intent.extras?.putAll(speechExtras.getBundle(
                    RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT_BUNDLE
                ))
            }
            val resultIntent = speechExtras.getParcelable<PendingIntent>(
                RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT
            )
            try {
                resultIntent?.send(this, RESULT_OK, intent)
            } catch (e: CanceledException) {
                Log.w(TAG, "Speech result pending intent canceled", e)
            }
        }
    }

    companion object {
        private val TAG = SttServiceActivity::class.java.simpleName
    }
}