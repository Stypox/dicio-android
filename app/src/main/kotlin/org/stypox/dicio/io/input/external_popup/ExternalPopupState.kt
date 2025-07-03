package org.stypox.dicio.io.input.external_popup

import org.stypox.dicio.io.input.InputEvent
import org.stypox.dicio.io.input.SttState

sealed interface ExternalPopupState {

    /**
     * No activity could be resolved
     */
    data object NotAvailable : ExternalPopupState

    /**
     * Some activity could be resolved, so the input device should be able to answer to queries
     */
    data object Available : ExternalPopupState

    /**
     * `startActivity(intent)` has just been called and now we are waiting for the activity result
     */
    data class WaitingForResult(
        internal val listener: (InputEvent) -> Unit,
    ) : ExternalPopupState {
        override fun equals(other: Any?): Boolean {
            return other is WaitingForResult
        }

        override fun hashCode(): Int {
            return 0
        }
    }

    /**
     * `startActivityForResult(intent)` failed to run
     */
    data class ErrorStartingActivity(
        val throwable: Throwable
    ) : ExternalPopupState {
        override fun equals(other: Any?): Boolean {
            return other is ErrorStartingActivity
        }

        override fun hashCode(): Int {
            return 1
        }
    }

    /**
     * The result from `startActivityForResult(intent)` is not RESULT_OK
     */
    data class ErrorActivityResult(
        val resultCode: Int
    ) : ExternalPopupState {
        override fun equals(other: Any?): Boolean {
            return other is ErrorActivityResult
        }

        override fun hashCode(): Int {
            return resultCode * 10
        }
    }

    fun toUiState(): SttState {
        return when (this) {
            NotAvailable -> SttState.NotAvailable
            Available -> SttState.Loaded
            is WaitingForResult -> SttState.WaitingForResult
            is ErrorStartingActivity -> SttState.ErrorLoading(throwable)
            is ErrorActivityResult -> SttState.ErrorLoading(ResultCodeException(resultCode))
        }
    }
}