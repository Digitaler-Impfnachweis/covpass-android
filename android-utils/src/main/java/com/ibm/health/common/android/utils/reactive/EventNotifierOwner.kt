package com.ibm.health.common.android.utils.reactive

import com.ensody.reactivestate.EventNotifier

/** Common interface for all classes holding an [EventNotifier]. */
public interface EventNotifierOwner<T> : CoroutineScopeOwner {
    /**
     * The queue/stream of events sent out by this object.
     *
     * This is actually a queue of lambda functions which are executed in the listener's correct
     * lifecycle state (STARTED). Each lambda function calls event methods on the event handler interface [T].
     *
     * Example:
     *
     * ```
     * scope.launch {
     *     try {
     *         // ...
     *     } catch (t: Throwable) {
     *         // Send a lambda function into the stream
     *         eventNotifier {
     *             // Trigger onError event
     *             onError(t)
     *         }
     *     }
     * }
     * ```
     */
    public val eventNotifier: EventNotifier<T>
}

/** Base abstract class defining a [eventNotifier]. */
public abstract class BaseEventNotifierOwner<T> : EventNotifierOwner<T> {
    override val eventNotifier: EventNotifier<T> = EventNotifier()
}
