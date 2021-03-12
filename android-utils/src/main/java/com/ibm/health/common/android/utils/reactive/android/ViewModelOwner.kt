package com.ibm.health.common.android.utils.reactive.android

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.ibm.health.common.android.utils.reactive.BaseEvents
import com.ensody.reactivestate.EventNotifier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

// --------------------------------------------------------------------------------------------------------------------
// IMPORTANT: This whole concept is about implementing correct lifecycle handling around Android's messed-up API.
// See BaseViewModel for a more detailed explanation.
// --------------------------------------------------------------------------------------------------------------------

/** Common interface for activities/fragments holding a [BaseViewModel]. */
public interface ViewModelOwner<T : BaseEvents> : LifecycleOwner {
    public val viewModel: BaseViewModel<T>
}

public fun <T : BaseEvents> ViewModelOwner<T>.watchLoading(
    isLoading: StateFlow<Boolean>,
    setLoading: (Boolean) -> Unit,
) {
    watchLoading(isLoading, viewModel.eventNotifier, setLoading)
}

/** Observes a Boolean and triggers the [setLoading] function, taking lifecycle handling into account. */
public fun <T> LifecycleOwner.watchLoading(
    isLoading: StateFlow<Boolean>,
    eventNotifier: EventNotifier<T>,
    setLoading: (Boolean) -> Unit,
) {
    lifecycleScope.launch {
        isLoading.collect {
            // Since calling setLoading(true) is only safe in the started state we pass it into the eventNotifier
            // while setLoading(false) MUST be called directly because otherwise this fragment won't ever reach
            // the started state as long as the loading dialog is visible (because the loading dialog is above it).
            if (it) {
                eventNotifier {
                    // XXX: Just in case isLoading was modified in the meantime
                    setLoading(isLoading.value)
                }
            } else {
                setLoading(it)
            }
        }
    }
}

/** Processes the events in the given [eventNotifier], taking lifecycle handling into account. */
public fun <T : BaseEvents> LifecycleOwner.processEvents(target: T, eventNotifier: EventNotifier<T>) {
    // XXX: There are edge cases with launchWhenStarted, but there are no perfect alternatives:
    //
    // * Sending an event in onStop can cause event loss because the collect consumes an element without executing it.
    // * Pausing the coroutine works only on the main thread. If you switch to dispatchers.io the coroutine will
    //   continue running in the stopped state, so accessing the UI will cause a crash!!!
    //
    // Usually, this isn't a big problem because if you do any async computations (esp. ones that require switching
    // threads) you should do them on the viewModelScope and send the result back via eventNotifier.
    //
    // Some people decide to cancel the whole coroutine in onStop, but this has the problem that computations can
    // get canceled (and lost) when locking the screen. With launchWhenStarted, unlocking the screen will just resume
    // the computation without any loss.
    //
    // Maybe the even better solution is to handle screen rotation and other events without activity destruction.
    // Then you can run everything in the lifecycleScope and all problems are gone and maybe new problems will appear.
    // How can you not fall in love with Android's wonderfully simple architecture?

    lifecycleScope.launchWhenStarted {
        eventNotifier.collect {
            try {
                target.it()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                target.onError(e)
            }
        }
    }
}
