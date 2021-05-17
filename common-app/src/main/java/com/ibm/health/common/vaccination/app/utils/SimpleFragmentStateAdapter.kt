package com.ibm.health.common.vaccination.app.utils

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.ensody.reactivestate.MutableValueFlow
import com.ensody.reactivestate.android.onDestroyViewOnce
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Simple [FragmentStateAdapter] that takes a list of [Fragment] as argument.
 */
public class SimpleFragmentStateAdapter(
    private val parent: Fragment,
    fragments: List<Fragment>,
) : FragmentStateAdapter(parent) {

    private val _currentFragment = MutableValueFlow<Fragment?>(null)
    public val currentFragment: StateFlow<Fragment?> = _currentFragment.asStateFlow()

    public val fragments: List<Fragment> = fragments.map { fragment ->
        // Handle state restoration
        parent.childFragmentManager.fragments.firstOrNull { it::class == fragment::class }
            ?: fragment
    }

    public fun attachTo(viewPager: ViewPager2) {
        viewPager.adapter = this
        parent.onDestroyViewOnce {
            viewPager.adapter = null
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                _currentFragment.value = fragments[viewPager.currentItem]
            }
        })
    }

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}
