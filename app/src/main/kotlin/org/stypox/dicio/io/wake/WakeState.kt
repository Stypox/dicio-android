package org.stypox.dicio.io.wake

sealed interface WakeState {

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
