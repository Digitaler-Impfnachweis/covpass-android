/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils.test.reactive

import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.State
import com.ibm.health.common.android.utils.test.BaseTest
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope

/** Base class for testing an [State]. */
public abstract class BaseStateTest<E : BaseEvents, S : State<E>> : BaseTest() {
    protected abstract val events: E
    protected abstract val state: S

    override fun runBlockingTest(block: suspend TestCoroutineScope.() -> Unit) {
        super.runBlockingTest {
            coroutineTestRule.testCoroutineScope.launch {
                state.eventNotifier.collect { events.it() }
            }
            advanceUntilIdle()
            block()
        }
    }
}
