/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

/**
 * Base [FragmentStateAdapter] that prevents memory leaks by setting the `adapter` to `null` in `onDestroyView`.
 */
public abstract class BaseFragmentStateAdapter(public val parent: Fragment) : FragmentStateAdapter(parent) {
    public open fun attachTo(viewPager: ViewPager2) {
        parent.attachViewPager(this, viewPager)
    }
}
