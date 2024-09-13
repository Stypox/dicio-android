package org.stypox.dicio.io.wake.oww

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.stypox.dicio.io.wake.WakeDevice
import org.stypox.dicio.io.wake.WakeState
import org.stypox.dicio.util.downloadBinaryFileWithPartial
import org.stypox.dicio.util.getResponse
import java.io.File
import java.io.IOException

class OpenWakeWordDevice(
    @ApplicationContext appContext: Context,
    private val okHttpClient: OkHttpClient,
) : WakeDevice {
    private val _state: MutableStateFlow<WakeState>
    override val state: StateFlow<WakeState>

    private val cacheDir: File = appContext.cacheDir
    private val owwFolder = File(appContext.filesDir, "openWakeWord")
    private val melFile = File(owwFolder, "melspectrogram.tflite")
    private val embFile = File(owwFolder, "embedding.tflite")
    private val wakeFile = File(owwFolder, "wake.tflite")

    private val audio = FloatArray(OwwModel.MEL_INPUT_COUNT)
    private var model: OwwModel? = null

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        _state = if (melFile.exists() && embFile.exists() && wakeFile.exists()) {
            MutableStateFlow(WakeState.Loading)
        } else {
            MutableStateFlow(WakeState.NotDownloaded)
        }
        state = _state
    }

    override fun download() {
        _state.value = WakeState.Downloading(0, 0)

        scope.launch {
            try {
                owwFolder.mkdirs()
                downloadModelFileIfNeeded(melFile, MEL_URL)
                downloadModelFileIfNeeded(embFile, EMB_URL)
                downloadModelFileIfNeeded(wakeFile, WAKE_URL)
            } catch (e: Throwable) {
                Log.e(TAG, "Can't download OpenWakeWord model", e)
                _state.value = WakeState.ErrorDownloading(e)
                return@launch
            }

            _state.value = WakeState.NotLoaded
        }
    }

    private suspend fun downloadModelFileIfNeeded(file: File, url: String) {
        if (file.exists()) {
            return
        }

        downloadBinaryFileWithPartial(
            response = okHttpClient.getResponse(url),
            file = file,
            cacheDir = cacheDir,
        ) { currentBytes, totalBytes ->
            _state.value = WakeState.Downloading(currentBytes, totalBytes)
        }
    }

    override fun processFrame(audio16bitPcm: ShortArray): Float {
        if (audio16bitPcm.size != OwwModel.MEL_INPUT_COUNT) {
            throw IllegalArgumentException(
                "OwwModel can only process audio frames of ${OwwModel.MEL_INPUT_COUNT} samples"
            )
        }

        if (model == null) {
            if (_state.value != WakeState.NotLoaded) {
                throw IOException("Model has not been downloaded yet")
            }

            try {
                _state.value = WakeState.Loading
                model = OwwModel(melFile, embFile, wakeFile)
                _state.value = WakeState.Loaded
            } catch (t: Throwable) {
                _state.value = WakeState.ErrorLoading(t)
                throw t
            }
        }

        for (i in 0..<OwwModel.MEL_INPUT_COUNT) {
            audio[i] = audio16bitPcm[i].toFloat() / 32768.0f
        }

        return model!!.processFrame(audio)
    }

    override fun frameSize(): Int {
        return OwwModel.MEL_INPUT_COUNT
    }

    override fun destroy() {
        model?.close()
        model = null
        scope.cancel()
    }

    companion object {
        val TAG = OpenWakeWordDevice::class.simpleName
        const val MEL_URL = "https://github.com/dscripka/openWakeWord/releases/download/v0.5.1/melspectrogram.tflite"
        const val EMB_URL = "https://github.com/dscripka/openWakeWord/releases/download/v0.5.1/embedding_model.tflite"
        const val WAKE_URL = "https://github.com/dscripka/openWakeWord/releases/download/v0.5.1/hey_mycroft_v0.1.tflite"
    }
}
