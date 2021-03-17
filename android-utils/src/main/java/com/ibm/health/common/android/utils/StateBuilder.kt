package com.ibm.health.common.android.utils

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.ensody.reactivestate.ErrorEvents
import com.ensody.reactivestate.StateFlowStore
import com.ensody.reactivestate.android.handleEvents
import com.ensody.reactivestate.android.stateFlowStore
import com.ensody.reactivestate.android.stateViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.emitAll
import kotlin.reflect.KClass

/**
 * Creates a [State] wrapped in a [WrapperStateViewModel].
 *
 * If you need a [SavedStateHandle] you can access it using [WrapperStateViewModel.savedStateHandle].
 */
public inline fun <E : BaseEvents, reified S : State<E>, O> O.buildState(
    noinline block: StateHost.() -> S,
): Lazy<S> where O : Fragment, O : ErrorEvents, O : LoadingStateHook =
    _buildState(S::class, block)

@Suppress("FunctionName")
public fun <E : BaseEvents, S : State<E>, O> O._buildState(
    cls: KClass<S>,
    block: StateHost.() -> S,
): Lazy<S> where O : Fragment, O : ErrorEvents, O : LoadingStateHook =
    attachLazyState(cls, stateViewModel { WrapperStateViewModel(it) }, block)

/**
 * Creates a [State] wrapped in a [WrapperStateViewModel].
 *
 * If you need a [StateFlowStore] you can access it using [WrapperStateViewModel.stateFlowStore].
 */
public inline fun <E : BaseEvents, reified S : State<E>, O> O.buildState(
    noinline block: StateHost.() -> S,
): Lazy<S> where O : ComponentActivity, O : ErrorEvents, O : LoadingStateHook =
    _buildState(S::class, block)

public fun <E : BaseEvents, S : State<E>, O> O._buildState(
    cls: KClass<S>,
    block: StateHost.() -> S,
): Lazy<S> where O : ComponentActivity, O : ErrorEvents, O : LoadingStateHook =
    attachLazyState(cls, stateViewModel { WrapperStateViewModel(it) }, block)

/** Creates a child [State] and merges its [State.eventNotifier] and [State.isLoading] into the parent. */
public fun <E : BaseEvents, P : BaseState<out E>, S : State<E>> P.buildState(block: () -> S): Lazy<S> {
    val child = block()
    attachState(child)
    return lazy { child }
}

/** Merges the [child]'s [State.eventNotifier] and [State.isLoading] into the parent. */
public fun <E : BaseEvents> BaseState<out E>.attachState(child: State<E>) {
    launch(withLoading = false) {
        eventNotifier.emitAll(child.eventNotifier)
    }
    isLoading.addLoadingState(child.isLoading)
}

@Suppress("UNCHECKED_CAST")
private fun <E : ErrorEvents, S : State<E>, O> O.attachLazyState(
    cls: KClass<S>,
    lazyViewModel: Lazy<WrapperStateViewModel>,
    block: WrapperStateViewModel.() -> S,
): Lazy<S> where O : LifecycleOwner, O : ErrorEvents, O : LoadingStateHook {
    val lazyState = lazy {
        val viewModel = lazyViewModel.value
        val state = (viewModel.stateRegistry[cls] as? S) ?: viewModel.block()
        viewModel.stateRegistry[cls] = state
        state
    }
    lifecycleScope.launchWhenCreated {
        isLoading.addLoadingState(lazyState.value.isLoading)
        lazyState.value.eventNotifier.handleEvents(this@attachLazyState as E, this@attachLazyState)
    }
    return lazyState
}

/** Provides additional attributes for constructing a [State] using [buildState]. */
public interface StateHost {
    public val scope: CoroutineScope

    public val stateFlowStore: StateFlowStore

    public val savedStateHandle: SavedStateHandle
}

/** A [State] container. */
private class WrapperStateViewModel(
    override val savedStateHandle: SavedStateHandle,
) : ViewModel(), StateHost {

    override val scope: CoroutineScope get() = viewModelScope

    override val stateFlowStore: StateFlowStore = savedStateHandle.stateFlowStore(viewModelScope)

    val stateRegistry: MutableMap<KClass<*>, State<*>> = mutableMapOf()
}
