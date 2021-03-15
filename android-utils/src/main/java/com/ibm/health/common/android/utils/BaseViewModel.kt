package com.ibm.health.common.android.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensody.reactivestate.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext

/**
 * Base ViewModel, providing observable state via `StateFlow`/`MutableStateFlow` attributes and
 * events via [eventNotifier].
 */
public abstract class BaseViewModel<T : BaseEvents> : ViewModel(), State<T> {
    final override val launcherScope: CoroutineScope get() = viewModelScope
    override val eventNotifier: EventNotifier<T> = EventNotifier()
    private val loadingCount = MutableValueFlow(AtomicInteger(0))
    override val isLoading: StateFlow<Boolean> = derived {
        get(loadingCount).get() > 0
    }

    override fun launch(
        context: CoroutineContext,
        start: CoroutineStart,
        withLoading: Boolean,
        onError: (suspend (Throwable) -> Unit)?,
        block: suspend CoroutineScope.() -> Unit,
    ): Job =
        launcherScope.launch(context = context, start = start) {
            try {
                if (withLoading) {
                    loadingCount.update { it.incrementAndGet() }
                }
                withErrorReporting(eventNotifier, onError) {
                    block()
                }
            } finally {
                if (withLoading) {
                    loadingCount.update { it.decrementAndGet() }
                }
            }
        }
}
