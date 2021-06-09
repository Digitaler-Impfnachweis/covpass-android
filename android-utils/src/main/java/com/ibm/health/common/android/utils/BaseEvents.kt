/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils

import com.ensody.reactivestate.ErrorEvents
import com.ensody.reactivestate.ReactiveState

/**
 * Base interface for an event listener receiving events from [ReactiveState.eventNotifier].
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
 * and trigger an error dialog exactly once. In contrast, [ReactiveState.loading] is an observable state because when
 * the UI is re-created you usually want to set the loading indicator to visible again and again and again.
 */
public interface BaseEvents : ErrorEvents
