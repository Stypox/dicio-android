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

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A centralized exchange point for input events. Emitters (e.g. STT engines) should call
 * [tryEmitEvent], while consumers should subscribe to [events].
 */
@Singleton
class InputEventsModule @Inject constructor() {
    private val _events = MutableSharedFlow<InputEvent>(0, 1, BufferOverflow.DROP_OLDEST)
    val events: SharedFlow<InputEvent> = _events

    /**
     * Emits an event on [events].
     */
    internal fun tryEmitEvent(event: InputEvent) {
        _events.tryEmit(event)
    }
}
