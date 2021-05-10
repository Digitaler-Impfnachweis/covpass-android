package com.ibm.health.vaccination.app.vaccinee.onboarding

import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.vaccination.app.dependencies.commonDeps
import com.ibm.health.common.vaccination.app.onboarding.BaseOnboardingContainerFragment
import com.ibm.health.common.vaccination.app.utils.SimpleFragmentStateAdapter
import com.ibm.health.vaccination.app.vaccinee.main.MainFragmentNav
import kotlinx.parcelize.Parcelize

@Parcelize
internal class OnboardingContainerFragmentNav : FragmentNav(OnboardingContainerFragment::class)

internal class OnboardingContainerFragment : BaseOnboardingContainerFragment() {

    override val fragmentStateAdapter by lazy {
        SimpleFragmentStateAdapter(
            parent = this,
            fragments = listOf(
                OnboardingInfo1Fragment(),
                OnboardingInfo2Fragment(),
                OnboardingInfo3Fragment(),
                OnboardingConsentFragment(),
            ),
        )
    }

    override fun finishOnboarding() {
        launchWhenStarted {
            commonDeps.onboardingRepository.onboardingDone.set(true)
            findNavigator().popAll()
            findNavigator().push(MainFragmentNav(), true)
        }
    }
}
