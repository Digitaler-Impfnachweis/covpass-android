package com.ibm.health.common.android.utils.test.reactive

import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import com.ibm.health.common.android.utils.State
import com.ibm.health.common.android.utils.test.BaseTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope

/** Base class for testing an [State]. */
public abstract class BaseStateTest<E : BaseEvents, S : State<E>> : BaseTest() {
    protected abstract val events: E
    protected abstract val state: S

    /** Optional parent [State] in case you need the parent delegation pattern. */
    protected val parentState: State<E> by lazy { WrapperState(coroutineTestRule.testCoroutineScope) }

    override fun runBlockingTest(block: suspend TestCoroutineScope.() -> Unit) {
        super.runBlockingTest {
            coroutineTestRule.testCoroutineScope.launch {
                // We collect the state's eventNotifier in case it doesn't use the parentState
                state.eventNotifier.collect { events.it() }
            }
            advanceUntilIdle()
            block()
        }
    }
}

private class WrapperState<T : BaseEvents>(scope: CoroutineScope) : BaseState<T>(scope)
