package com.ibm.health.common.android.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope

/**
 * Base ViewModel, providing observable state via `StateFlow`/`MutableStateFlow` attributes and
 * events via [eventNotifier].
 */
public abstract class BaseViewModel<T : BaseEvents> private constructor(
    public val parentState: State<T>,
) : ViewModel(), State<T> by ViewModelBaseState<T>() {
    public constructor() : this(ViewModelBaseState())

    init {
        (parentState as? ViewModelBaseState)?.wrappedLauncherScope?.value = viewModelScope
    }
}

private class ViewModelBaseState<T : BaseEvents>(
    val wrappedLauncherScope: Wrapper<CoroutineScope?> = Wrapper(null),
) : BaseState<T>(lazy { wrappedLauncherScope.value!! })

private class Wrapper<T>(var value: T)
