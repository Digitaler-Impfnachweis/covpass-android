package com.ibm.health.vaccination.app.vaccinee.onboarding

import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.vaccination.app.onboarding.BaseOnboardingContainerFragment
import com.ibm.health.vaccination.app.vaccinee.dependencies.vaccineeDeps
import com.ibm.health.vaccination.app.vaccinee.main.MainFragmentNav
import kotlinx.parcelize.Parcelize

@Parcelize
class OnboardingContainerFragmentNav : FragmentNav(OnboardingContainerFragment::class)

class OnboardingContainerFragment : BaseOnboardingContainerFragment() {

    override fun createFragmentStateAdapter() = OnboardingFragmentStateAdapter(this)

    override fun finishOnboarding() {
        vaccineeDeps.storage.onboardingDone = true
        findNavigator().popAll()
        findNavigator().push(MainFragmentNav(), true)
    }
}
