/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.onboarding

import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.lifecycle.lifecycleScope
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.get
import com.google.android.material.tabs.TabLayoutMediator
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.annotations.Continue
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.databinding.OnboardingContainerBinding
import de.rki.covpass.commonapp.utils.SimpleFragmentStateAdapter

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
                    get(fragment.isScrolledToBottom).let {
                        binding.onboardingContinueButton.isInvisible = !it
                        binding.onboardingScrollDownButton.isInvisible = it
                    }
                    binding.onboardingContinueButton.setText(fragment.buttonTextRes)
                    fragment.lifecycleScope.launchWhenResumed {
                        fragment.resetFocus()
                    }
                }
                is BaseOnboardingInfoFragment -> {
                    binding.onboardingContinueButton.isInvisible = false
                    binding.onboardingScrollDownButton.isInvisible = true
                    binding.onboardingContinueButton.setText(fragment.buttonTextRes)
                    fragment.lifecycleScope.launchWhenResumed {
                        fragment.resetFocus()
                    }
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
        binding.onboardingScrollDownButton.setOnClickListener {
            (fragmentStateAdapter.currentFragment.value as? BaseOnboardingConsentFragment)?.scrollToBottom()
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
