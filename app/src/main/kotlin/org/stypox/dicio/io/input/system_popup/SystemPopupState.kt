package org.stypox.dicio.io.input.system_popup

import org.stypox.dicio.io.input.InputEvent
import org.stypox.dicio.io.input.SttState

sealed interface SystemPopupState {

    /**
     * No activity could be resolved
     */
    data object NotAvailable : SystemPopupState

    /**
     * Some activity could be resolved, so the input device should be able to answer to queries
     */
    data object Available : SystemPopupState

    /**
     * `startActivity(intent)` has just been called and now we are waiting for the activity result
     */
    data class WaitingForResult(
        internal val listener: (InputEvent) -> Unit,
    ) : SystemPopupState {
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
    ) : SystemPopupState {
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
    ) : SystemPopupState {
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