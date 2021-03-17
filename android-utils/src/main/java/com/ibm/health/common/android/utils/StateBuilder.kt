package com.ibm.health.common.android.utils

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.ensody.reactivestate.ErrorEvents
import com.ensody.reactivestate.StateFlowStore
import com.ensody.reactivestate.android.stateFlowStore

/**
 * Creates a [State] wrapped in a [WrapperStateViewModel].
 *
 * If you need a [SavedStateHandle] you can access it using [WrapperStateViewModel.savedStateHandle].
 */
public inline fun <reified E : BaseEvents, reified S : State<E>, O> O.buildState(
    noinline block: WrapperStateViewModel<E, S>.() -> S,
): Lazy<S> where O : Fragment, O : ErrorEvents, O : LoadingStateHook =
    LazyStateProperty(stateBaseViewModel { WrapperStateViewModel(it, block) })

/**
 * Creates a [State] wrapped in a [WrapperStateViewModel].
 *
 * If you need a [StateFlowStore] you can access it using [WrapperStateViewModel.stateFlowStore].
 */
public inline fun <reified E : BaseEvents, reified S : State<E>, O> O.buildState(
    noinline block: WrapperStateViewModel<E, S>.() -> S,
): Lazy<S> where O : ComponentActivity, O : ErrorEvents, O : LoadingStateHook =
    LazyStateProperty(stateBaseViewModel { WrapperStateViewModel(it, block) })

/** A [State] container. */
public class WrapperStateViewModel<E : BaseEvents, S : State<E>>(
    public val savedStateHandle: SavedStateHandle,
    block: WrapperStateViewModel<E, S>.() -> S,
) : BaseViewModel<E>() {

    public val stateFlowStore: StateFlowStore = savedStateHandle.stateFlowStore(viewModelScope)

    internal val state: S by lazy { block() }
}

public class LazyStateProperty<E : BaseEvents, T : State<E>>(
    public val viewModel: Lazy<WrapperStateViewModel<E, T>>,
) : Lazy<T> by lazy({ viewModel.value.state })
