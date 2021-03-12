package com.ibm.health.common.android.utils.reactive

import com.ensody.reactivestate.CoroutineLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/*
 * --------------------------------------------------------------------------------------------------------------------
 * WHY OH WHY?
 *
 * This whole concept is about implementing correct lifecycle handling around Android's messed-up API.
 * Concepts like Model-View-Presenter cause many problems with lifecycle handling which can make your app crash or
 * lose results because your UI can be in the stopped state when your async call returns.
 *
 * With ViewModels you have a different problem: The UI and the ViewModel have two separate CoroutineScopes and you
 * have to communicate between them somehow because most operations run within the viewModelScope and the UI can't
 * wait for them to finish because the UI can also get destroyed and re-created independently of the ViewModel.
 * So, you need to send "messages" between the scopes (using our eventNotifier API).
 *
 * Also, in our code, any function call in the business logic (esp. ePA) could trigger an authentication UI flow.
 * This means, anytime during an async operation your UI code has to be prepared for going into the background
 * because some auth UI takes over. Once the auth UI returns back to the original UI screen you have to wait
 * for the UI to get back into the started state because many UI actions will crash when called in the stopped state.
 *
 * In most cases, your async action will return while the UI is still in the stopped state. So, how do we wait for it
 * to get back into the started state? We use an event queue (again, called eventNotifier).
 * --------------------------------------------------------------------------------------------------------------------
 */

/**
 * Base interface for an event listener receiving events from [State.eventNotifier].
 *
 * The events are implemented as simple methods (e.g. [onError]).
 *
 * The `Activity` or `Fragment` implementing this interface has to make sure that it only processes the events
 * in at least the STARTED state. Otherwise the app can crash because the UI isn't ready.
 * Usually we have a `BaseViewModelActivity` and `BaseViewModelFragment` which take care of processing events in the
 * correct lifecycle state.
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
public interface BaseEvents {

    /**
     * Handle the given throwable.
     * Usually this is done by showing an error dialog.
     */
    public fun onError(throwable: Throwable)
}

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
 * open class MyState<T: MyFatView>(parentState: State<T>) : State<T> by parentState {
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
