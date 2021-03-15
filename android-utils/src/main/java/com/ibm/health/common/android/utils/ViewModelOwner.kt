package com.ibm.health.common.android.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.ensody.reactivestate.EventNotifier
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/** Common interface for activities/fragments holding a [BaseViewModel]. */
public interface ViewModelOwner<T : BaseEvents> : LifecycleOwner {
    public val viewModel: BaseViewModel<T>
}

/** Watches the loading state and triggers `setLoading()`. */
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
            // the started state as long as the loading dialog is visible.
            if (it) {
                eventNotifier {
                    // XXX: Take current value in case isLoading was modified in the meantime
                    setLoading(isLoading.value)
                }
            } else {
                setLoading(it)
            }
        }
    }
}
