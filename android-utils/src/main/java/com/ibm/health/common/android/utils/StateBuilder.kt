package com.ibm.health.common.android.utils

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.ensody.reactivestate.StateFlowStore
import com.ensody.reactivestate.android.stateFlowStore

/**
 * Creates a [State] wrapped in a [WrapperStateViewModel].
 *
 * If you need a [SavedStateHandle] you can access it using [WrapperStateViewModel.savedStateHandle].
 */
public inline fun <reified E : BaseEvents, reified S : State<E>> BaseHookedFragment.buildState(
    crossinline block: WrapperStateViewModel<E, S>.() -> S,
): Lazy<S> {
    val builder: WrapperStateViewModel<E, S>.() -> S = { block() }
    return lazy { stateBaseViewModel { WrapperStateViewModel(it, builder) }.value.state }
}

/**
 * Creates a [State] wrapped in a [WrapperStateViewModel].
 *
 * If you need a [StateFlowStore] you can access it using [WrapperStateViewModel.stateFlowStore].
 */
public inline fun <reified E : BaseEvents, reified S : State<E>> BaseHookedActivity.buildState(
    noinline block: WrapperStateViewModel<E, S>.() -> S,
): Lazy<S> {
    val builder: WrapperStateViewModel<E, S>.() -> S = { block() }
    return lazy { stateBaseViewModel { WrapperStateViewModel(it, builder) }.value.state }
}

/** A [State] container. */
public class WrapperStateViewModel<E : BaseEvents, S : State<E>>(
    public val savedStateHandle: SavedStateHandle,
    block: WrapperStateViewModel<E, S>.() -> S,
) : BaseViewModel<E>() {

    public val stateFlowStore: StateFlowStore = savedStateHandle.stateFlowStore(viewModelScope)

    public val state: S by lazy { block() }
}
