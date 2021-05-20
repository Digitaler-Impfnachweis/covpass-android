/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import com.ensody.reactivestate.CoroutineLauncher
import com.ensody.reactivestate.withErrorReporting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/** Base class that comes with hook support. */
public abstract class BaseHookedFragment(@LayoutRes contentLayoutId: Int = 0) :
    Fragment(contentLayoutId),
    CoroutineLauncher,
    LoadingStateHook,
    BaseEvents {

    internal var inflaterHook: ((LayoutInflater, ViewGroup?) -> View)? = null

    override val isLoading: IsLoading = IsLoading()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return this.inflaterHook?.invoke(inflater, container)
            ?: super.onCreateView(inflater, container, savedInstanceState)
    }

    public open fun launchWhenStarted(block: suspend CoroutineScope.() -> Unit) {
        lifecycleScope.launchWhenStarted {
            withErrorReporting(::onError) {
                block()
            }
        }
    }

    @Deprecated(
        "Temporary solution until we integrate ReactiveState 4.x",
        ReplaceWith("lifecycleScope")
    )
    override val launcherScope: CoroutineScope get() = lifecycleScope

    @Deprecated(
        "Temporary solution until we integrate ReactiveState 4.x",
        ReplaceWith("launchWhenStarted")
    )
    override fun launch(
        context: CoroutineContext,
        start: CoroutineStart,
        withLoading: Boolean,
        onError: (suspend (Throwable) -> Unit)?,
        block: suspend CoroutineScope.() -> Unit
    ): Job =
        lifecycleScope.launch(context, start) {
            withErrorReporting(::onError) {
                whenStarted(block)
            }
        }
}
