/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ensody.reactivestate.MutableValueFlow
import com.ensody.reactivestate.withErrorReporting
import kotlinx.coroutines.CoroutineScope

/** Base class that comes with hook support. */
public abstract class BaseHookedFragment(@LayoutRes contentLayoutId: Int = 0) :
    Fragment(contentLayoutId),
    LoadingStateHook,
    BaseEvents {

    internal var inflaterHook: ((LayoutInflater, ViewGroup?) -> View)? = null

    final override val loading: MutableValueFlow<Int> = MutableValueFlow(0)

    public open val announcementAccessibilityRes: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenStarted {
            watchLoading(loading, ::setLoading)
        }
        childFragmentManager.setupForAccessibility()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return this.inflaterHook?.invoke(inflater, container)
            ?: super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        announcementAccessibilityRes?.let { sendAccessibilityAnnouncementEvent(it) }
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

    public open fun sendAccessibilityAnnouncementEvent(accessibilityResId: Int) {
        val manager = context?.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        if (manager.isEnabled) {
            val accessibilityEvent = AccessibilityEvent.obtain()
            accessibilityEvent.eventType = AccessibilityEvent.TYPE_ANNOUNCEMENT
            accessibilityEvent.className = javaClass.name
            accessibilityEvent.packageName = requireContext().packageName
            accessibilityEvent.text.add(getString(accessibilityResId))
            manager.sendAccessibilityEvent(accessibilityEvent)
        }
    }
}
