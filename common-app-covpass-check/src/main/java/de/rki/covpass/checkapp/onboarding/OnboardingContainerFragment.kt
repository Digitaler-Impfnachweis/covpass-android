/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.onboarding

import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.onboarding.BaseOnboardingContainerFragment
import de.rki.covpass.commonapp.utils.SimpleFragmentStateAdapter
import de.rki.covpass.checkapp.main.MainFragmentNav
import kotlinx.parcelize.Parcelize

@Parcelize
internal class OnboardingContainerFragmentNav : FragmentNav(OnboardingContainerFragment::class)

/**
 * Fragment which holds the [SimpleFragmentStateAdapter] with CovPass Check specific onboarding steps.
 */
internal class OnboardingContainerFragment : BaseOnboardingContainerFragment() {

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
            commonDeps.onboardingRepository.onboardingDone.set(true)
            findNavigator().popAll()
            findNavigator().push(MainFragmentNav(), true)
        }
    }
}
