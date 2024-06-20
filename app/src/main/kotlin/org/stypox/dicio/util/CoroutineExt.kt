package org.stypox.dicio.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


/**
 * Partially copied from DistinctFlowImpl in kotlinx.coroutines.flow.Distinct
 */
private class DistinctFlowWithFirstValue<T>(
    private val upstream: Flow<T>,
    private val firstValue: T,
): Flow<T> {
    override suspend fun collect(collector: FlowCollector<T>) {
        var previousValue = firstValue
        upstream.collect { newValue ->
            if (newValue != previousValue) {
                previousValue = newValue
                collector.emit(newValue)
            }
        }
    }
}

/**
 * Like `distinctUntilChanged()`, but the first value is collected in a blocking way and returned
 * immediately, while all following values are emitted in the returned flow, but only if they are
 * distinct from the previous value, and initially the first value counts as the previous value.
 */
fun <T> Flow<T>.distinctUntilChangedBlockingFirst(): Pair<T, Flow<T>> {
    val firstValue: T = runBlocking { first() }
    return Pair(firstValue, DistinctFlowWithFirstValue(this, firstValue))
}

fun <T> Flow<T>.toStateFlowDistinctBlockingFirst(scope: CoroutineScope): StateFlow<T> {
    val (firstValue, nextValueFlow) = distinctUntilChangedBlockingFirst()
    val stateFlow = MutableStateFlow(firstValue)
    scope.launch {
        nextValueFlow.collect { stateFlow.emit(it) }
    }
    return stateFlow
}
