package com.ibm.health.common.android.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.ensody.reactivestate.ReducingStateFlow
import kotlinx.coroutines.CoroutineScope
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

/**
 * Creates a new [IsLoading] instance and on change calls [setLoading] (defaults to [LoadingStateHook.setLoading]).
 */
@Suppress("FunctionName")
public fun <T> T.IsLoading(
    setLoading: ((Boolean) -> Unit)? = ::setLoading,
): IsLoading where T : LifecycleOwner, T : LoadingStateHook =
    IsLoading(lifecycleScope).also { isLoading ->
        if (setLoading != null) {
            lifecycleScope.launchWhenStarted {
                watchLoading(isLoading, setLoading)
            }
        }
    }

/** Creates a new [IsLoading] instance. */
@Suppress("FunctionName")
public fun State<*>.IsLoading(): IsLoading =
    IsLoading(launcherScope)

@Suppress("FunctionName")
public fun IsLoading(scope: CoroutineScope): IsLoading =
    ReducingStateFlow(scope) { loadings -> loadings.count { it } > 0 }

public typealias IsLoading = ReducingStateFlow<Boolean, Boolean>

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
