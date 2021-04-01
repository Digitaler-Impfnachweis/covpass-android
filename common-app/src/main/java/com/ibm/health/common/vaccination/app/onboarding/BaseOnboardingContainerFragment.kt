package com.ibm.health.common.vaccination.app.onboarding

import android.os.Bundle
import android.view.View
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.annotations.Continue
import com.ibm.health.common.vaccination.app.BaseFragment
import com.ibm.health.common.vaccination.app.databinding.OnboardingContainerBinding

public abstract class BaseOnboardingContainerFragment : BaseFragment() {

    private val binding by viewBinding(OnboardingContainerBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentStateAdapter = createFragmentStateAdapter()
        binding.onboardingViewPager.adapter = fragmentStateAdapter
        TabLayoutMediator(binding.onboardingTabLayout, binding.onboardingViewPager) { tab, position ->
            // no special tab config necessary
        }.attach()
        binding.onboardingContinueButton.setOnClickListener {
            val currentItemPosition = binding.onboardingViewPager.currentItem
            if (currentItemPosition < fragmentStateAdapter.itemCount - 1) {
                binding.onboardingViewPager.setCurrentItem(currentItemPosition + 1)
            } else {
                finishOnboarding()
            }
        }
    }

    override fun onBackPressed(): Abortable {
        val currentItemPosition = binding.onboardingViewPager.currentItem
        if (currentItemPosition > 0) {
            binding.onboardingViewPager.setCurrentItem(currentItemPosition - 1)
            return Abort
        } else {
            return Continue
        }
    }

    protected abstract fun createFragmentStateAdapter(): FragmentStateAdapter

    protected abstract fun finishOnboarding()
}
