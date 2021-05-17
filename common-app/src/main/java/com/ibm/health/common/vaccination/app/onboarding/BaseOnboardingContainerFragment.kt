package com.ibm.health.common.vaccination.app.onboarding

import android.os.Bundle
import android.view.View
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.get
import com.google.android.material.tabs.TabLayoutMediator
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.annotations.Continue
import com.ibm.health.common.vaccination.app.BaseFragment
import com.ibm.health.common.vaccination.app.databinding.OnboardingContainerBinding
import com.ibm.health.common.vaccination.app.utils.SimpleFragmentStateAdapter

/**
 * Abstract base container fragment for the onboarding fragments. It hosts a viewpager to add the actual onboarding
 * fragments.
 */
public abstract class BaseOnboardingContainerFragment : BaseFragment() {

    private val binding by viewBinding(OnboardingContainerBinding::inflate)

    protected abstract val fragmentStateAdapter: SimpleFragmentStateAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentStateAdapter.attachTo(binding.onboardingViewPager)
        TabLayoutMediator(binding.onboardingTabLayout, binding.onboardingViewPager) { _, _ ->
            // no special tab config necessary
        }.attach()

        autoRun {
            when (val fragment = get(fragmentStateAdapter.currentFragment)) {
                is BaseOnboardingConsentFragment -> {
                    binding.onboardingContinueButton.text = getString(fragment.buttonTextRes)
                    binding.onboardingContinueButton.isEnabled = get(fragment.isFormValid)
                }
                is BaseOnboardingInfoFragment -> {
                    binding.onboardingContinueButton.text = getString(fragment.buttonTextRes)
                    binding.onboardingContinueButton.isEnabled = true
                }
            }
        }

        binding.onboardingContinueButton.setOnClickListener {
            val currentItemPosition = binding.onboardingViewPager.currentItem
            if (currentItemPosition < fragmentStateAdapter.itemCount - 1) {
                binding.onboardingViewPager.currentItem = currentItemPosition + 1
            } else {
                finishOnboarding()
            }
        }
    }

    override fun onBackPressed(): Abortable {
        val currentItemPosition = binding.onboardingViewPager.currentItem
        return if (currentItemPosition > 0) {
            binding.onboardingViewPager.currentItem = currentItemPosition - 1
            Abort
        } else {
            Continue
        }
    }

    protected abstract fun finishOnboarding()
}
