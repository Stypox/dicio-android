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

package org.stypox.dicio.io.input

/**
 * This is almost symmetrical to [org.stypox.dicio.io.input.vosk.VoskState], except that there are
 * no implementation-defined fields. For this reason, if in the future another STT engine will be
 * used, this class and the whole UI layer could be kept the same.
 */
sealed interface SttState {
    /**
     * Does not have a counterpart in [org.stypox.dicio.io.input.vosk.VoskState] and should never be
     * generated directly by a [org.stypox.dicio.io.input.SttInputDevice]. In fact, this is used
     * directly in the UI layer, since permission checks can only be done there.
     */
    data object NoMicrophonePermission : SttState

    /**
     * The STT engine has not been initialized yet (waiting for a locale to be available)
     */
    data object NotInitialized : SttState

    /**
     * The STT engine cannot be made available, e.g. because the current language is not supported
     */
    data object NotAvailable : SttState

    /**
     * The model is not present on disk, neither in unzipped and in zipped form.
     */
    data object NotDownloaded : SttState

    data class Downloading(
        val currentBytes: Long,
        val totalBytes: Long,
    ) : SttState

    data class ErrorDownloading(
        val throwable: Throwable
    ) : SttState

    data object Downloaded : SttState

    /**
     * Vosk models are distributed in Zip files that need unzipping to be ready.
     */
    data class Unzipping(
        val currentBytes: Long,
        val totalBytes: Long,
    ) : SttState

    data class ErrorUnzipping(
        val throwable: Throwable
    ) : SttState

    /**
     * The model is present on disk, but was not loaded in RAM yet.
     */
    data object NotLoaded : SttState

    /**
     * The model is being loaded, and [thenStartListening] indicates whether, once loading is
     * finished, the STT should start listening right away.
     */
    data class Loading(
        val thenStartListening: Boolean
    ) : SttState

    data class ErrorLoading(
        val throwable: Throwable
    ) : SttState

    /**
     * The model is ready in RAM, and can start listening at any time.
     */
    data object Loaded : SttState

    /**
     * The model is listening.
     */
    data object Listening : SttState
}
