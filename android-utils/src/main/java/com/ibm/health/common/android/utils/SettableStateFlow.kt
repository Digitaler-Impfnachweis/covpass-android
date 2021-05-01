package com.ibm.health.common.android.utils

import com.ensody.reactivestate.MutableValueFlow
import kotlinx.coroutines.flow.StateFlow

// FIXME/TODO: Switch to SuspendMutableValueFlow

/** A [StateFlow] that has a suspendable [set] function to change its value. */
public class SettableStateFlow<T>(
    private val flow: MutableValueFlow<T>,
    public val set: suspend (value: T) -> Unit,
) : StateFlow<T> by flow {
    public suspend fun update(block: (value: T) -> Unit) {
        block(value)
        set(value)
    }
}
