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

import org.stypox.dicio.ui.main.SttState
import org.vosk.android.SpeechService

/**
 * The internal state for [VoskInputDevice]. This is an enum with different fields depending on the
 * current state, to avoid having nullable objects all over the place in [VoskInputDevice].
 * [SttState] is symmetrical to this enum, except that it does not expose implementation-defined
 * fields to the UI, such as [SpeechService].
 */
sealed class VoskState {

    /**
     * The model is not available for the current locale
     */
    data object NotAvailable : VoskState()

    /**
     * The model is not present on disk, neither in unzipped and in zipped form.
     */
    data class NotDownloaded(
        val modelUrl: String
    ) : VoskState()

    data class Downloading(
        val currentBytes: Long,
        val totalBytes: Long,
    ) : VoskState()

    data class ErrorDownloading(
        val modelUrl: String,
        val throwable: Throwable
    ) : VoskState()

    data object Downloaded : VoskState()

    /**
     * Vosk models are distributed in Zip files that need unzipping to be ready.
     */
    data class Unzipping(
        val currentBytes: Long,
        val totalBytes: Long,
    ) : VoskState()

    data class ErrorUnzipping(
        val throwable: Throwable
    ) : VoskState()

    /**
     * The model is present on disk, but was not loaded in RAM yet.
     */
    data object NotLoaded : VoskState()

    /**
     * The model is being loaded, and [thenStartListening] indicates whether once loading is
     * finished, the STT should start listening right away.
     */
    data class Loading(
        val thenStartListening: Boolean
    ) : VoskState()

    data class ErrorLoading(
        val throwable: Throwable
    ) : VoskState()

    /**
     * The model, stored in [SpeechService], is ready in RAM, and can start listening at any time.
     */
    data class Loaded(
        internal val speechService: SpeechService
    ) : VoskState()

    /**
     * The model, stored in [SpeechService], is listening.
     */
    data class Listening(
        internal val speechService: SpeechService
    ) : VoskState()

    /**
     * Converts this [VoskState] to a [SttState], which is basically the same, except that
     * implementation-defined fields (e.g. [SpeechService]) are stripped away.
     */
    fun toUiState(): SttState {
        return when (this) {
            NotAvailable -> SttState.NotAvailable
            is NotDownloaded -> SttState.NotDownloaded
            is Downloading -> SttState.Downloading(currentBytes, totalBytes)
            is ErrorDownloading -> SttState.ErrorDownloading(throwable)
            Downloaded -> SttState.Downloaded
            is Unzipping -> SttState.Unzipping(currentBytes, totalBytes)
            is ErrorUnzipping -> SttState.ErrorUnzipping(throwable)
            NotLoaded -> SttState.NotLoaded
            is Loading -> SttState.Loading(thenStartListening)
            is ErrorLoading -> SttState.ErrorLoading(throwable)
            is Loaded -> SttState.Loaded
            is Listening -> SttState.Listening
        }
    }
}
