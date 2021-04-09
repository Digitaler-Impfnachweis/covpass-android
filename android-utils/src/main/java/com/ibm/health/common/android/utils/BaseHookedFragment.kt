package com.ibm.health.common.android.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ensody.reactivestate.withErrorReporting
import kotlinx.coroutines.CoroutineScope

/** Base class that comes with hook support. */
public abstract class BaseHookedFragment(@LayoutRes contentLayoutId: Int = 0) :
    Fragment(contentLayoutId),
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

    public open fun withErrorReporting(block: () -> Unit) {
        withErrorReporting(::onError) {
            block()
        }
    }
}
