/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils.test.reactive

import com.ensody.reactivestate.ReactiveState
import com.ensody.reactivestate.test.ReactiveStateTest
import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.State

/** Base class for testing a [State]. */
public abstract class BaseStateTest<E : BaseEvents> : ReactiveStateTest<E>() {
    public abstract val state: State<E>
    override val reactiveState: ReactiveState<E> get() = state
}
