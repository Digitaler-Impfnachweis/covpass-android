/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.ensody.reactivestate.Disposable
import com.ensody.reactivestate.android.onDestroyView

/**
 * Attaches a [FragmentStateAdapter] to a [ViewPager2] and sets the adapter to `null` in [onDestroyView].
 *
 * Use this function to prevent memory leaks.
 */
public fun Fragment.attachViewPager(adapter: FragmentStateAdapter, viewPager: ViewPager2) {
    viewPager.adapter = adapter
    // FIXME/XXX: Workaround for bug in onDestroyViewOnce. Will be fixed in ReactiveState 4.0.0.
    lateinit var disposable: Disposable
    disposable = onDestroyView {
        viewPager.adapter = null
        disposable.dispose()
    }
}
