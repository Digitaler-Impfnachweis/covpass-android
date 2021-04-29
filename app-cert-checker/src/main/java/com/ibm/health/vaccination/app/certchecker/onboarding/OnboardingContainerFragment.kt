package com.ibm.health.vaccination.app.certchecker.onboarding

import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.vaccination.app.onboarding.BaseOnboardingContainerFragment
import com.ibm.health.common.vaccination.app.utils.SimpleFragmentStateAdapter
import com.ibm.health.vaccination.app.certchecker.dependencies.certCheckerDeps
import com.ibm.health.vaccination.app.certchecker.main.MainFragmentNav
import kotlinx.parcelize.Parcelize

@Parcelize
class OnboardingContainerFragmentNav : FragmentNav(OnboardingContainerFragment::class)

class OnboardingContainerFragment : BaseOnboardingContainerFragment() {

    override val fragmentStateAdapter by lazy {
        SimpleFragmentStateAdapter(
            parent = this,
            fragments = listOf(
                OnboardingInfo1Fragment(),
                OnboardingInfo2Fragment(),
                OnboardingConsentFragment(),
            ),
        )
    }

    override fun finishOnboarding() {
        launchWhenStarted {
            certCheckerDeps.storage.onboardingDone.set(true)
            findNavigator().popAll()
            findNavigator().push(MainFragmentNav(), true)
        }
    }
}
