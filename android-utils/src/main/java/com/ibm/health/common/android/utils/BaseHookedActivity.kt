/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils

import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.ensody.reactivestate.MutableValueFlow
import com.ensody.reactivestate.withErrorReporting
import kotlinx.coroutines.CoroutineScope

/** Base class that comes with hook support. */
public abstract class BaseHookedActivity(@LayoutRes contentLayoutId: Int = 0) :
    AppCompatActivity(contentLayoutId),
    LoadingStateHook,
    BaseEvents {

    /** Override this to define a [ViewBinding] that is automatically inflated. */
    public open val binding: ViewBinding? = null

    override val loading: MutableValueFlow<Int> = MutableValueFlow(0)

    /** Helper to abstract away activity and fragment differences. */
    public open fun requireActivity(): FragmentActivity = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenStarted {
            watchLoading(loading, ::setLoading)
        }
        binding?.also {
            setContentView(it.root)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
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
