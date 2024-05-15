/*
 * Taken from /e/OS Assistant
 *
 * Copyright (C) 2024 MURENA SAS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.stypox.dicio.io.input.vosk

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import org.stypox.dicio.io.input.vosk.VoskState.Downloaded
import org.stypox.dicio.io.input.vosk.VoskState.Downloading
import org.stypox.dicio.io.input.vosk.VoskState.ErrorDownloading
import org.stypox.dicio.io.input.vosk.VoskState.ErrorLoading
import org.stypox.dicio.io.input.vosk.VoskState.ErrorUnzipping
import org.stypox.dicio.io.input.vosk.VoskState.Listening
import org.stypox.dicio.io.input.vosk.VoskState.Loaded
import org.stypox.dicio.io.input.vosk.VoskState.Loading
import org.stypox.dicio.io.input.vosk.VoskState.NotDownloaded
import org.stypox.dicio.io.input.vosk.VoskState.NotLoaded
import org.stypox.dicio.io.input.vosk.VoskState.Unzipping
import org.stypox.dicio.util.downloadBinaryFileWithPartial
import org.stypox.dicio.util.getDestinationFile
import org.stypox.dicio.util.useEntries
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.stypox.dicio.io.input.InputEvent
import org.stypox.dicio.io.input.InputEventsModule
import org.stypox.dicio.io.input.SttInputDevice
import org.stypox.dicio.ui.main.SttState
import org.vosk.BuildConfig
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.SpeechService
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoskInputDevice @Inject constructor(
    @ApplicationContext appContext: Context,
    private val okHttpClient: OkHttpClient,
    private val inputEventsModule: InputEventsModule,
) : SttInputDevice {

    private val _state: MutableStateFlow<VoskState>
    private val _uiState: MutableStateFlow<SttState>
    override val uiState: StateFlow<SttState>

    private val scope = CoroutineScope(Dispatchers.Default)

    private val filesDir: File = appContext.filesDir
    private val cacheDir: File = appContext.cacheDir
    private val modelUrl: String =
        "https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip"
    private val modelZipFile: File get() = File(filesDir, "model.zip")
    private val modelDirectory: File get() = File(filesDir, "vosk-model")
    private val modelExistFileCheck: File get() = File(modelDirectory, "ivector")


    init {
        val initialState = when {
            // if the model zip file exists, it means that the app was interrupted after the
            // download finished (because the file is downloaded in the cache, and is moved to its
            // actual position only after it finishes downloading), but before the unzip process
            // completed (because after that the zip is deleted), so the next step is to unzip
            modelZipFile.exists() -> Downloaded
            // if the model zip file does not exist, but the model directory exists, then the model
            // has been completely downloaded and unzipped, and should be ready to be loaded
            modelExistFileCheck.isDirectory -> NotLoaded
            // if the both the model zip file and the model directory do not exist, then the model
            // has not been downloaded yet
            else -> NotDownloaded
        }
        _state = MutableStateFlow(initialState)
        _uiState = MutableStateFlow(initialState.toUiState())
        uiState = _uiState
        scope.launch {
            _state.collect { _uiState.value = it.toUiState() }
        }
    }


    /**
     * Loads the model with [thenStartListening] if the model is already downloaded but not loaded
     * in RAM, or starts listening if [thenStartListening] is `true` and the model is already ready.
     */
    override fun tryLoad(thenStartListening: Boolean) {
        val s = _state.value
        if (s == NotLoaded) {
            load(thenStartListening)
        } else if (thenStartListening && s is Loaded) {
            startListening(s.speechService)
        }
    }

    /**
     * If the model is not being downloaded/unzipped/loaded, or if there was an error in any of
     * those steps, downloads/unzips/loads the model. If the model is already loaded (or is being
     * loaded) toggles listening state.
     */
    override fun onClick() {
        // the state can only be changed in the background by the jobs corresponding to Downloading,
        // Unzipping and Loading, but as can be seen below we don't do anything in case of
        // Downloading and Unzipping. For Loading however, special measures are taken in
        // toggleThenStartListening() and in load() to ensure the button click is not lost nor has
        // any unwanted behavior if the state changes right after checking its value in this switch.
        when (val s = _state.value) {
            is NotDownloaded -> download()
            is Downloading -> {} // wait for download to finish
            is ErrorDownloading -> download() // retry
            is Downloaded -> unzip()
            is Unzipping -> {} // wait for unzipping to finish
            is ErrorUnzipping -> unzip() // retry
            is NotLoaded -> load(true)
            is Loading -> toggleThenStartListening() // wait for loading to finish
            is ErrorLoading -> load(true) // retry
            is Loaded -> startListening(s.speechService)
            is Listening -> stopListening(s.speechService, true)
        }
    }

    /**
     * Downloads the model zip file. Sets the state to [Downloading], and periodically updates it
     * with downloading progress, until either [ErrorDownloading] or [Downloaded] are set as state.
     */
    private fun download() {
        _state.value = Downloading(0, 0)

        scope.launch(Dispatchers.IO) {
            try {
                val request: Request = Request.Builder().url(modelUrl).build()
                val response = okHttpClient.newCall(request).execute()

                downloadBinaryFileWithPartial(
                    response = response,
                    file = modelZipFile,
                    cacheDir = cacheDir,
                ) { currentBytes, totalBytes ->
                    _state.value = Downloading(currentBytes, totalBytes)
                }
            } catch (e: IOException) {
                Log.e(TAG, "Can't download Vosk model", e)
                _state.value = ErrorDownloading(e)
                return@launch
            }

            _state.value = Unzipping(0, 0)
            unzipImpl() // reuse same job
        }
    }

    /**
     * Sets the state to [Unzipping] and calls [unzipImpl] in the background.
     */
    private fun unzip() {
        _state.value = Unzipping(0, 0)

        scope.launch {
            unzipImpl()
        }
    }

    /**
     * Unzips the downloaded model zip file. Assumes the state has already ben set to an
     * indeterminate [Unzipping]`(0, 0)`, but periodically publishes states with unzipping progress.
     * Will set the state to [ErrorUnzipping] or [NotLoaded] in the end. Also deletes the zip
     * file once downloading is successfully complete, to save disk space.
     */
    private fun unzipImpl() {
        try {
            // delete the model directory in case there are leftover files from other models
            modelDirectory.deleteRecursively()

            ZipInputStream(modelZipFile.inputStream()).useEntries { entry ->
                val destinationFile = getDestinationFile(modelDirectory, entry.name)

                if (entry.isDirectory) {
                    // create directory
                    if (!destinationFile.mkdirs() && !destinationFile.isDirectory) {
                        throw IOException("mkdirs failed: $destinationFile")
                    }
                    return@useEntries
                }

                // else copy file
                BufferedOutputStream(FileOutputStream(destinationFile)).use { outputStream ->
                    val buffer = ByteArray(CHUNK_SIZE)
                    var length: Int
                    var currentBytes: Long = 0
                    _state.value = Unzipping(0, entry.size)

                    while (read(buffer).also { length = it } > 0) {
                        outputStream.write(buffer, 0, length)
                        currentBytes += length
                        _state.value = Unzipping(currentBytes, entry.size)
                    }
                    outputStream.flush()
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Can't unzip Vosk model", e)
            _state.value = ErrorUnzipping(e)
            return
        }

        // delete zip file after extraction to save memory
        if (!modelZipFile.delete()) {
            Log.w(TAG, "Can't delete Vosk model zip: $modelZipFile")
        }

        _state.value = NotLoaded
    }

    /**
     * Loads the model, and initially sets the state to [Loading] with [initialThenStartListening],
     * and later either sets the state to [Loaded] or calls [startListening] by checking the current
     * state's [Loading.thenStartListening] (which might have changed from
     * [initialThenStartListening] in the meantime, if the user clicked on the button while
     * loading).
     */
    private fun load(initialThenStartListening: Boolean) {
        _state.value = Loading(initialThenStartListening)

        scope.launch {
            val speechService: SpeechService
            try {
                LibVosk.setLogLevel(if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.WARNINGS)
                val model = Model(modelDirectory.absolutePath)
                val recognizer = Recognizer(model, SAMPLE_RATE)
                recognizer.setMaxAlternatives(ALTERNATIVE_COUNT)
                speechService = SpeechService(recognizer, SAMPLE_RATE)
            } catch (e: IOException) {
                Log.e(TAG, "Can't load Vosk model", e)
                _state.value = ErrorLoading(e)
                return@launch
            }

            if (!_state.compareAndSet(Loading(false), Loaded(speechService))) {
                // If the state wasn't thenStartListening=false, then thenStartListening=true.
                // compareAndSet() is used in conjunction with toggleThenStartListening() to ensure
                // atomicity.
                startListening(speechService)
            }
        }
    }

    /**
     * Atomically handles toggling the [Loading.thenStartListening] state, making sure that if in
     * the meantime the value is changed by [load], the user click is not wasted, and the state
     * machine does not end up in an inconsistent state.
     */
    private fun toggleThenStartListening() {
        if (
            !_state.compareAndSet(Loading(false), Loading(true)) &&
            !_state.compareAndSet(Loading(true), Loading(false))
        ) {
            // may happen if load() changes the state in the brief moment between when the state is
            // first checked before calling this function, and when the checks above are performed
            Log.w(TAG, "Cannot toggle thenStartListening")
            when (val newValue = _state.value) {
                is Loaded -> startListening(newValue.speechService)
                is Listening -> stopListening(newValue.speechService, true)
                is ErrorLoading -> {} // ignore the user's click
                // the else should never happen, since load() only transitions from Loading(...) to
                // one of Loaded, Listening or ErrorLoading
                else -> Log.e(TAG, "State was none of Loading, Loaded or Listening")
            }
        }
    }

    /**
     * Starts the speech service listening, and changes the state to [Listening].
     */
    private fun startListening(speechService: SpeechService) {
        _state.value = Listening(speechService)
        speechService.startListening(VoskListener(this, inputEventsModule, speechService))
    }

    /**
     * Stops the speech service from listening, and changes the state to [Loaded]. This is
     * `internal` because it is used by [VoskListener].
     */
    internal fun stopListening(speechService: SpeechService, sendNoneEvent: Boolean) {
        _state.value = Loaded(speechService)
        speechService.stop()
        if (sendNoneEvent) {
            inputEventsModule.tryEmitEvent(InputEvent.None)
        }
    }

    companion object {
        private const val CHUNK_SIZE = 1024 * 256 // 0.25 MB
        private const val SAMPLE_RATE = 44100.0f
        private const val ALTERNATIVE_COUNT = 5
        private val TAG = VoskInputDevice::class.simpleName
    }
}
