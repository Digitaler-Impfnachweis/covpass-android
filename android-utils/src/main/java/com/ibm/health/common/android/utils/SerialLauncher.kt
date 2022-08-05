/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils

import com.ensody.reactivestate.CoroutineLauncher
import com.ensody.reactivestate.MutableValueFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Launches coroutines and ensures that only one of them is running at the same time.
 *
 * @param cancelLatest If `true` this will cancel any currently running coroutine (similar to `Flow.mapLatest`).
 * @param withLoading Allows overriding the loading behavior.
 * @param block Use this to define the code block that should be executed by default or pass a custom block to [launch].
 */
public fun CoroutineLauncher.SerialLauncher(
    cancelLatest: Boolean = false,
    withLoading: MutableValueFlow<Int>? = loading,
    block: suspend CoroutineScope.() -> Unit,
): SerialLauncher =
    SerialLauncher(launcher = this, cancelLatest = cancelLatest, withLoading = withLoading, block = block)

/**
 * Launches coroutines and ensures that only one of them is running at the same time.
 *
 * This flexible version allows launching arbitrary coroutines.
 * You shouldn't expose such an instance publicly and instead wrap it behind a nice function, so the UI can't create
 * memory leaking closures.
 *
 * @param cancelLatest If `true` this will cancel any currently running coroutine (similar to `Flow.mapLatest`).
 * @param withLoading Allows overriding the loading behavior.
 */
public fun CoroutineLauncher.FlexiSerialLauncher(
    cancelLatest: Boolean = false,
    withLoading: MutableValueFlow<Int>? = loading,
): FlexiSerialLauncher =
    FlexiSerialLauncher(launcher = this, cancelLatest = cancelLatest, withLoading = withLoading)

public class SerialLauncher(
    launcher: CoroutineLauncher,
    cancelLatest: Boolean = false,
    withLoading: MutableValueFlow<Int>? = launcher.loading,
    private val block: suspend CoroutineScope.() -> Unit = {},
) : BaseSerialLauncher(launcher = launcher, cancelLatest = cancelLatest, withLoading = withLoading) {

    public operator fun invoke() {
        launch(block)
    }
}

public class FlexiSerialLauncher(
    launcher: CoroutineLauncher,
    cancelLatest: Boolean = false,
    withLoading: MutableValueFlow<Int>? = launcher.loading,
) : BaseSerialLauncher(launcher = launcher, cancelLatest = cancelLatest, withLoading = withLoading) {

    public override fun launch(block: suspend CoroutineScope.() -> Unit): Boolean =
        super.launch(block)
}

public abstract class BaseSerialLauncher(
    private val launcher: CoroutineLauncher,
    public val cancelLatest: Boolean = false,
    private val withLoading: MutableValueFlow<Int>? = launcher.loading,
) {
    protected val jobMutable: MutableStateFlow<Job?> = MutableStateFlow(null)
    public val job: StateFlow<Job?> = jobMutable.asStateFlow()

    protected val isActiveMutable: MutableStateFlow<Boolean> = MutableStateFlow(false)
    public val isActive: StateFlow<Boolean> = isActiveMutable.asStateFlow()

    protected open fun launch(block: suspend CoroutineScope.() -> Unit): Boolean {
        if (cancelLatest) {
            cancel()
        }
        if (jobMutable.value?.isActive != true) {
            isActiveMutable.value = true
            jobMutable.value = launcher.launch(withLoading = withLoading, block = block).apply {
                invokeOnCompletion { error ->
                    if (error !is CancellationException) {
                        isActiveMutable.value = false
                    }
                }
            }
            return true
        }
        return false
    }

    public fun cancel() {
        isActiveMutable.value = false
        jobMutable.value?.cancel()
    }
}
