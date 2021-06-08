/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.ensody.reactivestate.ErrorEvents
import com.ensody.reactivestate.android.BuildOnViewModelContext
import com.ensody.reactivestate.android.reactiveState
import com.ensody.reactivestate.incrementFrom

/**
 * Creates a [State] wrapped in a ViewModel and observes its [State.eventNotifier] and [State.loading].
 *
 * The [block] has the same semantics as with [reactiveState].
 */
public inline fun <reified E : BaseEvents, reified S : State<E>, O> O.buildState(
    noinline block: BuildOnViewModelContext.() -> S,
): Lazy<S> where O : Fragment, O : ErrorEvents, O : LoadingStateHook =
    reactiveState(block).attachLazyState(owner = this)

/**
 * Creates a [State] wrapped in a ViewModel and observes its [State.eventNotifier] and [State.loading].
 *
 * The [block] has the same semantics as with [reactiveState].
 */
public inline fun <reified E : BaseEvents, reified S : State<E>, O> O.buildState(
    noinline block: BuildOnViewModelContext.() -> S,
): Lazy<S> where O : ComponentActivity, O : ErrorEvents, O : LoadingStateHook =
    reactiveState(block).attachLazyState(owner = this)

@Suppress("UNCHECKED_CAST")
public inline fun <reified E : ErrorEvents, S : State<E>, O> Lazy<S>.attachLazyState(
    owner: O,
): Lazy<S> where O : LifecycleOwner, O : ErrorEvents, O : LoadingStateHook {
    owner.lifecycleScope.launchWhenCreated {
        owner.loading.incrementFrom(value.loading)
    }
    return this
}
