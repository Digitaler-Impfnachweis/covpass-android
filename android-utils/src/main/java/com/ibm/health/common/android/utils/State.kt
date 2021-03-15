package com.ibm.health.common.android.utils

import com.ensody.reactivestate.CoroutineLauncher
import com.ensody.reactivestate.ErrorEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Base interface for an event listener receiving events from [State.eventNotifier].
 *
 * The events are implemented as simple methods (e.g. [onError]).
 *
 * The `Activity` or `Fragment` implementing this interface has to make sure that it only processes the events
 * in at least the STARTED state. Otherwise the app can crash because the UI isn't ready.
 *
 * NOTE: There are no special event classes because with event classes you'd need an event loop and a `when()` statement
 * dispatching over event class types and then calling methods on the `Activity`/`Fragment`. So in the end you're doing
 * all this ceremony just to call a method on the `Activity`/`Fragment`.
 * That's why we skip the event classes and directly use methods to represent events. No need for boilerplate.
 *
 * NOTE 2: It's important to make a proper distinction between observable state (via `StateFlow`) and real events.
 * Events are things that should be consumed by the UI only once. If the UI gets destroyed and re-created,
 * an event wouldn't be triggered again. In contrast, observable state is used to always reproduce the exact same
 * UI (even if the `Activity` is re-created), so use `StateFlow` for things that should be re-executed on re-creation.
 * For example, [onError] is usually an event, consumed only once, because you want to log the error exactly once
 * and trigger an error dialog exactly once. In contrast, [State.isLoading] is an observable state because when
 * the UI is re-created you usually want to set the loading indicator to visible again and again and again.
 */
public interface BaseEvents : ErrorEvents

/**
 * Base interface anything that holds [StateFlow]s, sends events and has its lifecycle bound to a [CoroutineScope].
 *
 * We can use this as the base class for stateful UseCases that behave almost like a ViewModel, but shouldn't have their
 * own [CoroutineScope] because then you'd have to manually cancel the scope and anything that can be forgotten
 * increases the potential for bugs. So, we provide a slightly safer general concept.
 *
 * Typical usage:
 *
 * ```kotlin
 * open class MyState<T: MyEvents>(parentState: State<T>) : State<T> by parentState {
 *     fun foo() {
 *         launch {
 *             // something with error handling.
 *         }
 *     }
 * }
 * ```
 *
 * Note that you should use a generic / type argument to allow subclass to use an extended `MyFatView` sub-interface.
 */
public interface State<T : BaseEvents> : EventNotifierOwner<T>, CoroutineLauncher {
    /** Whether this object is currently loading data. Coroutines launched `withLoading = false` aren't tracked. */
    public val isLoading: StateFlow<Boolean>
}
