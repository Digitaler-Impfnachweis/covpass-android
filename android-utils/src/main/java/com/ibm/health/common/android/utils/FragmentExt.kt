/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.ensody.reactivestate.android.onDestroyViewOnce

/**
 * Attaches a [FragmentStateAdapter] to a [ViewPager2] and sets the adapter to `null` in `onDestroyView`.
 *
 * Use this function to prevent memory leaks.
 */
public fun Fragment.attachViewPager(adapter: FragmentStateAdapter, viewPager: ViewPager2) {
    viewPager.adapter = adapter
    onDestroyViewOnce {
        viewPager.adapter = null
    }
}

/**
 * Sets a [Toolbar] as support action bar to this [Fragment]. Sets the support action bar to `null` in `onDestroyView`,
 * if it equals the one that was set in this fragment.
 *
 * Use this function to prevent memory leaks.
 */
public fun Fragment.attachToolbar(toolbar: Toolbar) {
    (this.activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
    val setActionBar = (this.activity as? AppCompatActivity)?.supportActionBar
    onDestroyViewOnce {
        (this.activity as? AppCompatActivity)?.let {
            if (it.supportActionBar == setActionBar) {
                it.setSupportActionBar(null)
            }
        }
    }
}
