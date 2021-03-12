package com.ibm.health.common.android.utils.reactive.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensody.reactivestate.MutableValueFlow
import com.ibm.health.common.android.utils.reactive.BaseEvents
import com.ensody.reactivestate.EventNotifier
import com.ensody.reactivestate.derived
import com.ensody.reactivestate.get
import com.ibm.health.common.android.utils.reactive.State
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext

// --------------------------------------------------------------------------------------------------------------------
// WHY OH WHY? See State.kt for the explanation.
// --------------------------------------------------------------------------------------------------------------------
/**
 * Base ViewModel, providing observable state via `StateFlow`/`MutableStateFlow` attributes and
 * events via [eventNotifier].
 */
public abstract class BaseViewModel<T : BaseEvents> : ViewModel(), State<T> {
    final override val launcherScope: CoroutineScope get() = viewModelScope
    override val eventNotifier: EventNotifier<T> = EventNotifier()
    private val loadingCount = MutableValueFlow(AtomicInteger(0))
    override val isLoading: StateFlow<Boolean> = derived(Eagerly) {
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
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                onError?.invoke(e) ?: eventNotifier { onError(e) }
            } finally {
                if (withLoading) {
                    loadingCount.update { it.decrementAndGet() }
                }
            }
        }
}
