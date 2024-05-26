package org.stypox.dicio.io.input.stt_service

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.stypox.dicio.di.SttInputDeviceWrapper
import org.stypox.dicio.io.input.InputEvent
import org.stypox.dicio.io.input.SttInputDevice
import javax.inject.Inject

@HiltViewModel
class SttServiceViewModel @Inject constructor(
    application: Application,
    val sttInputDevice: SttInputDeviceWrapper,
) : AndroidViewModel(application) {

    private val _textFieldValue = MutableStateFlow("")
    val textFieldValue: StateFlow<String> = _textFieldValue

    private var lastFinalUtterances: List<Pair<String, Float>> = listOf()
    private var lastTextFieldInput = ""


    init {
        // start listening right away when the SttServiceActivity is started
        sttInputDevice.tryLoad(this::onReceiveInputEvent)
    }


    fun setTextFieldValue(value: String) {
        lastTextFieldInput = value
        _textFieldValue.value = value
        if (lastFinalUtterances.isNotEmpty() &&
            lastFinalUtterances[0].first.trim() != value.trim()) {
            // the user manually set the text field value to something else,
            // so ditch all utterance alternatives and keep only what the user said
            lastFinalUtterances = listOf()
        }
    }

    fun getUtterancesWithConfidence(): List<Pair<String, Float>> {
        return lastFinalUtterances.ifEmpty {
            // the user inserted this value manually, so confidence = 1.0
            listOf(Pair(lastTextFieldInput, 1.0f))
        }
    }

    fun onSttClick() {
        sttInputDevice.onClick(this::onReceiveInputEvent)
    }

    private fun onReceiveInputEvent(inputEvent: InputEvent) {
        when (inputEvent) {
            is InputEvent.Partial -> {
                // the text field will show the partial value, but the previous value will be kept
                _textFieldValue.value = inputEvent.utterance
            }
            is InputEvent.Error,
            InputEvent.None -> {
                // in case of error or in case the user said nothing, restore the previous value
                _textFieldValue.value = lastTextFieldInput
            }
            is InputEvent.Final -> {
                // receiving a final event from the STT behaves almost identically as receiving a
                // new value in setTextFieldValue when the user types manually
                lastTextFieldInput = inputEvent.utterances[0].first
                _textFieldValue.value = inputEvent.utterances[0].first
                lastFinalUtterances = inputEvent.utterances
            }
        }
    }
}
