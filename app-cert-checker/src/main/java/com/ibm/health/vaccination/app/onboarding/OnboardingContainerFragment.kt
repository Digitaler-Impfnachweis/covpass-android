package com.ibm.health.vaccination.app.onboarding

import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.vaccination.app.onboarding.BaseOnboardingContainerFragment
import com.ibm.health.vaccination.app.dependencies.certCheckerDeps
import com.ibm.health.vaccination.app.main.MainFragmentNav
import kotlinx.parcelize.Parcelize

@Parcelize
class OnboardingContainerFragmentNav : FragmentNav(OnboardingContainerFragment::class)

class OnboardingContainerFragment : BaseOnboardingContainerFragment() {

    override fun createFragmentStateAdapter() = OnboardingFragmentStateAdapter(this)

    override fun finishOnboarding() {
        certCheckerDeps.storage.onboardingDone = true
        findNavigator().popAll()
        findNavigator().push(MainFragmentNav(), true)
    }
}
