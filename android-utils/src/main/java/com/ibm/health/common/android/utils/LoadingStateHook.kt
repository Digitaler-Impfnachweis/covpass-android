/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.ensody.reactivestate.ErrorEvents
import com.ensody.reactivestate.MutableValueFlow
import com.ensody.reactivestate.OnReactiveStateAttached
import com.ensody.reactivestate.ReactiveState
import com.ensody.reactivestate.incrementFrom
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/** Implement this in your fragment/activity to get automatic tracking of dependencies' loading states. */
public interface LoadingStateHook : OnReactiveStateAttached {
    public val loading: MutableValueFlow<Int>

    /**
     * This is triggered whenever the loading state changes (i.e. any dependency is loading something).
     *
     * If you need more fine-grained loading states you can ignore this and track the individual dependencies.
     */
    public fun setLoading(isLoading: Boolean)

    override fun onReactiveStateAttached(reactiveState: ReactiveState<out ErrorEvents>) {
        (this as? LifecycleOwner)?.lifecycleScope?.launchWhenStarted {
            loading.incrementFrom(reactiveState.loading)
        }
    }
}

/** Observes a Boolean and triggers the [setLoading] function, taking lifecycle handling into account. */
public fun LifecycleOwner.watchLoading(
    isLoading: StateFlow<Int>,
    setLoading: (Boolean) -> Unit,
) {
    lifecycleScope.launch {
        isLoading.collect {
            // Since calling setLoading(true) is only safe in the started state we pass it into the eventNotifier
            // while setLoading(false) MUST be called directly because otherwise this fragment won't ever reach
            // the started state as long as the loading dialog is visible.
            if (it > 0) {
                lifecycleScope.launchWhenStarted {
                    // Take current value in case isLoading was modified in the meantime
                    setLoading(isLoading.value > 0)
                }
            } else {
                setLoading(it > 0)
            }
        }
    }
}
