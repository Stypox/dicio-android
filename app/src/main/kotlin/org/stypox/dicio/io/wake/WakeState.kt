package org.stypox.dicio.io.wake

sealed interface WakeState {
    /**
     * Should never be generated directly by a [org.stypox.dicio.io.wake.WakeDevice]. In fact,
     * this is used directly in the UI layer, since permission checks can only be done there.
     */
    data object NoMicOrNotificationPermission : WakeState

    data object NotDownloaded : WakeState

    data class Downloading(
        val currentBytes: Long,
        val totalBytes: Long,
    ) : WakeState

    data class ErrorDownloading(
        val throwable: Throwable
    ) : WakeState

    data object NotLoaded : WakeState

    data object Loading : WakeState

    data class ErrorLoading(
        val throwable: Throwable
    ) : WakeState

    data object Loaded : WakeState
}
