package com.ibm.health.common.android.utils

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.ensody.reactivestate.ErrorEvents
import com.ensody.reactivestate.EventNotifier
import com.ensody.reactivestate.NamespacedStateFlowStore
import com.ensody.reactivestate.StateFlowStore
import com.ensody.reactivestate.android.handleEvents
import com.ensody.reactivestate.android.stateFlowStore
import com.ensody.reactivestate.android.stateViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.emitAll
import kotlin.reflect.KClass

/**
 * Creates a [State] wrapped in a ViewModel.
 *
 * The `this` argument to [block] is a [StateHost] which provides a [StateHost.scope] (the [ViewModel.viewModelScope])
 * and [StateHost.stateFlowStore] and other useful properties.
 *
 * The resulting [State]'s `isLoading` and `eventNotifier` are automatically observed.
 */
public inline fun <E : BaseEvents, reified S : State<E>, O> O.buildState(
    withLoading: IsLoading? = isLoading,
    noinline block: StateHost.() -> S,
): Lazy<S> where O : Fragment, O : ErrorEvents, O : LoadingStateHook =
    _buildState(S::class, withLoading, block)

@Suppress("FunctionName")
public fun <E : BaseEvents, S : State<E>, O> O._buildState(
    cls: KClass<S>,
    withLoading: IsLoading?,
    block: StateHost.() -> S,
): Lazy<S> where O : Fragment, O : ErrorEvents, O : LoadingStateHook =
    attachLazyState(cls, stateViewModel { WrapperStateViewModel(it) }, withLoading, block)

/**
 * Creates a [State] wrapped in a ViewModel.
 *
 * The `this` argument to [block] is a [StateHost] which provides a [StateHost.scope] (the [ViewModel.viewModelScope])
 * and [StateHost.stateFlowStore] and other useful properties.
 *
 * The resulting [State]'s `isLoading` and `eventNotifier` are automatically observed.
 */
public inline fun <E : BaseEvents, reified S : State<E>, O> O.buildState(
    withLoading: IsLoading? = isLoading,
    noinline block: StateHost.() -> S,
): Lazy<S> where O : ComponentActivity, O : ErrorEvents, O : LoadingStateHook =
    _buildState(S::class, withLoading, block)

@Suppress("FunctionName")
public fun <E : BaseEvents, S : State<E>, O> O._buildState(
    cls: KClass<S>,
    withLoading: IsLoading?,
    block: StateHost.() -> S,
): Lazy<S> where O : ComponentActivity, O : ErrorEvents, O : LoadingStateHook =
    attachLazyState(cls, stateViewModel { WrapperStateViewModel(it) }, withLoading, block)

/**
 * Creates a child [State] and merges its [State.eventNotifier] and [State.isLoading] into the parent.
 *
 * Note that the parent has to implement the child's events interface.
 */
public fun <E : BaseEvents, P : State<out E>, S : State<E>> P.buildState(
    withLoading: IsLoading? = isLoading,
    block: () -> S,
): Lazy<S> {
    val child = block()
    attachState(child, withLoading = withLoading)
    // We return a Lazy only for consistency with the other buildState functions
    return lazy { child }
}

/** Merges the [child]'s [State.eventNotifier] and [State.isLoading] into the parent. */
public fun <E : BaseEvents> State<out E>.attachState(
    child: State<E>,
    withLoading: IsLoading? = isLoading,
) {
    mergeEventsFrom(child.eventNotifier)
    withLoading?.add(child.isLoading)
}

/** Merges the given [eventNotifier] into this [State]'s [EventNotifier]. */
public fun <E : BaseEvents> State<out E>.mergeEventsFrom(
    eventNotifier: EventNotifier<E>,
) {
    launch(withLoading = false) {
        this@mergeEventsFrom.eventNotifier.emitAll(eventNotifier)
    }
}

@Suppress("UNCHECKED_CAST")
private fun <E : ErrorEvents, S : State<E>, O> O.attachLazyState(
    cls: KClass<S>,
    lazyViewModel: Lazy<WrapperStateViewModel>,
    withLoading: IsLoading?,
    block: StateHost.() -> S,
): Lazy<S> where O : LifecycleOwner, O : ErrorEvents, O : LoadingStateHook {
    val lazyState = lazy {
        val viewModel = lazyViewModel.value
        val namespace = cls.qualifiedName
            ?: throw IllegalArgumentException("The State class has no qualified name")
        val state = (viewModel.stateRegistry[cls] as? S)
            ?: StateHostImpl(
                scope = viewModel.viewModelScope,
                stateFlowStore = NamespacedStateFlowStore(viewModel.stateFlowStore, namespace),
            ).block()
        viewModel.stateRegistry[cls] = state
        state
    }
    lifecycleScope.launchWhenCreated {
        withLoading?.add(lazyState.value.isLoading)
        lazyState.value.eventNotifier.handleEvents(this@attachLazyState as E, this@attachLazyState)
    }
    return lazyState
}

/** Provides additional attributes for constructing a [State] using [buildState]. */
public interface StateHost {
    /** The [CoroutineScope] to use for the [State]. */
    public val scope: CoroutineScope

    /**
     * A [StateFlowStore] backed by a [SavedStateHandle].
     */
    public val stateFlowStore: StateFlowStore
}

private class StateHostImpl(
    override val scope: CoroutineScope,
    override val stateFlowStore: StateFlowStore,
) : StateHost

/** The container [ViewModel] holding [State] instances. */
private class WrapperStateViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val stateFlowStore: StateFlowStore = savedStateHandle.stateFlowStore(viewModelScope)

    val stateRegistry: MutableMap<KClass<*>, State<*>> = mutableMapOf()
}
