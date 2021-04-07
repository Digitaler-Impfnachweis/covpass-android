package com.ibm.health.common.android.utils

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.ensody.reactivestate.ErrorEvents
import com.ensody.reactivestate.EventNotifier
import com.ensody.reactivestate.android.*
import kotlinx.coroutines.flow.emitAll

/**
 * Creates a [State] wrapped in a ViewModel and observes its [State.eventNotifier] and [State.isLoading].
 *
 * The [block] has the same semantics as with [buildOnViewModel].
 */
public inline fun <reified E : BaseEvents, reified S : State<E>, O> O.buildState(
    withLoading: IsLoading? = isLoading,
    noinline block: BuildOnViewModelContext.() -> S,
): Lazy<S> where O : Fragment, O : ErrorEvents, O : LoadingStateHook =
    buildOnViewModel(block).attachLazyState(owner = this, withLoading = withLoading)

/**
 * Creates a [State] wrapped in a ViewModel and observes its [State.eventNotifier] and [State.isLoading].
 *
 * The [block] has the same semantics as with [buildOnViewModel].
 */
public inline fun <reified E : BaseEvents, reified S : State<E>, O> O.buildState(
    withLoading: IsLoading? = isLoading,
    noinline block: BuildOnViewModelContext.() -> S,
): Lazy<S> where O : ComponentActivity, O : ErrorEvents, O : LoadingStateHook =
    buildOnViewModel(block).attachLazyState(owner = this, withLoading = withLoading)

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
public inline fun <reified E : ErrorEvents, S : State<E>, O> Lazy<S>.attachLazyState(
    owner: O,
    withLoading: IsLoading?,
): Lazy<S> where O : LifecycleOwner, O : ErrorEvents, O : LoadingStateHook {
    owner.lifecycleScope.launchWhenCreated {
        withLoading?.add(value.isLoading)
        value.eventNotifier.handleEvents(owner as E, owner)
    }
    return this
}
