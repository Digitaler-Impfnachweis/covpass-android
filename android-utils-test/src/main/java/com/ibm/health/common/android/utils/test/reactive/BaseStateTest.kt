package com.ibm.health.common.android.utils.test.reactive

import com.ibm.health.common.android.utils.reactive.BaseEvents
import com.ibm.health.common.android.utils.reactive.android.BaseViewModel
import com.ibm.health.common.android.utils.reactive.State
import com.ibm.health.common.android.utils.test.BaseTest
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope

/** Base class for testing an [State]. */
public abstract class BaseStateTest<E : BaseEvents, S : State<E>> : BaseTest() {
    protected abstract val events: E
    protected abstract val state: S

    protected val wrapperViewModel: BaseViewModel<E> by lazy { WrapperViewModel() }

    override fun runBlockingTest(block: suspend TestCoroutineScope.() -> Unit) {
        super.runBlockingTest {
            coroutineTestRule.testCoroutineScope.launch {
                // We collect the state's eventNotifier in case it doesn't use the wrapper
                state.eventNotifier.collect { events.it() }
            }
            advanceUntilIdle()
            block()
        }
    }
}

private class WrapperViewModel<T : BaseEvents> : BaseViewModel<T>()
