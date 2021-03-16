package com.ibm.health.common.android.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.ensody.reactivestate.MutableValueFlow
import com.ensody.reactivestate.android.onCreate
import com.ensody.reactivestate.derived
import com.ensody.reactivestate.get
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/** Implement this in your fragment/activity to get automatic tracking of dependencies' loading states. */
public interface LoadingStateHook {
    /** A [StateFlow] tracking the loading state of all dependencies. */
    public val isLoading: IsLoading

    /**
     * This is triggered whenever the [isLoading] state changes (i.e. any dependency is loading something).
     *
     * If you need more fine-grained loading states you can ignore this and track the individual dependencies.
     */
    public fun setLoading(isLoading: Boolean)
}

@Suppress("FunctionName")
public fun <T> T.IsLoading(): IsLoading where T : LifecycleOwner, T : LoadingStateHook {
    val loadingStates: MutableValueFlow<MutableSet<StateFlow<Boolean>>> = MutableValueFlow(mutableSetOf())
    val isLoading = lifecycleScope.derived {
        get(loadingStates).count { get(it) } > 0
    }
    onCreate {
        watchLoading(isLoading, ::setLoading)
    }
    return IsLoading(loadingStates, isLoading)
}

public class IsLoading internal constructor(
    private val loadingStates: MutableValueFlow<MutableSet<StateFlow<Boolean>>>,
    private val isLoading: StateFlow<Boolean>,
) : StateFlow<Boolean> by isLoading {

    public fun addLoadingState(loading: StateFlow<Boolean>) {
        loadingStates.update { it.add(loading) }
    }
}

/** Observes a Boolean and triggers the [setLoading] function, taking lifecycle handling into account. */
public fun LifecycleOwner.watchLoading(
    isLoading: StateFlow<Boolean>,
    setLoading: (Boolean) -> Unit,
) {
    lifecycleScope.launch {
        isLoading.collect {
            // Since calling setLoading(true) is only safe in the started state we pass it into the eventNotifier
            // while setLoading(false) MUST be called directly because otherwise this fragment won't ever reach
            // the started state as long as the loading dialog is visible.
            if (it) {
                lifecycleScope.launchWhenStarted {
                    // Take current value in case isLoading was modified in the meantime
                    setLoading(isLoading.value)
                }
            } else {
                setLoading(it)
            }
        }
    }
}
