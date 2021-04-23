package com.ibm.health.vaccination.app.certchecker.onboarding

import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.getArgs
import com.ibm.health.common.vaccination.app.onboarding.BaseOnboardingInfoFragment
import com.ibm.health.vaccination.app.certchecker.R
import kotlinx.parcelize.Parcelize

@Parcelize
class OnboardingInfoFragmentNav(val position: Int) : FragmentNav(OnboardingInfoFragment::class)

class OnboardingInfoFragment : BaseOnboardingInfoFragment() {

    override fun getTitleRes(): Int =
        when (getArgs<OnboardingInfoFragmentNav>().position) {
            0 -> R.string.onboarding_info_title_1
            1 -> R.string.onboarding_info_title_2
            else -> throw IllegalArgumentException()
        }

    override fun getTextRes(): Int =
        when (getArgs<OnboardingInfoFragmentNav>().position) {
            0 -> R.string.onboarding_info_text_1
            1 -> R.string.onboarding_info_text_2
            else -> throw IllegalArgumentException()
        }

    // FIXME use final icon
    override fun getImageRes(): Int =
        when (getArgs<OnboardingInfoFragmentNav>().position) {
            0 -> R.drawable.onboarding_info_validation_1
            1 -> R.drawable.onboarding_info_validation_2
            else -> throw IllegalArgumentException()
        }
}
