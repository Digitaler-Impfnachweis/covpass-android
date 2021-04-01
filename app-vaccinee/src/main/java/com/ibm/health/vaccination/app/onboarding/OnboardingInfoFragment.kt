package com.ibm.health.vaccination.app.onboarding

import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.getArgs
import com.ibm.health.common.vaccination.app.onboarding.BaseOnboardingInfoFragment
import com.ibm.health.vaccination.app.R
import kotlinx.parcelize.Parcelize

@Parcelize
class OnboardingInfoFragmentNav(val position: Int) : FragmentNav(OnboardingInfoFragment::class)

class OnboardingInfoFragment : BaseOnboardingInfoFragment() {

    override fun getTitleRes(): Int =
        when (getArgs<OnboardingInfoFragmentNav>().position) {
            0 -> R.string.onboarding_info_title_1
            1 -> R.string.onboarding_info_title_2
            2 -> R.string.onboarding_info_title_3
            else -> throw IllegalArgumentException()
        }

    override fun getTextRes(): Int =
        when (getArgs<OnboardingInfoFragmentNav>().position) {
            0 -> R.string.onboarding_info_text_1
            1 -> R.string.onboarding_info_text_2
            2 -> R.string.onboarding_info_text_3
            else -> throw IllegalArgumentException()
        }

    // FIXME use final icon
    override fun getImageRes() = R.drawable.onboarding_welcome
}
